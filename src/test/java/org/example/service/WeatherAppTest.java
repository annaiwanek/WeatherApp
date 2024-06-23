package org.example.service;

import org.example.ui.WeatherApp;
import org.junit.jupiter.api.*;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WeatherAppTest {

    private WeatherApp weatherApp;

    @Test
    public void testInitialUIComponents() {
        WeatherApp weatherApp = new WeatherApp();
        assertNotNull(weatherApp);
        assertNotNull(weatherApp.getLocationField());
        assertNotNull(weatherApp.getGetWeatherButton());
        assertNotNull(weatherApp.getUnitsComboBox());
        assertNotNull(weatherApp.getLocationLabel());
        assertNotNull(weatherApp.getTemperatureLabel());
        assertNotNull(weatherApp.getDescriptionLabel());
        assertNotNull(weatherApp.getIconLabel());
    }

    @Test
    public void testGetWeatherButtonAction() {
        WeatherApp weatherApp = new WeatherApp();
        weatherApp.getLocationField().setText("Warsaw");
        weatherApp.getGetWeatherButton().doClick();
        assertEquals("Warsaw", weatherApp.getCurrentLocation());
    }
}
