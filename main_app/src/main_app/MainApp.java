/*
 * license: standard library, none
 * this file is used to start the app
 */

package main_app;

import java.awt.FlowLayout;
import java.awt.event.AdjustmentListener;
import java.awt.event.ActionEvent;
import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import javax.swing.*;

/*main class whiich is used to start the program*/
public class MainApp {
    public static GUI ui= new GUI();;
    public static IO communicator = new IO();
    
    /*main method whiich is used to start the program
    parameter: none
    return: none    */
    public static void main (String args[])throws IOException {
        float[][] data;
        int ifRun = 1;
        int delayTime = 0;    
        
        
        //connect with software
        communicator.searchForPorts();
        communicator.connect();
        //if it has connected
        if (communicator.getConnected() == true)
        {
            if (communicator.initIOStream() == true)
            {
                communicator.initListener();
            }
        }
        
        // timmer
        while (ifRun == 1) {
            delayTime = ui.getSpeed();
            
            java.awt.Window win[] = java.awt.Window.getWindows();  
            
            /*check if the gui is closed*/
            if (win[0].getName().equals("frame0")) {
                try {
                    Thread.sleep(delayTime);                 

                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                
                ui.updateData();
            } else {
                ifRun = 0;
            }
        
        }
         
        
        

    }
    
}
