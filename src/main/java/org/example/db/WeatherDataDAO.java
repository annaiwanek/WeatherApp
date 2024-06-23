package org.example.db;

import org.example.model.WeatherData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WeatherDataDAO {

    private static final String DB_URL = "jdbc:sqlite:src/main/resources/weather.db";

    public WeatherDataDAO() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS weather ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "location TEXT NOT NULL, "
                    + "temperature REAL NOT NULL, "
                    + "description TEXT NOT NULL, "
                    + "icon_code TEXT NOT NULL, "
                    + "wind_speed REAL, "
                    + "humidity INTEGER, "
                    + "pressure INTEGER, "
                    + "timestamp TEXT NOT NULL)";
            Statement stmt = conn.createStatement();
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveWeatherData(WeatherData data) {
        String insertSQL = "INSERT INTO weather(location, temperature, description, icon_code, wind_speed, humidity, pressure, timestamp) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, data.getLocation());
            pstmt.setDouble(2, data.getTemperature());
            pstmt.setString(3, data.getDescription());
            pstmt.setString(4, data.getIcon());
            pstmt.setDouble(5, data.getWindSpeed());
            pstmt.setInt(6, (int) data.getHumidity());
            pstmt.setInt(7, (int) data.getPressure());
            pstmt.setString(8, data.getTimestamp() != null ? data.getTimestamp() : "N/A");
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<WeatherData> getAllWeatherData() {
        List<WeatherData> weatherDataList = new ArrayList<>();
        String selectSQL = "SELECT location, temperature, description, icon_code, wind_speed, humidity, pressure, timestamp FROM weather";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                String location = rs.getString("location");
                double temperature = rs.getDouble("temperature");
                String description = rs.getString("description");
                String icon = rs.getString("icon_code");
                double windSpeed = rs.getDouble("wind_speed");
                int humidity = rs.getInt("humidity");
                int pressure = rs.getInt("pressure");
                String timestamp = rs.getString("timestamp");
                weatherDataList.add(new WeatherData(location, temperature, description, icon, windSpeed, humidity, pressure, timestamp));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return weatherDataList;
    }

    public void clearWeatherData() {
        String deleteSQL = "DELETE FROM weather;";
        String vacuum = "VACUUM;";
        try (Connection conn = DriverManager.getConnection(DB_URL)){
            conn.prepareStatement(deleteSQL).execute();
            conn.prepareStatement(vacuum).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
