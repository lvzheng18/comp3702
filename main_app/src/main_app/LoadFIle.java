/*
 * license: standard library, none
 * this file is used to load file
 */
package main_app;

/**
 *
 * @author lvzheng
 */
import java.io.*;
import java.util.*;

public class LoadFIle {
    
    private Scanner dataFile;
    
    /*method is used to open file
    parameter: string
    return float[][]*/
    public void openF(String datafile) {
        int dim1;
        int i = 0, j = 0;
        String timeSting;
        ArrayList<Float> recordData = new ArrayList<Float>();                
        String buffer, buffer2;
        GUI gu = MainApp.ui;
        
        //open a scanner
        try{
            dataFile = new Scanner(new File(datafile));
        }
        catch (Exception error) {
            System.out.println("I did not find the file");
        }
        
        MainApp.communicator.CleanGPS();
        
        //read file
        while(dataFile.hasNext()) {
            recordData.clear();
            buffer = dataFile.nextLine();
            timeSting = buffer.substring(0, 13);
            //convert the direction to + -
            buffer2 = buffer.replaceAll("S.", "-").replaceAll("E.", "").replaceAll("N.", "").replaceAll("W.", "-").trim();
            //split the string by ','
            for (String x: buffer2.split(",")){
                recordData.add(Float.parseFloat(x)); 
            }
           
            if (recordData.size() > 4) {
                MainApp.communicator.UpdateGPS(recordData, timeSting);
            } else {
                gu.update3(recordData.get(2), recordData.get(1), recordData.get(3));
                gu.updateTim(timeSting);
            }
        }
        dataFile.close();
        
        //run map
        if (IO.lat.size() != 0) {
            try {
                IO.gmap.runMap();
            } catch (Exception e) {
                System.out.println("fail to run GPS map from load file");
            }
            
        }
    }
}
