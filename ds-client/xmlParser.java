//Parses ds-system.xml file and returns arraylist of server objects

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class xmlParser {
    public static void main(String[] args) { // For testing only

        ArrayList<Server> serverObjListTest = parseXML(getFilePath());

        System.out.println("Output List:");

        for (int i = 0; i < serverObjListTest.size(); i++) {

            System.out.println(

                    serverObjListTest.get(i).getType() + " " + serverObjListTest.get(i).getID() + " "
                            + serverObjListTest.get(i).getState() + " " + serverObjListTest.get(i).getCurStartTime()
                            + " " + serverObjListTest.get(i).getCore() + " " + serverObjListTest.get(i).getMem() + " "
                            + serverObjListTest.get(i).getDisk() + " " + serverObjListTest.get(i).getBootTime() + " "
                            + serverObjListTest.get(i).getHourlyRate()

            );

        }

    }

    public static ArrayList<Server> parseXML(String filepath) {

        // Returned List of Extracted Server Objects
        ArrayList<Server> serverObjList = new ArrayList<Server>();

        try {

            File inputFile = new File(filepath);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList serverNodeList = doc.getElementsByTagName("server");

            for (int temp = 0; temp < serverNodeList.getLength(); temp++) {

                Node serverNode = serverNodeList.item(temp);

                if (serverNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element serverElement = (Element) serverNode;

                    int numServers = Integer.parseInt(serverElement.getAttribute("limit"));

                    for (int tempy = 0; tempy < numServers; tempy++) {

                        String serverObjInitString = "";

                        // 0) Server Type
                        serverObjInitString += (serverElement.getAttribute("type") + " ");

                        // 1) Server ID
                        serverObjInitString += (Integer.toString(tempy) + " ");

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

                        System.out.println("String is: " + serverObjInitString);

                        Server serverObj = new Server(serverObjInitString);
                        serverObjList.add(serverObj);

                    }

                }

            }

        } catch (Exception e) {

            e.printStackTrace();
            System.exit(-1);

        }

        return serverObjList;

    }

    public static String getFilePath() {

        String filepath = xmlparsertest.class.getProtectionDomain().getCodeSource().getLocation().getPath()
                + "ds-system.xml";

        // Account for possible space chars in filepath
        filepath = filepath.replaceAll("%20", " ");

        System.out.println("Filepath is: " + filepath);

        // Check to see whether ds-system.xml file exists in local directory
        File checkDir = new File(filepath);
        if (checkDir.exists() == false) {

            System.out.println("ds-system.xml file not found in local directory");
            System.exit(1);

        }

        return filepath;

    }

}
