import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;
import java.time.*;

/**
 * The client side implementation for the client-server model distributed system
 * simulator ds-sim (https://github.com/distsys-MQ/ds-sim). This implementation
 * uses the AllToLargest algorithm for job scheduling where all jobs are
 * dispatched to the largest server.
 * 
 * Arguments:
 * 
 * "-g | --getsall" -> Force client to use GETS All method to retrieve server
 * list. Client defaults to parsing ds-system.xml for server list if omitted.
 * Client will always use GETS All method if ds-system.xml not found.
 * 
 * "-e | --est" -> Uses estimated server waiting time instead of the more
 * precise next available server time to speed up the program.
 * 
 * "-t | --termidle" -> Terminate servers when they are idle.
 * 
 * "-b | --boot" -> Consider booting servers as available servers with no
 * waiting jobs.
 * 
 * "-f | --fitcore" -> Calculate server fitness in Best Fit by only the number
 * of available cores.
 */
public class DSClient {
  private final static int PORT = 50000;
  private final static String IP_ADDRESS = "localhost";

  Socket DSServer;
  boolean useEstWaitTime = false, terminateIdleServers = false, useXMLParser = true,
      bootingAsAvailable = false, fitnessByCore = false;;

  public DSClient () {
  }

  public static void main(String[] args) {

    DSClient dsclient = new DSClient();

    try {
      // Setup simple log file in client local directory
      dsclient.startLog(args);

      // Configure the client and scheduling algorithm
      dsclient.configureClient(args);

      // Connect to the ds-server instance running on the default port 50000 and
      // complete the handshake.
      dsclient.connect(PORT, System.getProperty("user.name"));

      // Get the first job for scheduling.
      Job j = dsclient.getNextJob();

      while (j != null) {
        // Get the best fit server for the job
        Server bestFitServer;
        bestFitServer = dsclient.bestFitServer(j);

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
   * Parses the argument list and sets the specified program configuration
   * parameters.
   * 
   * @param args the program runtime arguments
   */
  public void configureClient(String[] args) {
    // Check through each argument and set the corresponding program configuration
    // variable.
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-g") || args[i].equals("--getsall")) {
        useXMLParser = false;
      } else if (args[i].equals("-t") || args[i].equals("-termidle")) {
        terminateIdleServers = true;
      } else if (args[i].equals("-e") || args[i].equals("--est")) {
        useEstWaitTime = true;
      } else if (args[i].equals("-b") || args[i].equals("--boot")) {
        bootingAsAvailable = true;
      } else if (args[i].equals("-f") || args[i].equals("--fitcore")) {
        fitnessByCore = true;
      } else if (args[i].equals("--help")) {
        System.out.println("Usage: DSClient [-g | --getsall] [-c | --cost] [-e | --est] [-t | --termidle] [-b | --boot] [-f | --fitcore]");
        System.exit(0);
      } else {
        System.err.println("Invalid argument: " + args[i]);
        System.err.println("Usage: DSClient [-g | --getsall] [-c | --cost] [-e | --est] [-t | --termidle] [-b | --boot] [-f | --fitcore]");
        System.exit(1);
      }
    }

    // Check to see if ds-system.xml exists at expected path if using XMLParser
    if (useXMLParser) {
      String filepath = XMLParser.getFilePath();
      if (filepath == "unavailable") {
        // If it's unavailable, report & default to "GETS All" request to ds-server
        System.err.println("ds-system.xml file not found in local directory");
        useXMLParser = false;
      }
    }
  }

  /**
   * This function gets a list of capable servers using the getCapableServers()
   * function which sends ‘GETS Capable’ to ds-sim, and then implements best fit
   * by finding the server with sufficient available resources for the job and
   * with the lowest aggregated fitness value from the list. The available server
   * resources are calculated by the calcServerUtilisation() function. If there is
   * no capable server with sufficient available resources for the job, the next
   * available server is retrieved with the getNextAvailableServer() function that
   * returns the capable server with the lowest available server time for the job.
   * 
   * @param j the Job that the Server should fit
   * @return the best fitting Server
   */
  public Server bestFitServer(Job j) {
    // Get a list of servers with the capable for the job.
    List<Server> capableServers = getCapableServers(j.getCore(), j.getMemory(), j.getDisk());

    float minFitness = Float.MAX_VALUE;
    Server BFServer = null;
    // Find the available server with the lowest fitness.
    for (Server s : capableServers) {
      // Consider a server in the booting state as available if the option is
      // configured
      if ((bootingAsAvailable && s.getState().equals("booting")) || s.getWJobs() == 0) {
        Resource fitness = calcServerUtilisation(s);
        if (fitness.getPendingJobs() == 0 && fitness.getAvailableMem() >= j.getMemory()
            && fitness.getAvailableDisk() >= j.getDisk() && fitness.getAvailableCores() >= j.getCore()) {
          
          float statistic;
          // Calculate the fitness ans the available cores if configured as such,
          // otherwise calculate the combined fitness statistic.
          if (fitnessByCore) {
            statistic = fitness.getAvailableCores();
          } else {
            statistic = ((float) fitness.getAvailableCores() / (float) j.getCore())
                + ((float) fitness.getAvailableMem() / (float) j.getMemory())
                + ((float) fitness.getAvailableDisk() / (float) j.getDisk());
          }

          if (statistic < minFitness) {
            minFitness = statistic;
            BFServer = s;
          }
        }
      }
    }

    // If there are no available servers, get the next available server.
    if (BFServer == null) {
      BFServer = getNextAvailableServer(capableServers, j.getCore(), j.getMemory(), j.getDisk());
    }
    return BFServer;
  }

  /**
   * This function finds the next available server times for the servers in the
   * list and returns the server that is available soonest.
   * 
   * @param capableServers the list of Servers to compare from
   * @param reqCore        the required available cores from the server
   * @param reqMem         the required available memory from the server
   * @param reqDisk        the required available disk from the server
   * @return
   */
  public Server getNextAvailableServer(List<Server> capableServers, int reqCore, int reqMem, int reqDisk) {
    int minTime = Integer.MAX_VALUE;
    Server nextServer = capableServers.get(0);
    // Find the server that is available soonest.
    for (Server s : capableServers) {
      int availableTime;
      // If configured, use estimated server wait time, otherwise use the more precise
      // getServerAvailableTime function.
      if (useEstWaitTime) {
        availableTime = getServerEstWaitTime(s);
      } else {
        availableTime = getServerAvailableTime(s, reqCore, reqMem, reqDisk);
      }
      if (availableTime < minTime) {
        nextServer = s;
        minTime = availableTime;
      }
    }

    return nextServer;
  }

  /**
   * Get the estimated server wait time from ds-sim using the 'EJWT' command.
   * 
   * @param s the Server to get the estimated wait time for
   * @return the estimated wait time in seconds
   */
  public int getServerEstWaitTime(Server s) {
    int estWaitTime = 0;
    try {
      // Send 'EJWT' to the ds-server and retrieve the estimated waiting time of the
      // server.
      this.write("EJWT " + s.getType() + " " + s.getID());

      String resp = this.read();

      estWaitTime = Integer.parseInt(resp);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return estWaitTime;
  }

  /**
   * This function finds the next available server time for a specified server
   * 
   * @param s       the Server to get the next available time for
   * @param reqCore the required available cores from the server
   * @param reqMem  the required available memory from the server
   * @param reqDisk the required available disk from the server
   * @return the next available server time for the Server
   */
  public int getServerAvailableTime(Server s, int reqCore, int reqMem, int reqDisk) {
    // Get a list of server jobs.
    List<Job> serverJobs = getServerJobs(s);

    // Divide the server jobs into running jobs and waiting jobs.
    List<Job> runningJobs;
    List<Job> waitingJobs;

    // If the server is booting, consider waiting jobs with a know start time as
    // running jobs.
    if (s.getState().equals("booting")) {
      runningJobs = serverJobs.stream().filter(j -> j.getStartTime() >= 0).collect(Collectors.toList());
      waitingJobs = serverJobs.stream().filter(j -> j.getStartTime() == -1).collect(Collectors.toList());
    } else {
      runningJobs = serverJobs.stream().filter(j -> j.getState() == 2).collect(Collectors.toList());
      waitingJobs = serverJobs.stream().filter(j -> j.getState() == 1).collect(Collectors.toList());
    }

    // Sort running jobs by job end time.
    runningJobs.sort((j1, j2) -> Integer.valueOf(j1.getEndTime()).compareTo(j2.getEndTime()));
    // Sort waiting jobs by id (order of submission).
    waitingJobs.sort((j1, j2) -> Integer.valueOf(j1.getID()).compareTo(j2.getID()));

    // Get the current available resources of the server.
    Resource utilisedResources = calcServerUtilisation(s);
    int availableCores = utilisedResources.getAvailableCores();
    int availableMem = utilisedResources.getAvailableMem();
    int availableDisk = utilisedResources.getAvailableDisk();
    int time = 0;

    // Iterate through the server jobs until there is enough available resources to
    // satisfy the requirement.
    while (!waitingJobs.isEmpty() || (availableCores < reqCore || availableMem < reqMem || availableDisk < reqDisk)) {
      // Remove the first job to finish from the list of running jobs and update
      // available resources.
      Job finishedJob = runningJobs.remove(0);
      // Update the time to the removed job's end time.
      time = finishedJob.getEndTime();
      availableCores += finishedJob.getCore();
      availableMem += finishedJob.getMemory();
      availableDisk += finishedJob.getDisk();

      // Add any waiting jobs that can be added to the running jobs list.
      boolean addRunJob = false;
      while (!waitingJobs.isEmpty() && waitingJobs.get(0).getCore() <= availableCores
          && waitingJobs.get(0).getMemory() <= availableMem && waitingJobs.get(0).getDisk() <= availableDisk) {
        Job nextJob = waitingJobs.remove(0);

        nextJob.setState(2);
        nextJob.setStartTime(time);

        runningJobs.add(nextJob);
        addRunJob = true;

        // Remove the resources requirements of the added waiting jobs from the
        // available server resources.
        availableCores -= nextJob.getCore();
        availableMem -= nextJob.getMemory();
        availableDisk -= nextJob.getDisk();
      }

      // If a waiting job was added to the running jobs list, resort the list by job
      // end time.
      if (addRunJob) {
        runningJobs.sort((j1, j2) -> Integer.valueOf(j1.getEndTime()).compareTo(j2.getEndTime()));
      }
    }

    // Return the next available server time.
    return time;
  }

  /**
   * Get the jobs assigned to a server from ds-sim using the 'LSTJ' command.
   * 
   * @param s the Server to get the assigned jobs for
   * @return a List of Jobs assigned to the Server
   */
  public List<Job> getServerJobs(Server s) {
    List<Job> serverJobs = new ArrayList<Job>();

    try {
      // Send 'LSTJ' to the ds-server and retrieve the list of jobs scheduled to the server.
      this.write("LSTJ " + s.getType() + " " + s.getID());
      this.read();
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

    return serverJobs;
  }

  /**
   * Terminates the specified Server using the 'TERM' command.
   * 
   * @param s the Server to terminate
   */
  public void terminateServer(Server s) {
    try {
      // Send 'TERM' to the ds-server and terminate the server.
      this.write("TERM " + s.getType() + " " + s.getID());
      this.read();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Calculates the current amount of resources available to the server, and the
   * number of pending jobs on the server.
   * 
   * @param s the Server to calculate the resources for
   * @return available Server Resources
   */
  public Resource calcServerUtilisation(Server s) {
    int availableCores = s.getCore();
    int availableMem = s.getMem();
    int availableDisk = s.getDisk();
    int pendingJobs = s.getWJobs();

    // If a Server is in a booting state, recalculate the waiting jobs to determine
    // the precise number of pending jobs.
    if (s.getState().equals("booting") && pendingJobs > 0) {
      List<Job> serverJobs = getServerJobs(s);
      for (Job job : serverJobs) {
        if (job.getState() == 1 && job.getStartTime() != -1) {
          pendingJobs--;
        }
      }
    }

    Resource serverUtilisation = new Resource(availableCores, availableMem, availableDisk, pendingJobs);

    return serverUtilisation;
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
  public List<Server> decideGetServers() {
    String filepath = null; // Filepath to ds-system.xml
    List<Server> servers = null; // Server list to be returned when populated

    if (!useXMLParser) {
      // Call own method to retrieve server list by GETS All request sent to ds-server
      servers = this.getServers();
    } else {
      // Get the filepath of ds-system.xml
      filepath = XMLParser.getFilePath();

      // Use XMLParser to return list of servers
      XMLParser xmlParser = new XMLParser(filepath);
      servers = xmlParser.getServers();
      System.err.println("Using XMLParser");
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
        // If the program is configured to terminate idle servers and a job has
        // completed, verify if the server on which the job completed is not now idle,
        // and terminate it if it is.
        if (terminateIdleServers && type.equals("JCPL")) {
          String serverType = resp.split(" ")[3];
          int serverID = Integer.parseInt(resp.split(" ")[4]);
          Server s = new Server(serverType, serverID);
          if (getServerJobs(s).isEmpty()) {
            terminateServer(s);
          }
        }
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
  @SuppressWarnings("deprecation")
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