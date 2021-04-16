import java.io.*;
import java.net.*;
import java.util.*;

public class DSClient {

  Socket client;

  public DSClient () {
  }

  public static void main(String[] args) {
     
    DSClient dsclient = new DSClient();

    try {
      // Connect to the ds-server instance running on the default port 50000 and
      // complete the handshake.
      dsclient.connect(50000, "eli");

      // Get the first job for scheduling.
      Job j = dsclient.getNextJob();

      // Get the list of available servers and store the largest server.
      List<Server> servers = dsclient.getServers();
      Server largestServer = dsclient.getLargestServer(servers);

      // While there are jobs to schedule, get them and dispatch them all to the
      // largest server.
      while (j != null) {
        dsclient.dispatch(j, largestServer);
        j = dsclient.getNextJob();
      }
      
      // When there are no more jobs to schedule or an unexpected error occurs,
      // disconnect safely fro the ds-server.
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
    this.client = new Socket("localhost", port);

    // Complete the handshake.
    this.write("HELO");
    this.read();
    this.write("AUTH " + user);
    this.read();

    return client;
  }

  /**
   * Retrieves the list of Servers available on ds-server using the 'GETS All'
   * command.
   * 
   * @return a List of Server objects available on ds-server.
   */
  public List<Server> getServers() {
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
      this.write("SCHD " + j.ID + " " + s.type + " " + s.ID);
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
    if (client != null) {
      try {
        this.write("QUIT");
        this.read();
        // After quitting, close the Socket connection.
        client.close();
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
    DataInputStream in = new DataInputStream(client.getInputStream());
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
    client.getOutputStream().write(message.getBytes());
    client.getOutputStream().flush();
  }
}
