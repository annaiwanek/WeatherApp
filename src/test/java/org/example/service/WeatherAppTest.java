package org.example.service;

import org.example.ui.WeatherApp;
import org.junit.jupiter.api.*;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WeatherAppTest {

    private WeatherApp weatherApp;

    @BeforeAll
    public void setUp() {
        SwingUtilities.invokeLater(() -> weatherApp = new WeatherApp());
    }

    @Test
    public void testInitialUIComponents() {
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
        SwingUtilities.invokeLater(() -> {
            weatherApp.getLocationField().setText("Warsaw");
            weatherApp.getGetWeatherButton().doClick();
            assertEquals("Warsaw", weatherApp.getCurrentLocation());
        });
    }
}
