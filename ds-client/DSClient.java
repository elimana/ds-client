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
      dsclient.connect(50000);

      Job j = dsclient.getNextJob();

      List<Server> servers = dsclient.getServers();
      Server largestServer = dsclient.getLargestServer(servers);

      while (j != null) {
        dsclient.dispatch(j, largestServer);
        j = dsclient.getNextJob();
      }
      
      dsclient.disconnect();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Server getLargestServer(List<Server> servers) {
    Collections.sort(servers);

    return servers.get(0);
  }

  public Socket connect(int port) throws UnknownHostException, IOException {
    client = new Socket("localhost", 50000);

    // Handshake
    this.write("HELO");
      
    this.read();

    this.write("AUTH eli");
    
    this.read();

    return client;
  }

  public List<Server> getServers() {
    List<Server> servers = new ArrayList<Server>();

    try {
      this.write("GETS All");

      String resp = this.read();

      String data[] = resp.split(" ");

      int lines = Integer.parseInt(data[1]);
      this.write("OK");

      for (int i = 0; i < lines; i++) {
        Server s = new Server(this.read());
        servers.add(s);
      }

      this.write("OK");

      this.read();

    } catch (IOException e) {
      e.printStackTrace();
    }

    return servers;
  }

  public Job getNextJob() {
    Job j = null;

    try {
      this.write("REDY");

      String resp = this.read();
      String type = resp.split(" ")[0];

      while (type.equals("JCPL") || type.equals("RESF") || type.equals("RESR")) {
        this.write("REDY");
        resp = this.read();
        type = resp.split(" ")[0];
      }

      if (type.equals("JOBN") || type.equals("JOBP")) {
        j = new Job(resp);
      } else if (resp.equals("NONE")) {
        return null;
      } else {
        System.err.println("Unexpected response from server to 'REDY': '" + resp + "'.");
        // unexpected error
        return null;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return j;
  }

  public void dispatch(Job j, Server s) {
    try {
      this.write("SCHD " + j.ID + " " + s.type + " " + s.ID);
      this.read();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void disconnect() {
    if (client != null) {
      try {
        this.write("QUIT");
        this.read();
        client.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public String read() throws IOException {
    DataInputStream in = new DataInputStream(client.getInputStream());
    return in.readLine();
  }

  public void write(String message) throws IOException {
    message = message + "\n";
    client.getOutputStream().write(message.getBytes());
    client.getOutputStream().flush();
  }

}
