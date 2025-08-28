package com.example;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class MainFrame extends JFrame{
    private JSONObject weatherData;

    public MainFrame(){
        super("Weather App"); //title
        setSize(420,650); //creates the dimension of the frame
        setLocationRelativeTo(null);
        setLayout(null);
        ImageIcon icon = new ImageIcon("bin\\image.png"); //create an image icon
        setIconImage(icon.getImage()); //change icon of frame
        addGuiComponents();
       
    }
    private void addGuiComponents(){
        //Textbox to put in a locaiton
        JTextField location = new JTextField();
        location.setBounds(5, 300, 351,45);
        location.setFont(new Font("Garamond",Font.BOLD,25));
        add(location);
        //Search for a city
        JLabel search = new JLabel(); //creates label
        search.setText("Search a city or airport"); //sets text
        search.setBounds(85,335,250,45);
        search.setFont(new Font("Garamond",Font.BOLD,25));;
        add(search);
        //Weather icon
        JLabel weatherIcon = new JLabel(loadImage("bin\\weather.png",250,250));
        weatherIcon.setBounds(-45,-125,500,500);
        add(weatherIcon);
        //Temperature
        JLabel temp = new JLabel("0 F");
        temp.setBounds(157,380,450,50);
        temp.setFont(new Font("Dialog",Font.BOLD,50));
        add(temp);
        //weather description
        JLabel weatherDesc = new JLabel("Sunny");
        weatherDesc.setBounds(150,250,450,50);
        weatherDesc.setFont(new Font("Dialog",Font.BOLD,35));
        add(weatherDesc);
        //precipitation icon
        JLabel pIcon = new JLabel(loadImage("bin\\precip.png", 60, 60));
        pIcon.setBounds(40,425,60,60);
        add(pIcon);
        //precipitation txt
        JLabel pTxt = new JLabel("Precipitation");
        pTxt.setBounds(27,470,450,50);
        pTxt.setFont(new Font("Dialog",Font.BOLD,15));
        add(pTxt);
        //precip status
        JLabel pStatus = new JLabel("10%");
        pStatus.setBounds(35,495,450,50);
        pStatus.setFont(new Font("Dialog",Font.PLAIN,35));
        add(pStatus);
        //wind speed icon
        JLabel wIcon = new JLabel(loadImage("bin\\wind.png", 70, 70));
        wIcon.setBounds(295,420,70,70);
        add(wIcon);
        //wind speed txt
        JLabel wTxt = new JLabel("Windspeed");
        wTxt.setBounds(288,470,450,50);
        wTxt.setFont(new Font("Dialog",Font.BOLD,15));
        add(wTxt);
        //windspeed mph
        JLabel mph = new JLabel("10 mph");
        mph.setBounds(265, 495, 450,50);
        mph.setFont(new Font("Dialog",Font.PLAIN,35));
        add(mph);
        //Search Button
        JButton button = new JButton(loadImage("bin\\serach.jpg",50,45));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBounds(355,300,50,45);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                //get location from user from the search bar
                String userLocation = location.getText();
                //remove white spaces from the text
                if( userLocation.replaceAll("\\s","").length()<= 0 ){
                    return;
                }
                
                // Try to extract city, state, and country from user input
                String[] parts = userLocation.split(",");
                String city = parts[0].trim();
                String state = null;
                String country = null;
                
                if (parts.length >= 2) {
                    state = parts[1].trim();
                }
                if (parts.length >= 3) {
                    country = parts[2].trim();
                }
                
                // Try different search strategies
                JSONArray locationData = null;
                
                // First try the enhanced search with country/state
                if (country != null && state != null) {
                    locationData = WeatherData.getLocationWithStateCountry(city, state, country);
                } else if (country != null) {
                    locationData = WeatherData.getLocationWithCountry(city, country);
                } else if (state != null) {
                    // Try searching with just city and state
                    locationData = WeatherData.getLocationWithState(city, state);
                }
                
                // If enhanced search fails, fall back to basic search
                if (locationData == null || locationData.isEmpty()) {
                    locationData = WeatherData.getLocation(city);
                }
                
                // If we still don't have location data, show error and return
                if (locationData == null || locationData.isEmpty()) {
                    System.out.println("Could not find location: " + userLocation);
                    // You could add a JOptionPane here to show error to user
                    return;
                }
                
                // Use the first result to get weather data
                JSONObject firstLocation = (JSONObject) locationData.get(0);
                double longitude = (double) firstLocation.get("longitude");
                double latitude = (double) firstLocation.get("latitude");
                
                // Now get weather data using coordinates
                System.out.println("Getting weather data for coordinates: " + latitude + ", " + longitude);
                weatherData = WeatherData.getWeatherDataFromCoordinates(latitude, longitude);
                if (weatherData == null) {
                    System.out.println("Weather data is null.");
                    return;  // Handle the null case appropriately
                }
                
                System.out.println("Weather data retrieved successfully:");
                System.out.println("Temperature: " + weatherData.get("temperature"));
                System.out.println("Condition: " + weatherData.get("weather_condition"));
                System.out.println("Precipitation: " + weatherData.get("precipitation"));
                System.out.println("Wind Speed: " + weatherData.get("windspeed"));

                //update gui

                //update weather image
                String weatherCondition = (String) weatherData.get("weather_condition");
                System.out.println("Weather condition: " + weatherCondition);

                // Try to load weather-specific image, fall back to default if not found
                ImageIcon weatherImage = null;
                switch(weatherCondition){
                    case "Clear":
                        weatherImage = loadImage("bin\\clear.png",250,250);
                        break;
                    case "Cloudy":
                        weatherImage = loadImage("bin\\cloudy.png",250,250);
                        break;
                    case "Rain":
                        weatherImage = loadImage("bin\\rain.png",250,250);
                        break;
                    case "Snow":
                        weatherImage = loadImage("bin\\snow.png",250,250);
                        break;
                }
                
                // If weather-specific image not found, use default weather image
                if (weatherImage != null) {
                    weatherIcon.setIcon(weatherImage);
                } else {
                    System.out.println("Using default weather image for: " + weatherCondition);
                    // Load the default weather image from bin folder
                    weatherImage = loadImage("bin\\weather.png",250,250);
                    if (weatherImage != null) {
                        weatherIcon.setIcon(weatherImage);
                    }
                }
                double tempData = (double) weatherData.get("temperature");
                temp.setText(tempData + " F");
                weatherDesc.setText(weatherCondition);
                long precipitation = (long)weatherData.get("precipitation");
                pStatus.setText(precipitation+ "%");

                double windSpeedData = (double)weatherData.get("windspeed");
                mph.setText(windSpeedData+ "mph");      
               
            }
        });
        add(button);


    }
    private ImageIcon loadImage(String URL,int width, int height){
        try{
            BufferedImage image = ImageIO.read(new File(URL));
            Image resizedImage = image.getScaledInstance(width,height,1);
            return new ImageIcon(resizedImage);
        }catch(IOException e ){
            e.printStackTrace();
        }
        System.out.println("Could not find resource");
        return null;

    }


}

