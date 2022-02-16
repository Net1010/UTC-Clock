/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utclock;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author nc184
 */
public class UTClock {
    static ClockPanel clock;
    static JFrame jf;
    public static void main(String[] args) {
        // TODO code application logic here
        jf = new JFrame("UT Clock");
        jf.setSize(500,500);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clock = new ClockPanel();
        jf.add(clock);
        
        ClockThread ct = new ClockThread();
        ct.start();
    }
    
}

class ClockPanel extends JPanel{
    int d;
    int cpX, cpY;   // center points
    int second, hour, minute;
    // End points of the clock hands (x,y) coordinates
    int secondEpX, secondEpY;
    int minuteEpX, minuteEpY;
    int hourEpX, hourEpY;
    ClockPanel(){
        super();
        second = 0; minute = 0; hour = 0;
    }
    
    protected void paintComponent(Graphics g){
        d = 200;
        cpX = (this.getSize().width)/2;
        cpY = (this.getSize().height)/2;
        
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(cpX - (d/2), cpY - (d/2), d , d);
        
        // Math
        // 6 degrees every second/minute
        secondEpX = (int)(Math.sin(Math.toRadians(second * 6)) * 80 + cpX); // 80 is the length of the hand (hypotenuse)
        secondEpY = (int)(Math.cos(Math.toRadians(second * 6)) * -1 * 80 + cpY);
        minuteEpX = (int)(Math.sin(Math.toRadians(minute * 6)) * 70 + cpX); // 70 is the length
        minuteEpY = (int)(Math.cos(Math.toRadians(minute * 6)) * -1 * 70 + cpY);
        hourEpX = (int)(Math.sin(Math.toRadians(hour * 30 + minute / 2)) * 50 + cpX); // 50 is the length
        hourEpY = (int)(Math.cos(Math.toRadians(hour * 30 + minute / 2)) * -1 * 50 + cpY);
        
        g.setColor(Color.red); 
        g.drawLine(cpX, cpY, secondEpX, secondEpY); 
        g.setColor(Color.black);
        g.drawLine(cpX, cpY, minuteEpX, minuteEpY); 
        g.drawLine(cpX, cpY, hourEpX, hourEpY); 
        
    }
}

// continues
class ClockThread extends Thread{
    int second, minute, hour;
    public void run(){
        while(true){
            getTime();
            update();
            UTClock.jf.setVisible(true);
            
            // waits at least 1 minute until checking NIST
            for (int i = 0; i < 60; i++){
                try {
                    this.sleep(1000);   // imprecise second
                    tick();
                    update();
                }
                catch(InterruptedException e){
                    System.out.println(e);
                }
            }
        }
    }
    
    public void getTime(){
        try{
            Socket s = new Socket("time-a-g.nist.gov", 13); // connects to time-a-g.nist.gov on port 13
            Scanner sin = new Scanner(s.getInputStream());
            String temp = sin.nextLine();
            String daytime = sin.nextLine();
            s.close();
            
            String time = daytime.split(" ")[2];
            String[] arrTime = time.split(":");
            second = Integer.valueOf(arrTime[2]);
            minute = Integer.valueOf(arrTime[1]);
            hour = Integer.valueOf(arrTime[0]);
            // convert to regular time
            if (hour == 0){
                hour = 12;
            }
            else if (hour > 12){
                hour -= 12;
            }
        }
        catch(IOException e){
            System.out.println("IOError: "+e.toString());
        }

    }
    
    public void update (){
        UTClock.clock.second = second;
        UTClock.clock.minute = minute;
        UTClock.clock.hour = hour;
        UTClock.clock.repaint();
    }
    
    // imprecise clock up
    public void tick(){
        if (second == 59){
            second = 0;
            minute++;
        }
        else {
            second++;
        }
        
        if (minute == 60){
            minute = 0;
            hour++;
        }
        
        if (hour == 13){
            hour = 1;
        }
        
    }
}
