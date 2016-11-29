/*
 * license: standard library, none
 * this file is used to create a gui and display
 * digit number and analogy signal, also communicate with IO file*/

package main_app;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.*;
import java.util.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.*;
import java.text.SimpleDateFormat;
import java.lang.Math;

public class GUI extends JFrame implements MouseWheelListener, MouseListener {
    public int[] lineData = new int[30];
    public int[] barData = new int[10];
    public float[] currentValue = new float[3];
    public ArrayList<Float> temp = new ArrayList();
    public ArrayList<Float> wind = new ArrayList();
    public ArrayList<Float> sun = new ArrayList();
    public ArrayList<String> tim = new ArrayList();
    
    public ArrayList<Float> tempDis = new ArrayList();
    public ArrayList<Float> windDis = new ArrayList();
    public ArrayList<Float> sunDis = new ArrayList();
    
    public ArrayList<Float> tempPoints = new ArrayList();
    public ArrayList<Float> windPoints = new ArrayList();
    public ArrayList<Float> sunPoints = new ArrayList();
    public DecimalFormat oneDec = new DecimalFormat("##.#");
    public draw paper;
    /*digit:1; bar chat:2; strip: 3*/
    private int graphType = 1; 
    private Graphics g;
    private int i;
    private int index = 0;
    private int indexIO = 1;
    private float hight;
    private String[] comboString = {"x1","x5", "x10"};
    private ImageIcon icon;
    private int googleRun = 0;
    private int wheel = 800;
    private JPanel  screen = new JPanel ();
    private JFrame window = new JFrame();
    private JScrollPane scpanel = new JScrollPane(screen);
    private JComboBox menu;
    private int[] linePosition = new int[4];
    private int[] labelPosition = new int[3];
    private DateFormat dateFormat = new SimpleDateFormat("ddMMyy.HHmmss");
    
