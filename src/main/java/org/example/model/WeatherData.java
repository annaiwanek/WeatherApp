package org.example.model;

public class WeatherData {
    private String location;
    private double temperature;
    private String description;
    private String icon;
    private double windSpeed;
    private double humidity;
    private double pressure;
    private String timestamp;

    public WeatherData(String location, double temperature, String description, String icon, double windSpeed, double humidity, double pressure, String timestamp) {
        this.location = location;
        this.temperature = temperature;
        this.description = description;
        this.icon = icon;
        this.windSpeed = windSpeed;
        this.humidity = humidity;
        this.pressure = pressure;
        this.timestamp = timestamp;
    }

    // Getters
    public String getLocation() { return location; }
    public double getTemperature() { return temperature; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public double getWindSpeed() { return windSpeed; }
    public double getHumidity() { return humidity; }
    public double getPressure() { return pressure; }
    public String getTimestamp() { return timestamp; }
}
