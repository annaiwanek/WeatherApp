package org.example.service;

import org.example.ui.WeatherWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.example.model.WeatherData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherWaypointTest {

    private WeatherWaypoint waypoint;

    @BeforeEach
    public void setUp() {
        WeatherData data = new WeatherData("TestLocation", 25.5, "Clear", "01d", 5.5, 60.0, 1013.0, "2024-06-23 12:00:00");
        GeoPosition position = new GeoPosition(52.2297, 21.0122);
        waypoint = new WeatherWaypoint(data, position);
    }

    @Test
    public void testGetWeatherData() {
        WeatherData data = waypoint.getWeatherData();
        assertNotNull(data);
        assertEquals("TestLocation", data.getLocation());
        assertEquals(25.5, data.getTemperature());
        assertEquals("Clear", data.getDescription());
        assertEquals("01d", data.getIcon());
        assertEquals(5.5, data.getWindSpeed());
        assertEquals(60.0, data.getHumidity());
        assertEquals(1013.0, data.getPressure());
        assertEquals("2024-06-23 12:00:00", data.getTimestamp());
    }

    @Test
    public void testGetPosition() {
        GeoPosition position = waypoint.getPosition();
        assertNotNull(position);
        assertEquals(52.2297, position.getLatitude());
        assertEquals(21.0122, position.getLongitude());
    }
}