    // create gui
    public GUI() {
        
        super("ENGG2800: weather station");
        //data1 = data2;
        graphType = 1;
        
        //clean the array
        for (i = 0; i < 10; i++) {
            barData[i] = 0;
        }
        barData[0] = 40;
        barData[1] = 80;
        barData[4] = 380;
        barData[7] = 680;
        
        //init the frame Jpanel
        window.setSize(850,500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel buttonLine = new JPanel(); 
        buttonLine.setSize(800,50);
        buttonLine.setBackground(Color.yellow);
        buttonLine.setLayout(new GridLayout(1,6));
        scpanel.setWheelScrollingEnabled(false); 
        
       
        screen.setPreferredSize(new Dimension(840, 400));
        screen.setBackground(Color.gray);
        screen.setLayout(new BorderLayout());
        
        paper = new draw();
        screen.add(paper);
        
        //add mouse listener
        screen.addMouseListener(new MouseAdapter() { 
            
            // add mouse press listener
            public void mousePressed(MouseEvent event) { 

                if (graphType == 3) {

                    linePosition[0] = event.getX();
                    linePosition[2] = event.getX();
                    linePosition[1] = 0;
                    linePosition[3] = 420;
                    
                    labelPosition[0] = 1;
                    labelPosition[1] = event.getX();
                    labelPosition[2] = event.getY();
                    
                    paper.repaint();
                }
            } 
            
            // add mouse release listener
            public void mouseReleased(MouseEvent event) {
                if (graphType == 3) {

                    linePosition[0] = 0;
                    linePosition[2] = 0;
                    linePosition[1] = 0;
                    linePosition[3] = 0;
                    
                    labelPosition[0] = 0;
                    paper.repaint();
                }
            }
          
        });
        window.addMouseWheelListener(this);
        
        //add button listener
        JButton digit = new JButton("digit");
        buttonLine.add(digit);
        digit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        screen.setPreferredSize(new Dimension(840, 400));
                        changeScreen(screen);
                        graphType = 1;
                        paper.repaint();
                        SwingUtilities.updateComponentTreeUI(window);
                    }
                }
        );
        
        //add button listener
        JButton bar = new JButton("bar chart");
        buttonLine.add(bar);
        bar.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        screen.setPreferredSize(new Dimension(840, 400));
                        changeScreen(screen);
                        graphType = 2;
                        paper.repaint();
                        SwingUtilities.updateComponentTreeUI(window);
                    }
                }
        );
        
        //add button listener
        JButton strip = new JButton("strip chart");
        buttonLine.add(strip);
        strip.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        screen.setPreferredSize(new Dimension(wheel + 40, 400));
                        changeScreen(screen);
                        graphType = 3;
                        paper.repaint(); 
                        SwingUtilities.updateComponentTreeUI(window);
                    }
                }
        );
        
        //add button listener
        JButton map = new JButton("map");
        buttonLine.add(map);
        map.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {                                               
                        MainApp.communicator.writeData(115);                        
                    }
                }
        );
        
        menu = new JComboBox(comboString);
        buttonLine.add(menu);
        
        //add button listener
        JButton load = new JButton("load");
        buttonLine.add(load);
        load.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {                        
                        int time = 1;     
                        
                        //load file, clean all the buffer
                        index = 0;
                        temp.clear();
                        wind.clear();
                        sun.clear();
                        tim.clear();
                        
                        tempDis.clear();
                        windDis.clear();
                        sunDis.clear();
                        
                        tempPoints.clear();
                        windPoints.clear();
                        sunPoints.clear();
                        
                        LoadFIle f = new LoadFIle();
                        f.openF("load_file.csv");
                    
                    }
                }
        );
        
        //save the file
        JButton save = new JButton("save");
        buttonLine.add(save);
        save.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        //try to open a file to save data
                        try {
                            PrintWriter outf = new PrintWriter("save_file.csv", "UTF-8");
                            for (int i = 0; i < temp.size(); i++) {
                                outf.print(tim.get(i) + "," + oneDec.format(wind.get(i)) + "," + Math.round(temp.get(i)) + "," + Math.round(sun.get(i)) + "\n");                           
                            }
                            
                            if (IO.lat.size() > 0) {
                                for (int i = 0; i < IO.lat.size(); i++) {
                                    outf.print(IO.mapTimeIO.get(i) + "," + oneDec.format(IO.mapW.get(i)) + "," + Math.round(IO.mapT.get(i)) + "," + Math.round(IO.mapSIO.get(i)) + "," 
                                            + (IO.lat.get(i) > 0 ? "N." + IO.lat.get(i) : "S." + Math.abs(IO.lat.get(i))) + ","
                                            + (IO.lon.get(i) > 0 ? "E." + IO.lon.get(i) : "W." + Math.abs(IO.lon.get(i))) + "\n");                            
                                }
                            }
                            
                            outf.close();
                        } catch(Exception e) {
                            System.out.println("I fail to open a file to save the file");
                        }
                        
                        
                        
                    }
                }
        );
        //config the gui
        window.add(scpanel, BorderLayout.CENTER);
        window.add(buttonLine, BorderLayout.SOUTH);
        window.setVisible(true);
        this.pack();
        
    }
    
    //chage the screen size
    public void changeScreen(JPanel  s) {
        if (googleRun == 1) {
            s.removeAll();
            s.add(paper);
            googleRun = 0;
        }
    }
    
    //receive from firmware
    public void update2 (String temp1) {
        
        float t = 0;
        float w = 0;
        float s = 0;
        
        float tAv = 0;
        float wAv = 0;
        float sAv = 0;
        String time;
        Scanner x = new Scanner(temp1.replaceAll("[^-.0-9]", " "));
 
        t = x.nextFloat();
        w = x.nextFloat();
        s = x.nextFloat();
        time = dateFormat.format(new Date());
        
        //for first one
        if (tim.size() == 0) {
            temp.add(t);
            wind.add(w);
            sun.add(s);
            tim.add(time);
            indexIO++;
            //calculate the average value
        } else if ((tim.get(tim.size() - 1).equals(time)) && (tim.size() != 0)) {
            tAv = (temp.get(temp.size() - 1) * indexIO + t) / (float)(indexIO + 1);
            temp.remove(temp.size() - 1);
            temp.add(tAv);
            
            tAv = (wind.get(wind.size() - 1) * indexIO + w) / (float)(indexIO + 1);
            wind.remove(wind.size() - 1);
            wind.add(tAv);
            
            tAv = (sun.get(sun.size() - 1) * indexIO + s) / (float)(indexIO + 1);
            sun.remove(sun.size() - 1);
            sun.add(tAv);
            indexIO++;
            
        } else {
            indexIO = 1;
            this.temp.add(t);
            wind.add((float)(w / 10.0));
            sun.add(s);
            tim.add(time);
        }
        
    }
    

    //return sppend of loading
    public int getSpeed() {
        if (menu.getSelectedIndex() == 1) {
            return 200;
        } else if (menu.getSelectedIndex() == 2) {
            return 100;
        } else {
            return 1000;
        }
    }
    
    /*load data*/
    public void update3 (float t, float w, float s) {
        temp.add(t);
        wind.add(w);
        sun.add(s);
    
    }
    
    //update time info
    public void updateTim (String time) {
        tim.add(time);
    }
    
    /*update data to display*/
    public void updateData() {
        float t = 0;
        float w = 0;
        float s = 0;
        float intervalT, intervalW, intervalS;
        int t1, t2, timeCoefficient;
        hight = 440;

        if (index < (temp.size())) {
            //data for mouse listener
            if (index == 0) {
                tempPoints.add(temp.get(index));
                windPoints.add(wind.get(index));
                sunPoints.add(sun.get(index));
            } else {
                t1 = Integer.parseInt(tim.get(index - 1).substring(7));
                t2 = Integer.parseInt(tim.get(index).substring(7));       
                timeCoefficient = (t2 % 100 - t1 % 100) + 60 * (t2 / 100 % 100 - t1 / 100 % 100) + 3600 * (t2 / 10000 - t1 / 10000);
                //calculate the interval
                intervalT = (temp.get(index) - temp.get(index - 1)) / timeCoefficient;
                intervalW = (wind.get(index) - wind.get(index - 1)) / timeCoefficient;
                intervalS = (sun.get(index) - sun.get(index - 1)) / timeCoefficient;
                
                for (int i = 0; i < timeCoefficient; i++) {
                    tempPoints.add(temp.get(index - 1) + (i + 1) * intervalT);
                    windPoints.add(wind.get(index - 1) + (i + 1) * intervalW);
                    sunPoints.add(sun.get(index - 1) + (i + 1) * intervalS);
                }
            }
            
            // for bar chart        
            t = (float)((temp.get(index) + 20) * 1.2);
            w = wind.get(index) * 5;
            s = (sun.get(index) / 50);

            if (t > 100) {
                t = 100;
            } else if (w > 100) {
                w = 100;
            } else if (s > 100) {
                s = 100;
            }
            currentValue[0] = temp.get(index);
            currentValue[1] = wind.get(index);        
            currentValue[2] = sun.get(index);

            if (tempDis.size() >= 3600) {
                return;
            }
            
            tempDis.add((float)(hight - t*3.0));
            
            barData[3] = (int)(t * 3);
            barData[2] = (int)(hight - barData[3]);

            windDis.add((float)(hight - w*3.0));
            
            barData[6] = (int)(w * 3);
            barData[5] = (int)(hight - barData[6]);

            sunDis.add((float)(hight - s*3.0));

            barData[8] = (int)(hight - s*3.0);
            barData[9] = (int)(s * 3);

            paper.repaint();
            index++;
        }
        
    }
    
    //draw digit analogy on gui
    private class draw extends JPanel{
        
      //set size and init
        public draw() {
            setPreferredSize(new Dimension(800,440));
        }
        
        //draw digit analogy on gui
        public void paint(Graphics g) {
            int c = 0;
            double x = 0;
            int t1, t2;
            int timeCoefficient = 0;
            
            super.paint(g);
            
            this.setBackground(Color.white);
            

            g.clearRect(0, 0, getWidth(), getHeight());
            if (graphType == 1) {
                //digit display
                g.setColor(Color.black);
                g.drawString("temperature: " + (int)currentValue[0],350,100);
                
                g.setColor(Color.blue);
                g.drawString("wind speed: " + oneDec.format(currentValue[1]),350,220);
                
                g.setColor(Color.red);
                g.drawString("sunshine: " + (int)currentValue[2],350,350);
                
            } else if (graphType == 2) {
              //bar chat
                g.setColor(Color.black);
                g.fillRect(barData[1], barData[2], barData[0], barData[3]);
                
                g.setColor(Color.black);
                g.drawString("temperature: "  + (int)currentValue[0],80,100);
                
                g.setColor(Color.blue);
                g.fillRect(barData[4], barData[5], barData[0], barData[6]);
                
                g.setColor(Color.blue);
                g.drawString("wind speed: "  + oneDec.format(currentValue[1]),400,100);

                g.setColor(Color.red);
                g.fillRect(barData[7], barData[8], barData[0], barData[9]);
                g.setColor(Color.red);
                g.drawString("sun shine: " + (int)currentValue[2],680,100);
                
            } else if (graphType == 3) {
                //strip chat
                x = 0;
                c = tempDis.size() - 1;
                if (c > 3599) {
                    c = 3599;
                } 
                double interval = wheel / 3601.0;
                for (int i = 0; i < c; i++) {
                    g.setColor(Color.black);
                    t1 = Integer.parseInt(tim.get(i).substring(7));
                    t2 = Integer.parseInt(tim.get(i + 1).substring(7));
                    
                    timeCoefficient = (t2 % 100 - t1 % 100) + 60 * (t2 / 100 % 100 - t1 / 100 % 100) + 3600 * (t2 / 10000 - t1 / 10000);
                    
                    g.drawLine((int)(x + 100), tempDis.get(i).intValue(), (int)(x + interval * timeCoefficient + 100), tempDis.get(i + 1).intValue());
                   
                    g.setColor(Color.blue);
                    g.drawLine((int)(x + 100), windDis.get(i).intValue(), (int)(x + interval * timeCoefficient + 100), windDis.get(i + 1).intValue());
                  
                    g.setColor(Color.red);
                    g.drawLine((int)(x + 100), sunDis.get(i).intValue(), (int)(x + interval * timeCoefficient + 100), sunDis.get(i + 1).intValue());
                    x += interval * timeCoefficient;
                }
                //draw a line when mouse click
                if (labelPosition[0] == 1) {
                    g.setColor(Color.black);
                    g.drawLine(linePosition[0], linePosition[1], linePosition[2], linePosition[3]);
                    
                    int labelIndex = (int)((labelPosition[1] - 100) / interval);
                    float tl = 0, wl = 0, sl = 0;
                    
                    if ((labelIndex < tempPoints.size()) && (labelIndex >= 0)) {
                        tl = tempPoints.get(labelIndex);
                        wl = windPoints.get(labelIndex);
                        sl = sunPoints.get(labelIndex);
                    }
                    

                    g.drawString("Time: "+ (labelIndex / 60) + "m" + (labelIndex % 60)+"s; T: " + tl + "; W: " + wl + "; S: " + sl, labelPosition[1] + 10, labelPosition[2] - 10);
                }
                
                int yInterval = 30;
                
                // create Y axis
                g.setColor(Color.black);
                g.drawLine(10, 30, 10, 440);
                for (int i = 0; i < 11; i++) {
                    g.setColor(Color.black);
                    g.drawString("-" + ((i * 8) - 20),10, 440 - i * yInterval);
                    
                }
                g.drawString("C",12, 100);
                
                g.setColor(Color.blue);
                g.drawLine(30, 30, 30, 440);
                for (int i = 0; i < 11; i++) {
                    g.setColor(Color.blue);
                    g.drawString("-" + ((i * 3)),30, 440 - i * yInterval);
                    
                }
                g.drawString("m/s",32, 100);
                
                g.setColor(Color.red);
                g.drawLine(50, 30, 50, 440);
                for (int i = 0; i < 11; i++) {
                    g.setColor(Color.red);
                    g.drawString("-" + ((i * 500)),50, 440 - i * yInterval);
                    
                }
                g.drawString("w/m^2", 55, 100);
                
                double timeInterval = 0;
                
                if (wheel <= 4000) {
                    for (int i = 0; i < 7; i++) {
                        g.setColor(Color.black);
                        g.drawString(i + "0 min",(int)(timeInterval * 600 + 100),20);
                        timeInterval += wheel/3601.0;
                    }
                } else if (wheel > 4000) {
                    for (int i = 0; i < 61; i++) {
                        g.setColor(Color.black);
                        g.drawString(i + " min",(int)(timeInterval * 60 + 100),20);
                        timeInterval += wheel/3601.0;
                    }
                                   
                }
                if (wheel >= 8000) {
                    timeInterval = 0;
                    for (int i = 0; i < 301; i++) {
                        g.setColor(Color.black);
                        g.drawString("|",(int)(timeInterval * 12 + 100),5);
                        timeInterval += wheel/3601.0;
                    }

                }
            }  
        }
    }
    
@Override
        // override the mouse listener
        public void mouseWheelMoved(MouseWheelEvent event) {
            int buffer;
            if (graphType == 3) {
            
                wheel = wheel + event.getWheelRotation() * 400;
                if (wheel > 24800) {
                    wheel = 24800;
                }

                if (wheel < 800) {
                    wheel = 800;
                }
                screen.setPreferredSize(new Dimension(wheel + 140, 400));
                SwingUtilities.updateComponentTreeUI(window);
            }
        }

@Override        
        public void mousePressed(MouseEvent event) {
            
        }

@Override        
        public void mouseReleased(MouseEvent event) {
            
        }
        
@Override
        public void mouseExited(MouseEvent event) {
            
            
         }
        
@Override
        public void mouseEntered(MouseEvent event) {
            
            
         }
        
@Override
        public void mouseClicked(MouseEvent e) {
            
           
         }
    
}

