package org.example.service;

import org.example.model.WeatherData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeatherServiceTest {

    @Test
    void testGetWeather() {
        WeatherProvider service = new OpenWeatherMapService(); // Zmiana na OpenWeatherMapService
        try {
            WeatherData data = service.getWeather("London");
            assertNotNull(data);
            assertEquals("London", data.getLocation());
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}
