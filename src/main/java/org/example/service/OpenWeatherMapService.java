package org.example.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.example.model.WeatherData;

import java.util.ArrayList;
import java.util.List;

public class OpenWeatherMapService implements WeatherProvider {
    private static final Logger logger = LogManager.getLogger(OpenWeatherMapService.class);
    private static final String API_KEY = "0c0db97fe14bea879471294b5774742f";
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=metric";
    private static final String CURRENT_API_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";

    @Override
    public WeatherData getCurrentWeather(String location) throws Exception {
        try {
            String urlString = String.format(CURRENT_API_URL, location, API_KEY);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                logger.info("Fetched current weather data for location: {}", location);
                return parseCurrentWeatherData(content.toString());
            } else {
                throw new Exception("Failed to get current weather data: " + responseCode);
            }
        } catch (Exception e) {
            logger.error("Error while fetching current weather data for location {}: {}", location, e.getMessage());
            throw new Exception("Error while fetching current weather data for location " + location + ": " + e.getMessage(), e);
        }
    }

    private WeatherData parseCurrentWeatherData(String json) throws Exception {
        try {
            JSONObject obj = new JSONObject(json);
            String location = obj.getString("name");
            double temperature = obj.getJSONObject("main").getDouble("temp");
            String description = obj.getJSONArray("weather").getJSONObject(0).getString("description");
            String icon = obj.getJSONArray("weather").getJSONObject(0).getString("icon");
            double windSpeed = obj.getJSONObject("wind").getDouble("speed");
            int humidity = obj.getJSONObject("main").getInt("humidity");
            int pressure = obj.getJSONObject("main").getInt("pressure");

            logger.info("Parsed current weather data for location: {}", location);
            return new WeatherData(location, temperature, description, icon, windSpeed, humidity, pressure, "N/A");
        } catch (Exception e) {
            logger.error("Error while parsing current weather data: {}", e.getMessage());
            throw new Exception("Error while parsing current weather data: " + e.getMessage(), e);
        }
    }

    @Override
    public List<WeatherData> getWeather(String location) throws Exception {
        try {
            String urlString = String.format(API_URL, location, API_KEY);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                logger.info("Fetched weather data for location: {}", location);
                return parseWeatherData(content.toString());
            } else {
                throw new Exception("Failed to get weather data: " + responseCode);
            }
        } catch (Exception e) {
            logger.error("Error while fetching weather data for location {}: {}", location, e.getMessage());
            throw new Exception("Error while fetching weather data for location " + location + ": " + e.getMessage(), e);
        }
    }

    private List<WeatherData> parseWeatherData(String json) throws Exception {
        try {
            List<WeatherData> weatherDataList = new ArrayList<>();
            JSONObject obj = new JSONObject(json);
            JSONArray list = obj.getJSONArray("list");
            String location = obj.getJSONObject("city").getString("name");

            for (int i = 0; i < list.length(); i++) {
                JSONObject entry = list.getJSONObject(i);
                double temperature = entry.getJSONObject("main").getDouble("temp");
                String description = entry.getJSONArray("weather").getJSONObject(0).getString("description");
                String icon = entry.getJSONArray("weather").getJSONObject(0).getString("icon");
                double windSpeed = entry.getJSONObject("wind").getDouble("speed");
                int humidity = entry.getJSONObject("main").getInt("humidity");
                int pressure = entry.getJSONObject("main").getInt("pressure");
                String timestamp = entry.getString("dt_txt");

                weatherDataList.add(new WeatherData(location, temperature, description, icon, windSpeed, humidity, pressure, timestamp));
            }
            logger.info("Parsed weather data for location: {}", location);
            return weatherDataList;
        } catch (Exception e) {
            logger.error("Error while parsing weather data: {}", e.getMessage());
            throw new Exception("Error while parsing weather data: " + e.getMessage(), e);
        }
    }
}
