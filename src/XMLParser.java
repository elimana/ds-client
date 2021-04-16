import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

/**
 * Parses the ds-system.xml file and generates a list of Server objects
 */
public class XMLParser {
  private Document xmlDocument;

  public static void main(String[] args) {
    // Test of parsing ds-system.xml and getting the server list
    XMLParser xmlParser = new XMLParser(XMLParser.getFilePath());
    List<Server> serverObjListTest = xmlParser.getServers();

    // Print the server list
    System.out.println("Output List:");
    for (int i = 0; i < serverObjListTest.size(); i++) {
      System.out.println(serverObjListTest.get(i).getType() + " " + serverObjListTest.get(i).getID() + " "
              + serverObjListTest.get(i).getState() + " " + serverObjListTest.get(i).getCurStartTime() + " "
              + serverObjListTest.get(i).getCore() + " " + serverObjListTest.get(i).getMem() + " "
              + serverObjListTest.get(i).getDisk() + " " + serverObjListTest.get(i).getBootTime() + " "
              + serverObjListTest.get(i).getHourlyRate());
    }
  }

  public XMLParser(String filepath) {
    try {
      // Load the ds-system.xml file and parse it as an xml Document object
      File inputFile = new File(filepath);

      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

      xmlDocument = dBuilder.parse(inputFile);
      xmlDocument.getDocumentElement().normalize();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the list of Servers from the ds-system.xml file as a List of Server
   * objects.
   * 
   * @return a List of Server objects from the ds-system.xml file
   */
  public List<Server> getServers() {
    List<Server> serverObjList = new ArrayList<Server>();

    // If no xml document is loaded, return an empty list
    if (xmlDocument == null) {
      return serverObjList;
    }

    // Get the elements under the 'server' tag in the ds-system.xml file
    NodeList serverNodeList = this.xmlDocument.getElementsByTagName("server");
    for (int i = 0; i < serverNodeList.getLength(); i++) {
      Node serverNode = serverNodeList.item(i);
      if (serverNode.getNodeType() == Node.ELEMENT_NODE) {
        Element serverElement = (Element) serverNode;
        
        // For each server of a type (specified by the 'limit' attribute) generate a
        // Server object with its unique ID and add it to the List
        int numServers = Integer.parseInt(serverElement.getAttribute("limit"));
        for (int ID = 0; ID < numServers; ID++) {
          String serverObjInitString = "";

          // 0) Server Type
          serverObjInitString += (serverElement.getAttribute("type") + " ");
          // 1) Server ID
          serverObjInitString += (Integer.toString(ID) + " ");
          // 2) Server State
          serverObjInitString += ("Unknown" + " ");
          // 3) Cur Start Time
          serverObjInitString += ("-1" + " ");
          // 4) Core Count
          serverObjInitString += (serverElement.getAttribute("coreCount") + " ");
          // 5 Memory
          serverObjInitString += (serverElement.getAttribute("memory") + " ");
          // 6 Disk
          serverObjInitString += (serverElement.getAttribute("disk") + " ");
          // 7 Boot Time
          serverObjInitString += (serverElement.getAttribute("bootupTime") + " ");
          // 8 Hourly Rate
          serverObjInitString += serverElement.getAttribute("hourlyRate");

          Server serverObj = new Server(serverObjInitString);
          serverObjList.add(serverObj);
        }
      }
    }

    return serverObjList;
  }

  /**
   * Gets the file path of the ds-system.xml file in the same directory as the
   * program.
   * 
   * @return the file path of ds-system.xml
   */
  public static String getFilePath() {
    String filepath = XMLParser.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "ds-system.xml";

    // Account for possible space chars in filepath
    filepath = filepath.replaceAll("%20", " ");

    // Check to see whether ds-system.xml file exists in local directory
    File checkDir = new File(filepath);
    if (checkDir.exists() == false) {
      System.err.println("ds-system.xml file not found in local directory");
    }

    return filepath;
  }
}