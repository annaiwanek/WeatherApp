package org.example.service;

import org.example.model.WeatherData;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class OpenWeatherMapServiceTest {

    private WeatherProvider weatherProvider;

    @BeforeEach
    public void setUp() {
        weatherProvider = new OpenWeatherMapService(true); // Metric units
    }

    @Test
    public void testGetCurrentWeather() throws Exception {
        WeatherData data = weatherProvider.getCurrentWeather("Warsaw");

        assertNotNull(data);
        assertEquals("Warsaw", data.getLocation());
        assertTrue(data.getTemperature() >= -50 && data.getTemperature() <= 50);
        assertNotNull(data.getDescription());
        assertNotNull(data.getIcon());
        assertTrue(data.getWindSpeed() >= 0);
        assertTrue(data.getHumidity() >= 0 && data.getHumidity() <= 100);
        assertTrue(data.getPressure() > 800 && data.getPressure() < 1200);
    }

    @Test
    public void testGetWeather() throws Exception {
        List<WeatherData> dataList = weatherProvider.getWeather("Warsaw");

        assertNotNull(dataList);
        assertFalse(dataList.isEmpty());

        for (WeatherData data : dataList) {
            assertEquals("Warsaw", data.getLocation());
            assertTrue(data.getTemperature() >= -50 && data.getTemperature() <= 50);
            assertNotNull(data.getDescription());
            assertNotNull(data.getIcon());
            assertTrue(data.getWindSpeed() >= 0);
            assertTrue(data.getHumidity() >= 0 && data.getHumidity() <= 100);
            assertTrue(data.getPressure() > 800 && data.getPressure() < 1200);
        }
    }
}
