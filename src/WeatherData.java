package com.example;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class WeatherData {
        //fetches all the data of weather based on location's longitude and latitude
    public static JSONObject getWeatherData(String location){
        JSONArray locationData = getLocation(location);
        if (locationData == null || locationData.isEmpty()) {
            System.out.println("Location data is null or empty.");
            return null;
        }
        //gets langitude and latitude from the geolocaiton api
        JSONObject locationdd = (JSONObject) locationData.get(0);
        double longitude =(double) locationdd.get("longitude");
        double latitude = (double)locationdd.get("latitude");
        
        return getWeatherDataFromCoordinates(latitude, longitude);
    }
    
    // New method to get weather data directly from coordinates
    public static JSONObject getWeatherDataFromCoordinates(double latitude, double longitude){
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude="+ latitude + "&longitude=" + longitude +
        "&hourly=temperature_2m,precipitation_probability,weather_code,wind_speed_10m&temperature_unit=fahrenheit&wind_speed_unit=mph&timezone=America%2FNew_York";

        try {
            //connects the API
            HttpURLConnection connect = fetchAPIResponse(urlString);
            // response code 200 means succesful
            if (connect.getResponseCode()!=200){
                System.out.println("ERROR");
                return null;
            }
                //stores API data
                StringBuilder data = new StringBuilder();
                Scanner scanner = new Scanner(connect.getInputStream());
                //goes through the api results and stores them into the stringbuilder
                while (scanner.hasNext()){
                    data.append(scanner.nextLine());
                }
                scanner.close();
                connect.disconnect();

                JSONParser parser = new JSONParser();
                JSONObject resultsW = (JSONObject) parser.parse(String.valueOf(data));
                JSONObject hourly = (JSONObject) resultsW.get("hourly");
                if (hourly == null) {
                    System.out.println("Hourly data is null.");
                    return null;
                }
                //we want to know the cindex of the current hour
                JSONArray time = (JSONArray) hourly.get("time");
                int index = getIndexofCurrentTime(time);
                //gets temperature data/degrees
                JSONArray tempData = (JSONArray) hourly.get("temperature_2m");
                if (tempData == null || index < 0 || index >= tempData.size()) {
                    System.out.println("Temperature data is null or index is out of bounds.");
                    return null;
                }
                double temp = (double) tempData.get(index);
                //gets weather code
                JSONArray weatherCodeData = (JSONArray) hourly.get("weather_code");
                if (weatherCodeData == null || index < 0 || index >= weatherCodeData.size()) {
                    System.out.println("Weather code data is null or index is out of bounds.");
                    return null;
                }
                String weatherCondition  = convertWeatherCode((long)weatherCodeData.get(index));
                //gets precipitation data/chances
                JSONArray precipData = (JSONArray) hourly.get("precipitation_probability");
                if (precipData == null || index < 0 || index >= precipData.size()) {
                    System.out.println("Precipitation data is null or index is out of bounds.");
                    return null;
                }
                long precip = (long) precipData.get(index);
                //gets windspeed data/mph
                JSONArray windSpeedData = (JSONArray) hourly.get("wind_speed_10m");
                if (windSpeedData == null || index < 0 || index >= windSpeedData.size()) {
                    System.out.println("Wind speed data is null or index is out of bounds.");
                    return null;
                }
                double windSpeed = (double) windSpeedData.get(index);
                //create object to store all weather data so that the frontend can use it
               
                JSONObject weatherData = new JSONObject();
                weatherData.put("temperature",temp);
                weatherData.put("weather_condition",weatherCondition);
                weatherData.put("precipitation",precip);
                weatherData.put("windspeed",windSpeed);
                return weatherData;
               
               
 

            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    //based off the location name typed in, we find the longitude and latitude
    public static JSONArray getLocation(String location){
        try {
            // Properly encode the location name for URL
            String encodedLocation = URLEncoder.encode(location, "UTF-8");
            // the url of the API - add country filter for better results
            String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + encodedLocation + "&count=10&language=en&format=json";
            System.out.println("Searching for location: " + location);
            System.out.println("Encoded URL: " + urlString);

            //calls for the API and connects
            HttpURLConnection connection = fetchAPIResponse(urlString);

            //test to see if connection is succesful
            if(connection.getResponseCode()!=200){
                System.out.println("Error: HTTP " + connection.getResponseCode());
                return null;
            }else{
                //store API results
                StringBuilder result = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                //goes through the API and stores the data into the stringbuilder
                while (scanner.hasNext()){
                    result.append(scanner.nextLine());
                }
               
                //close scanner
                scanner.close();
                //cut off connection
                connection.disconnect();
                
                //parse JSON String into a JSON Object
                JSONParser parser = new JSONParser();
                JSONObject resultsJSONobject = (JSONObject) parser.parse(String.valueOf(result));
                
                //get all the location data the API generated from the location name and store in array
                JSONArray locationData = (JSONArray) resultsJSONobject.get("results");
                
                if (locationData == null || locationData.isEmpty()) {
                    System.out.println("No results found for: " + location);
                    System.out.println("API Response: " + result.toString());
                } else {
                    System.out.println("Found " + locationData.size() + " results for: " + location);
                }
                
                return locationData;
            }

        }catch(Exception e){
            System.out.println("Exception in getLocation: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    // Enhanced location search with country/state support
    public static JSONArray getLocationWithCountry(String location, String country) {
        try {
            // Try different search strategies
            String[] searchQueries = {
                location,  // Just the city name
                location + ", " + country,  // City, Country
                location + " " + country   // City Country (no comma)
            };
            
            for (String searchQuery : searchQueries) {
                System.out.println("Trying search: " + searchQuery);
                
                // Properly encode the location name for URL
                String encodedLocation = URLEncoder.encode(searchQuery, "UTF-8");
                // the url of the API
                String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + encodedLocation + "&count=10&language=en&format=json";
                System.out.println("Encoded URL: " + urlString);

                //calls for the API and connects
                HttpURLConnection connection = fetchAPIResponse(urlString);

                //test to see if connection is succesful
                if(connection.getResponseCode()!=200){
                    System.out.println("Error: HTTP " + connection.getResponseCode());
                    continue;  // Try next search query
                }else{
                    //store API results
                    StringBuilder result = new StringBuilder();
                    Scanner scanner = new Scanner(connection.getInputStream());
                    //goes through the API and stores the data into the stringbuilder
                    while (scanner.hasNext()){
                        result.append(scanner.nextLine());
                    }
                   
                    //close scanner
                    scanner.close();
                    //cut off connection
                    connection.disconnect();
                    
                    //parse JSON String into a JSON Object
                    JSONParser parser = new JSONParser();
                    JSONObject resultsJSONobject = (JSONObject) parser.parse(String.valueOf(result));
                    
                    //get all the location data the API generated from the location name and store in array
                    JSONArray locationData = (JSONArray) resultsJSONobject.get("results");
                    
                    if (locationData == null || locationData.isEmpty()) {
                        System.out.println("No results found for: " + searchQuery);
                        System.out.println("API Response: " + result.toString());
                    } else {
                        System.out.println("Found " + locationData.size() + " results for: " + searchQuery);
                        return locationData;  // Return first successful result
                    }
                }
            }
            
            System.out.println("No results found with any search strategy");
            return null;
        } catch(Exception e){
            System.out.println("Exception in getLocationWithCountry: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    // Enhanced location search with just state/province
    public static JSONArray getLocationWithState(String city, String state) {
        try {
            System.out.println("Searching for city: " + city + " in state: " + state);
            
            // First, search for just the city name
            JSONArray allResults = getLocation(city);
            
            if (allResults == null || allResults.isEmpty()) {
                System.out.println("No results found for city: " + city);
                return null;
            }
            
            System.out.println("Found " + allResults.size() + " results for " + city + ", now filtering by state: " + state);
            
            // Filter results to find the one with the matching state
            for (int i = 0; i < allResults.size(); i++) {
                JSONObject location = (JSONObject) allResults.get(i);
                String admin1 = (String) location.get("admin1");
                
                if (admin1 != null && admin1.equalsIgnoreCase(state)) {
                    System.out.println("Found exact state match: " + admin1 + " for " + city);
                    
                    // Create a new array with just this matching result
                    JSONArray filteredResults = new JSONArray();
                    filteredResults.add(location);
                    return filteredResults;
                }
            }
            
            System.out.println("No exact state match found for " + city + " in " + state + ". Returning all results.");
            return allResults;
            
        } catch(Exception e){
            System.out.println("Exception in getLocationWithState: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    // Enhanced location search with state/province and country support
    public static JSONArray getLocationWithStateCountry(String city, String state, String country) {
        try {
            // Build the full location string
            StringBuilder fullLocation = new StringBuilder(city);
            if (state != null && !state.trim().isEmpty()) {
                fullLocation.append(", ").append(state);
            }
            if (country != null && !country.trim().isEmpty()) {
                fullLocation.append(", ").append(country);
            }
            
            String locationString = fullLocation.toString();
            
            // Properly encode the full location name for URL
            String encodedLocation = URLEncoder.encode(locationString, "UTF-8");
            // the url of the API with full location details
            String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + encodedLocation + "&count=10&language=en&format=json";
            System.out.println("Searching for location: " + locationString);
            System.out.println("Encoded URL: " + urlString);

            //calls for the API and connects
            HttpURLConnection connection = fetchAPIResponse(urlString);

            //test to see if connection is succesful
            if(connection.getResponseCode()!=200){
                System.out.println("Error: HTTP " + connection.getResponseCode());
                return null;
            }else{
                //store API results
                StringBuilder result = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                //goes through the API and stores the data into the stringbuilder
                while (scanner.hasNext()){
                    result.append(scanner.nextLine());
                }
               
                //close scanner
                scanner.close();
                //cut off connection
                connection.disconnect();
                
                //parse JSON String into a JSON Object
                JSONParser parser = new JSONParser();
                JSONObject resultsJSONobject = (JSONObject) parser.parse(String.valueOf(result));
                
                //get all the location data the API generated from the location name and store in array
                JSONArray locationData = (JSONArray) resultsJSONobject.get("results");
                
                if (locationData == null || locationData.isEmpty()) {
                    System.out.println("No results found for: " + locationString);
                    System.out.println("API Response: " + result.toString());
                } else {
                    System.out.println("Found " + locationData.size() + " results for: " + locationString);
                }
                
                return locationData;
            }

        }catch(Exception e){
            System.out.println("Exception in getLocationWithStateCountry: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
   
    private static HttpURLConnection fetchAPIResponse(String urlString){
        try{
           
            //creates a connection(attempts)
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //get location data from the API by requesting "get"
            conn.setRequestMethod("GET");
            //then connect to our API
            conn.connect();
            return conn;
        }catch(IOException e){
            e.printStackTrace();
        }
        //no connection found
        return null;
    }

    private static int getIndexofCurrentTime(JSONArray timeList){
        String currentTime = getCurrentTime();
        for ( int i = 0; i< timeList.size(); i++){
            String time = (String) timeList.get(i);
            if ( currentTime.contentEquals(time)){
                return i;
            }
        }

        return 0;
    }

    public static String getCurrentTime(){
        LocalDateTime currentTime = LocalDateTime.now();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        String formattedTime = currentTime.format(format);

        return formattedTime;
    }

    private static String convertWeatherCode(long weatherCode){
        String weatherCondition = "";
        if (weatherCode==0L){
            weatherCondition = "Clear";
        }else if(weatherCode <=3L && weatherCode>0L ){
            weatherCondition= "Cloudy";
        }else if((weatherCode >= 51L && weatherCode<=67L)
        ||(weatherCode >= 80L && weatherCode <=99L)){
            weatherCondition = "Rain";  
        }else if(weatherCode>= 71L && weatherCode<= 77L ) {
            weatherCondition = "Snow";
        }
        return weatherCondition;
    }

}
