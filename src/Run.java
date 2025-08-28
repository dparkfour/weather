package com.example;
import javax.swing.SwingUtilities;

public class Run {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                new MainFrame().setVisible(true);
                //System.out.println(WeatherData.getLocation("Tokyo"));
               
                //System.out.println(WeatherData.getCurrentTime());
            }
        });
    }
       
   
   
}

