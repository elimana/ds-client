import java.io.*;
import java.net.*;
import java.util.*;
import java.time.*;

/**
 * The client side implementation for the client-server model distributed system
 * simulator ds-sim (https://github.com/distsys-MQ/ds-sim). This implementation
 * uses the AllToLargest algorithm for job scheduling where all jobs are
 * dispatched to the largest server.
 * 
 * Arguments:
 * 
 * "-g" -> Force client to use GETS All method to retrieve server list. Client
 * defaults to parsing ds-system.xml for server list if omitted. Client will
 * always use GETS All method if ds-system.xml not found.
 */
public class DSClient {
  private final static int PORT = 50000;
  private final static String IP_ADDRESS = "localhost";
  Socket DSServer;

  public DSClient () {
  }

  public static void main(String[] args) {

    DSClient dsclient = new DSClient();

    try {

      // Setup simple log file in client local directory
      dsclient.startLog(args);

      // Connect to the ds-server instance running on the default port 50000 and
      // complete the handshake.
      dsclient.connect(PORT, System.getProperty("user.name"));

      // Get the first job for scheduling.
      Job j = dsclient.getNextJob();

      while (j != null) {
        // Get the best fit server for the job
        Server bestFitServer = dsclient.BestFitServer(j);

        // Schedule to the best fit server
        dsclient.dispatch(j, bestFitServer);
        j = dsclient.getNextJob();
      }
      
      // When there are no more jobs to schedule or an unexpected error occurs,
      // disconnect safely from the ds-server.
      dsclient.disconnect();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sorts the provided list of Server objects and retrieves the largest server
   * based on the number of cores.
   * 
   * @param servers the List of Server objects to sort in descending order based
   *                on the number of cores.
   * @return the largest Server from the list (the Server with the largest number
   *         of cores)
   */
  public Server getLargestServer(List<Server> servers) {
    // If no Servers are provided, return null.
    if (servers == null || servers.isEmpty()) {
      return null;
    }

    // Sort the list of servers in descending order based on the number of cores.
    Collections.sort(servers);

    // Return the largest Server object from the list.
    return servers.get(0);
  }

  public Server BestFitServer(Job j) {
    List<Server> capableServers = getCapableServers(j.getCore(), j.getMemory(), j.getDisk());
    Collections.sort(capableServers);

    int minFitness = Integer.MAX_VALUE;
    Server BFServer = null;
    for (Server s : capableServers) {
      if (s.getState().equals("booting") || s.getWJobs() == 0) {
        int fitness = getServerFitness(s);
        if (fitness >= j.getCore() && fitness < minFitness) {
          minFitness = fitness;
          BFServer = s;
        }
      }
    }

    if (BFServer == null) {
      // get next available server
      BFServer = capableServers.get(0);
    }
    return BFServer;
  }

  public int getServerFitness(Server s) {

    List<Job> serverJobs = new ArrayList<Job>();


    try {
      // Send 'LSTJ' to the ds-server and retrieve the list of jobs scheduled to the server.
      this.write("LSTJ " + s.getType() + " " + s.getID());

      String resp = this.read();
      //String data[] = resp.split(" ");
      //int lines = Integer.parseInt(data[1]);

      this.write("OK");

      // Create a new Job object for each retrieved job and add it to the List.
      
      String jobString = this.read();
      while (!jobString.equals(".")) {
        Job j = new Job(jobString);
        serverJobs.add(j);

        // Complete the communication with ds-server.
        this.write("OK");
        jobString = this.read();
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    int fitness = s.getCore();
    for (Job job : serverJobs) {
      if (job.getState() == 2 || s.getState().equals("booting"))
        fitness -= job.getCore();
    }

    return Math.max(fitness, 0);
  }

  /**
   * Connects to a ds-server instance and completes the authentication handshake.
   * 
   * @param port the port number that the ds-server instance is running on
   * @param user the username to authenticate with to ds-server
   * @return a Socket object containing the connection to the ds-server
   * @throws UnknownHostException
   * @throws IOException
   */
  public Socket connect(int port, String user) throws UnknownHostException, IOException {
    // Connect to the ds-server instance.
    this.DSServer = new Socket(IP_ADDRESS, port);

    // Complete the handshake.
    this.write("HELO");
    this.read();
    this.write("AUTH " + user);
    this.read();

    return DSServer;
  }

  /**
   * Decides whether to make GETS All request to ds-server, or parse ds-system.xml
   * 
   * Checks to see whether -g flag has been set at runtime. Checks to see whether
   * ds-system.xml exists in the client's local directory.
   * 
   * If -g flag has been set or ds-system.xml could not be found, makes GETS All
   * request to ds-server.
   * 
   * Otherwise, calls XMLParser to process ds-system.xml
   * 
   * @param args Array of arguments in string format, passed to DSClient.main at
   *             runtime
   * @return List of servers
   * 
   */
  public List<Server> decideGetServers(String[] args) {

    String filepath = null; // Filepath to ds-system.xml
    List<Server> servers = null; // Server list to be returned when populated
    Boolean useGetsAll = false; // Flag set according to detection of -g argument

    // Check args passed at runtime to see whether -g flag is active
    for (int i = 0; i < args.length; i++) {

      if (args[i].equals("-g")) {

        // Force client to use GETS All method to obtain list of servers
        useGetsAll = true;

      }

    }

    if (useGetsAll == true) {

      // Call own method to retrieve server list by GETS All request sent to ds-server
      servers = this.getServers();

    } else {

      filepath = XMLParser.getFilePath();

      // Check to see if ds-system.xml exists at expected path
      if (filepath == "unavailable") {

        // If it's unavailable, report & default to "GETS All" request to ds-server
        System.err.println("ds-system.xml file not found in local directory");
        servers = this.getServers();

      } else {

        // If it's available, use XMLParser to return list of servers
        System.err.println("Using XMLParser");
        XMLParser xmlParser = new XMLParser(filepath);
        servers = xmlParser.getServers();

      }

    }

    return servers;

  }

  /**
   * Retrieves the list of Servers available on ds-server using the 'GETS All'
   * command.
   * 
   * @return a List of Server objects available on ds-server.
   */
  public List<Server> getServers() {

    System.err.println("Using GETS All mode");

    // Create a list of Server objects
    List<Server> servers = new ArrayList<Server>();

    try {
      // Send 'GETS All' to the ds-server and retrieve the list of servers.
      this.write("GETS All");

      String resp = this.read();
      String data[] = resp.split(" ");
      int lines = Integer.parseInt(data[1]);

      this.write("OK");

      // Create a new Server object for each retrieved server and add it to the List.
      for (int i = 0; i < lines; i++) {
        Server s = new Server(this.read());
        servers.add(s);
      }

      // Complete the communication with ds-server.
      this.write("OK");
      this.read();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return servers;
  }

  /**
   * Retrieves the list of capable Servers available on ds-server using the 'GETS
   * Capable' command.
   * 
   * @return a List of Server objects available on ds-server.
   */
  public List<Server> getCapableServers(int core, int mem, int disk) {

    // Create a list of Server objects
    List<Server> servers = new ArrayList<Server>();

    try {
      // Send 'GETS All' to the ds-server and retrieve the list of servers.
      this.write("GETS Capable " + core + " " + mem + " " + disk);

      String resp = this.read();
      String data[] = resp.split(" ");
      int lines = Integer.parseInt(data[1]);

      this.write("OK");

      // Create a new Server object for each retrieved server and add it to the List.
      for (int i = 0; i < lines; i++) {
        Server s = new Server(this.read());
        servers.add(s);
      }

      // Complete the communication with ds-server.
      this.write("OK");
      this.read();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return servers;
  }

  /**
   * Sends 'REDY' to the server until the server sends a job to schedule (denoted
   * by either 'JOBN' or 'JOBP') and parses the server response into a Job object.
   * 
   * @return a Job object containing the attributes of the job sent by the server
   *         for scheduling. or null if the server returns 'NONE' or an unexpected
   *         reply.
   */
  public Job getNextJob() {
    Job j = null;

    try {
      // Send 'REDY' to the ds-server to request for a new job.
      this.write("REDY");

      // Read the ds-server response and extract the response message type.
      String resp = this.read();
      String type = resp.split(" ")[0];

      // If the response is a job/server status message, continue sending 'REDY' until
      // no more status messages are received.
      while (type.equals("JCPL") || type.equals("RESF") || type.equals("RESR")) {
        this.write("REDY");
        resp = this.read();
        type = resp.split(" ")[0];
      }

      // If a job is received create a new Job object for it.
      if (type.equals("JOBN") || type.equals("JOBP")) {
        j = new Job(resp);
        // Otherwise, if 'NONE' is received (no more jobs to schedule), return null
        // (disconnect).
      } else if (resp.equals("NONE")) {
        return null;
        // If there is an unexpected response print is to System.err and return null
        // (disconnect).
      } else {
        System.err.println("Unexpected response from server to 'REDY': '" + resp + "'.");
        return null;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return j;
  }

  /**
   * Dispatches or schedules a Job to a Server with the 'SCHD' command.
   * 
   * @param j the Job to be scheduled.
   * @param s the Server to which the the Job will be dispatched to.
   */
  public void dispatch(Job j, Server s) {
    try {
      // Construct the 'SCHD' scheduling message to send to the ds-server with the job
      // ID and server type and ID information.
      this.write("SCHD " + j.getID() + " " + s.getType() + " " + s.getID());
      this.read();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Disconnects the client from the connected ds-server.
   */
  public void disconnect() {
    // If there is a Socket connection to ds-server, send the 'QUIT' command to
    // disconnect safely.
    if (DSServer != null) {
      try {
        this.write("QUIT");
        this.read();
        // After quitting, close the Socket connection.
        DSServer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Read a line of text from the connected ds-server input stream. Requires
   * ds-server to be run with the '-n' parameter.
   * 
   * @return a String containing the line of text sent by ds-server
   * @throws IOException
   */
  public String read() throws IOException {
    // Retrieve the Data input stream of the Socket connection to ds-server.
    DataInputStream in = new DataInputStream(DSServer.getInputStream());
    // Use the readLine method to read a line of text sent by ds-server.
    return in.readLine();
  }

  /**
   * Write and send a line of text to the connected ds-server. Appends a new line
   * character '\n' to the provided message to comply with the ds-server running
   * with the '-n' parameter.
   * 
   * @param message a String containing the message text to be written and sent to
   *                the connected ds-server
   * @throws IOException
   */
  public void write(String message) throws IOException {
    // Append a new line character '\n' to the provided message to comply with the
    // ds-server running with the '-n' parameter.
    message = message + "\n";
    // Convert the message String to bytes and send the message to ds-server.
    DSServer.getOutputStream().write(message.getBytes());
    DSServer.getOutputStream().flush();
  }

  /**
   * Redirects system.err to a text file in client directory, for simple logging.
   * Prints header with session timestamp to log file.
   * 
   * @param String[] containing command line arguments passed to DSClient.main at
   *                 runtime
   * @throws IOException
   */
  public void startLog(String[] args) throws IOException {

    // Redirect system.err to log file in client local directory
    System.setErr(new PrintStream("log.txt"));

    // Capture local time from Epoch
    Instant current = Instant.now();
    // Parse into local time object
    LocalDateTime ldt = LocalDateTime.ofInstant(current, ZoneId.systemDefault());

    // Print local timestamp for current session
    System.err.println("Start session time:");
    System.err.printf("%s %d %d at %dh %dm%n", ldt.getMonth(), ldt.getDayOfMonth(), ldt.getYear(), ldt.getHour(),
        ldt.getMinute());

    // Print session arguments
    if (args.length == 0) {

      System.err.println("No args");

    } else {

      for (int i = 0; i < args.length; i++) {

        System.err.println("arg " + Integer.toString(i) + ": " + args[i]);

      }

    }

    // End header
    System.err.println("------");

  }

}