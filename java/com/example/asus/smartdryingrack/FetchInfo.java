package com.example.asus.smartdryingrack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

public class FetchInfo {

    private static final String OPEN_WEATHER_MAP_API = "http://api.openweathermap.org/data/2.5/weather?q=";
    private static final String params = "&units=metric&appid=2e7aa9c94b60174dea3edd971320caaf";

    private static final String HOME_INFO_API = "http://10.22.48.135:8080/";

    private static final String ON = "http://10.22.48.135:8080/on";
    private static final String OFF = "http://10.22.48.135:8080/off";


    private static String home_info = "{\"drying\": \"Y\", \"temperature\": 27, \"humidity\": 50, \"rainDrops\": \"N\"}";

    public static JSONObject getWeather(String city){
        try {
            URL url = new URL(OPEN_WEATHER_MAP_API + city + params);
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            // This value will be 404 if the request was not
            // successful
            if (data.getInt("cod") != 200) {
                return null;
            }

            return data;
        } catch(Exception e){
            return null;
        }
    }

    public static JSONObject getHomeInfo() {
        try {
            URL url = new URL(HOME_INFO_API);
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            return data;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void turnOn() {
        try {
            URL url = new URL(ON);
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();
            connection.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void turnOff() {
        try {
            URL url = new URL(OFF);
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();
            connection.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
