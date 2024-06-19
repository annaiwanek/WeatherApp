package org.example.model;

public class WeatherData {
    private String location;
    private double temperature;
    private String description;
    private String icon;
    private double windSpeed;
    private double humidity; // Dodano pole humidity
    private double pressure; // Dodano pole pressure

    public WeatherData(String location, double temperature, String description, String icon, double windSpeed, double humidity, double pressure) {
        this.location = location;
        this.temperature = temperature;
        this.description = description;
        this.icon = icon;
        this.windSpeed = windSpeed;
        this.humidity = humidity; // Dodano pole humidity
        this.pressure = pressure; // Dodano pole pressure
    }

    // Gettery
    public String getLocation() { return location; }
    public double getTemperature() { return temperature; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public double getWindSpeed() { return windSpeed; }
    public double getHumidity() { return humidity; } // Dodano getter
    public double getPressure() { return pressure; } // Dodano getter
}
