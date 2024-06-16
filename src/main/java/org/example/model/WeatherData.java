package org.example.model;

public class WeatherData {
    private String location;
    private double temperature;
    private String description;
    private String icon;

    public WeatherData(String location, double temperature, String description, String icon) {
        this.location = location;
        this.temperature = temperature;
        this.description = description;
        this.icon = icon;
    }

    // Getters
    public String getLocation() {
        return location;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
}
