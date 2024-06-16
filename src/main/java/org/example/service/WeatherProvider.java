package org.example.service;

import org.example.model.WeatherData;

public interface WeatherProvider {
    WeatherData getWeather(String location) throws Exception;
}
