//WIP - Currently working locally, a few issues to correct with sort function and interface with server class
//I should be able to resolve on 17/04

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class xmlparsertest {

    public static void main(String[] args) {

        System.out.println("Output List:");
        System.out.println(parseXML(getFilePath()));
        
    }
    
    public static ArrayList<Server> parseXML (String filepath) {
      
        //Returned List of Extracted Server Objects
        ArrayList<Server> serverObjList = new ArrayList<Server>();

        try{

            File inputFile = new File(filepath);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

            NodeList serverNodeList = doc.getElementsByTagName("server");
           
            for (int temp = 0; temp < serverNodeList.getLength(); temp++) {

                Node serverNode = serverNodeList.item(temp);

                if (serverNode.getNodeType() == Node.ELEMENT_NODE) {
               
                    Element serverElement = (Element) serverNode;

                    System.out.println("No. " + (temp+1));
                    System.out.println("Number of servers of this type: " + serverElement.getAttribute("limit"));

                    int numServers = Integer.parseInt(serverElement.getAttribute("limit"));

                    for (int tempy = 0; tempy < numServers; tempy++) {

                        String serverObjInitString = "";
                        
                        //0) Server Type
                        serverObjInitString += (serverElement.getAttribute("type") + " ");
                        
                        //1) Server ID 
                        serverObjInitString += (Integer.toString(tempy+1) + " ");
                        
                        //2) Server State
                        serverObjInitString += ("Unknown" + " ");

                        //3) Cur Start Time
                        serverObjInitString += ("-1" + " ");

                        //4) Core Count
                        serverObjInitString += (serverElement.getAttribute("coreCount") + " ");
                        
                        //5 Memory
                        serverObjInitString += (serverElement.getAttribute("memory") + " ");

                        //6 Disk
                        serverObjInitString += (serverElement.getAttribute("disk") + " ");
                    
                        //7 Boot Time
                        serverObjInitString += (serverElement.getAttribute("bootupTime") + " ");

                        //8 Hourly Rate
                        serverObjInitString += serverElement.getAttribute("hourlyRate");
                        
                        System.out.println("String is: " + serverObjInitString);
                        
                        Server serverObj = new Server(serverObjInitString);
                        serverObjList.add(serverObj);

                    }
                
                }

            }

           // System.out.println(serverObjList);
        
        } catch (Exception e) {

                e.printStackTrace();
                System.exit(-1);

        }

        return serverObjList;

    }

    public static String getFilePath() {

        String filepath = xmlparsertest.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "ds-system.xml";
        System.out.println("Filepath is: " + filepath); //Account for spaces in filepath name
        return filepath;

    }

    public static ArrayList<Server> sortByNumCores (ArrayList<Server> unsorted) {

        //TBC

    }


}
