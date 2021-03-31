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

      List<Server> servers = dsclient.getServers();

      Server largestServer = dsclient.getLargestServer(servers);

      Job j = dsclient.getNextJob();
      while (j != null) {
        dsclient.dispatch(j, largestServer);
        j = dsclient.getNextJob();
      }
      

    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
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
      
    System.out.println(this.readLine());

    this.write("AUTH eli");
    
    System.out.println(this.readLine());

    this.write("REDY");

    this.readLine();

    return client;
  }

  public List<Server> getServers() {
    List<Server> servers = new ArrayList<Server>();

    try {
      this.write("GETS All");

      String data[] = this.readLine().split(" ");

      int lines = Integer.parseInt(data[1]);
      this.write("OK");

      for (int i = 0; i < lines; i++) {
        // System.out.println(this.read(32));
        Server s = new Server(this.readLine());
        System.out.println(s);
        servers.add(s);
      }

      this.write("OK");

      this.readLine();

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return servers;
  }

  public Job getNextJob() {
    Job j = null;

    try {
      this.write("REDY");

      String reply = this.readLine();
      if (reply.equals("NONE")) {
        this.disconnect();
        return j;
      } else if (!reply.split(" ")[0].equals("JOBN")) {
        return this.getNextJob();
      } else {
        j = new Job(reply);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return j;
  }

  public void dispatch(Job j, Server s) {
    try {
      System.out.println("SCHD " + j.ID + " " + s.type + " " + s.ID);
      this.write("SCHD " + j.ID + " " + s.type + " " + s.ID);
      System.out.println(this.readLine());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void disconnect() {
    if (client != null) {
      try {
        this.write("QUIT");
        System.out.println(this.readLine());
        client.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  public String read() throws IOException {
    return read(32);
  }

  public String read(int len) throws IOException {
    byte[] b = new byte[len];
    InputStream in = client.getInputStream();
    in.read(b);
    return new String(b);
  }

  public String readLine() throws IOException {
    DataInputStream in = new DataInputStream(client.getInputStream());
    return in.readLine();
  }

  public void write(String message) throws IOException {
    message = message + "\n";
    client.getOutputStream().write(message.getBytes());
    client.getOutputStream().flush();
  }

}
