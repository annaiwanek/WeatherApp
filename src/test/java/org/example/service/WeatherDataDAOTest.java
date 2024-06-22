package org.example.service;

import org.example.db.WeatherDataDAO;
import org.example.model.WeatherData;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WeatherDataDAOTest {

    private WeatherDataDAO weatherDataDAO;

    @BeforeAll
    public void setUp() {
        weatherDataDAO = new WeatherDataDAO();
        weatherDataDAO.clearWeatherData();
    }

    @Test
    public void testSaveWeatherData() {
        WeatherData data = new WeatherData("TestLocation", 25.5, "Clear", "01d", 5.5, 60.0, 1013.0, "2024-06-23 12:00:00");
        weatherDataDAO.saveWeatherData(data);

        List<WeatherData> allWeatherData = weatherDataDAO.getAllWeatherData();
        assertEquals(1, allWeatherData.size());

        WeatherData retrievedData = allWeatherData.get(0);
        assertEquals("TestLocation", retrievedData.getLocation());
        assertEquals(25.5, retrievedData.getTemperature());
        assertEquals("Clear", retrievedData.getDescription());
        assertEquals("01d", retrievedData.getIcon());
        assertEquals(5.5, retrievedData.getWindSpeed());
        assertEquals(60.0, retrievedData.getHumidity());
        assertEquals(1013.0, retrievedData.getPressure());
        assertEquals("2024-06-23 12:00:00", retrievedData.getTimestamp());
    }

    @Test
    public void testClearWeatherData() {
        weatherDataDAO.clearWeatherData();
        List<WeatherData> allWeatherData = weatherDataDAO.getAllWeatherData();
        assertTrue(allWeatherData.isEmpty());
    }

    @Test
    public void testSaveAndRetrieveWeatherData() {
        WeatherData data = new WeatherData("TestLocation", 25.5, "Clear", "01d", 5.5, 60.0, 1013.0, "2024-06-23 12:00:00");
        weatherDataDAO.saveWeatherData(data);

        List<WeatherData> allWeatherData = weatherDataDAO.getAllWeatherData();
        assertEquals(1, allWeatherData.size());

        WeatherData retrievedData = allWeatherData.get(0);
        assertEquals("TestLocation", retrievedData.getLocation());
        assertEquals(25.5, retrievedData.getTemperature());
        assertEquals("Clear", retrievedData.getDescription());
        assertEquals("01d", retrievedData.getIcon());
        assertEquals(5.5, retrievedData.getWindSpeed());
        assertEquals(60.0, retrievedData.getHumidity());
        assertEquals(1013.0, retrievedData.getPressure());
        assertEquals("2024-06-23 12:00:00", retrievedData.getTimestamp());
    }


}
