/*
 * license: RXTX License v 2.1 - LGPL v 2.1 + Linking Over Controlled Interface
 * this file is used to create communicate with firmware
 */

package main_app;

import gnu.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;
import java.util.*;
import java.lang.Math;

public class IO implements SerialPortEventListener{
    private Enumeration ports = null;
    private HashMap portMap = new HashMap();
    private InputStream input = null;
    private OutputStream output = null;
    private boolean bConnected = false;
    private CommPortIdentifier selectedPort = null;
    private SerialPort serialPort = null;
        
    public static GoogleMap gmap = new GoogleMap();
    
    public static ArrayList<Double> lat = new ArrayList<Double>();
    public static ArrayList<Double> lon = new ArrayList<Double>();
    public static ArrayList<Float> mapT = new ArrayList<>();
    public static ArrayList<Float> mapW = new ArrayList<>();
    public static ArrayList<Float> mapSIO = new ArrayList<>();
    public static ArrayList<String> mapTimeIO = new ArrayList<>();

    String logText = "";
    String portUse = new String();
    String temp = new String();
    GUI gu = MainApp.ui;
    
    /*find the port
    parameter none
    return none*/
    public void searchForPorts() {
        int ifRun = 1;
        ports = CommPortIdentifier.getPortIdentifiers();
        
        //begin to search available port
        while (ifRun == 1) {
            if (!ports.hasMoreElements()) {
                ifRun = 0;
                break;
            }
            
            CommPortIdentifier curPort = (CommPortIdentifier)ports.nextElement();
            
            //find one can be used
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {                
                portUse = curPort.getName();
                portMap.put(curPort.getName(), curPort);
                System.out.println("I find one port can be used: "+ curPort.getName());
            }
        }
    }
    
    /*try to connect with  a available port*/
    public void connect(){
        selectedPort = (CommPortIdentifier)portMap.get(portUse);
        CommPort commPort = null;
        
        //try to connect
        try {
            commPort = selectedPort.open("TigerControlPanel", 2000);
            serialPort = (SerialPort)commPort;
            this.bConnected = true;
        } catch (Exception e) {
            System.out.println("port is not available");
        } 
    }
    
    /*try to init port
    parameter none
    return boolean*/
    public boolean initIOStream()
    {
        boolean successful = false;

        try {
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();           
            successful = true;
            return successful;
        }
        catch (IOException e) {
            System.out.println("port is not available");
            return successful;
        }
    }
    
    
    /*init listener
    parameter none
    return none*/
    public void initListener() {
        try {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (TooManyListenersException e)
        {
            System.out.println("initlistener failed");
        }
    }
    
    /*get status
    parameter none
    return boolean*/
    final public boolean getConnected() {
        return bConnected;
    }
    
    
    /*update the GPS value from the file
    parameter arraylist<float>
    return none*/
    public void UpdateGPS (ArrayList<Float> GPS, String t) {
        mapTimeIO.add(t);
        mapW.add(GPS.get(1));
        mapT.add(GPS.get(2));
        mapSIO.add(GPS.get(3));
        lat.add((double)GPS.get(4));
        lon.add((double)GPS.get(5));        
    }
    
    /*clean the GPS value 
    parameter none
    return none*/
    public void CleanGPS () {
        mapTimeIO.clear();
        mapW.clear();
        mapT.clear();
        mapSIO.clear();
        lat.clear();
        lon.clear();
    }

    /*listen the port
    parameter port
    return none*/
    public void serialEvent(SerialPortEvent evt) {
        Scanner z;
        String timeGPS = "";
        
        //listen the port
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                byte singleData = (byte)input.read();
                
                if (singleData != 10) {
                    logText = new String(new byte[] {singleData});
                    
                    temp += logText;
                    
                }
                else {
                    //begin to analyse the data
                    temp = temp.trim();
                    if (temp.charAt(0) == '!' || temp.charAt(1) == '!') {
                        if (temp.contains(",,,,")) {
                            return;
                        }
                        
                        z = new Scanner(temp.substring(1).replaceAll("S.", " -1 ").replaceAll("E.", " 1 ").
                                replaceAll("N.", " 1 ").replaceAll("W.", " -1 ").replaceAll("[^-.0-9]", " "));
                        mapT.add(z.nextFloat());
                        mapW.add(z.nextFloat());
                        mapSIO.add(z.nextFloat());
                        timeGPS = "." + (int)z.nextFloat();
                        lat.add(z.nextDouble() * z.nextInt());
                        lon.add(z.nextDouble() * z.nextInt());
                        z.nextFloat();
                        timeGPS = z.nextInt() + timeGPS;
                        mapTimeIO.add(timeGPS);
                        z.close();
                        timeGPS = "";
                       
                        //transmitting gps is end
                    } else if (temp.charAt(0) == '?' || temp.charAt(1) == '?') {                      
                        gmap.runMap();
 
                        mapT.clear();
                        mapW.clear();
                        mapSIO.clear();
                        lat.clear();
                        lon.clear();
                        
                    } else {
                        
                        gu.update2(temp);
                        
                    }

                    temp = "";
                }
            }
            catch (Exception e){                
                System.out.println("Failed to read data!!!!" + e.getMessage());
            }
        }
    }

    /*write towards the usb port
    parameter int
    return none*/
    public void writeData(int c) {
        try {
            output.write(c);
            output.flush();
        
        }
        catch (Exception e) {
            System.out.println("Failed to write data");
 
        }
    }

}
