package org.example.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;
import org.example.model.WeatherData;

public class WeatherService implements WeatherProvider {
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(WeatherService.class);
    private static final String API_KEY = "0c0db97fe14bea879471294b5774742f"; // Wstaw swój klucz API
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";

    @Override
    public WeatherData getWeather(String location) throws Exception {
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
            logger.error("Error while fetching weather data: {}", e.getMessage());
            throw new Exception("Error while fetching weather data", e);
        }
    }

    private WeatherData parseWeatherData(String json) throws Exception {
        try {
            JSONObject obj = new JSONObject(json);
            String location = obj.getString("name");
            double temperature = obj.getJSONObject("main").getDouble("temp");
            String description = obj.getJSONArray("weather").getJSONObject(0).getString("description");
            String icon = obj.getJSONArray("weather").getJSONObject(0).getString("icon");

            logger.info("Parsed weather data for location: {}", location);
            return new WeatherData(location, temperature, description, icon);
        } catch (Exception e) {
            logger.error("Error while parsing weather data: {}", e.getMessage());
            throw new Exception("Error while parsing weather data", e);
        }
    }
}
