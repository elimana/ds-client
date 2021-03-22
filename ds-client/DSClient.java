import java.io.*;
import java.net.*;

public class DSClient {

  Socket client;

  public DSClient () {
  }

  public static void main(String[] args) {
      
    DSClient dsclient = new DSClient();

    try {
      dsclient.connect(50000);

      dsclient.printJobs();

      dsclient.disconnect();
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    
  }

  public Socket connect(int port) throws UnknownHostException, IOException {
    client = new Socket("localhost", 50000);

    // Handshake
    this.write("HELO");
      
    System.out.println(this.read());

    this.write("AUTH eli");
    
    System.out.println(this.read());

    this.write("REDY");

    return client;
  }

  public void disconnect() {
    if (client != null) {
      try {
        this.write("QUIT");
        System.out.println(read());
        client.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  public String read() throws IOException {

    byte[] b = new byte[32];
    InputStream in = client.getInputStream();
    in.read(b);
    return new String(b);
  }

  public void write(String message) throws IOException {
    client.getOutputStream().write(message.getBytes());
    client.getOutputStream().flush();
  }

  public void printJobs() throws IOException {
    String input = this.read();
    while (!input.equals("NONE")) {
      System.out.println(input);
    }
  }

}
