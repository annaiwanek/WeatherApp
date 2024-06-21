package org.example.ui;

import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.example.model.WeatherData;

public class WeatherWaypoint implements Waypoint {
    private final WeatherData weatherData;
    private final GeoPosition position;

    public WeatherWaypoint(WeatherData weatherData, GeoPosition position) {
        this.weatherData = weatherData;
        this.position = position;
    }

    public WeatherData getWeatherData() {
        return weatherData;
    }

    @Override
    public GeoPosition getPosition() {
        return position;
    }
}

