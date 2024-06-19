package org.example.service;

import org.example.model.WeatherData;
import java.util.List;

public interface WeatherProvider {
    WeatherData getCurrentWeather(String location) throws Exception; // Metoda zwracająca bieżącą pogodę
    List<WeatherData> getWeather(String location) throws Exception; // Metoda zwracająca listę pogody
}
