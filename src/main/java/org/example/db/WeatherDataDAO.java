package org.example.db;

import org.example.model.WeatherData;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WeatherDataDAO {

    private static final String DB_URL = "jdbc:sqlite:weather.db";

    public WeatherDataDAO() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS weather ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "location TEXT NOT NULL, "
                    + "temperature REAL NOT NULL, "
                    + "description TEXT NOT NULL, "
                    + "icon TEXT NOT NULL, "
                    + "windSpeed REAL, "
                    + "humidity REAL NOT NULL, "
                    + "pressure REAL NOT NULL, "
                    + "timestamp TEXT)"; // Dodano pole timestamp
            Statement stmt = conn.createStatement();
            stmt.execute(createTableSQL);

            // Dodanie kolumny timestamp, jeśli jej nie ma
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet rs = dbm.getColumns(null, null, "weather", "timestamp");
            if (!rs.next()) { // kolumna nie istnieje, więc ją dodajemy
                String addColumnSQL = "ALTER TABLE weather ADD COLUMN timestamp TEXT";
                stmt.execute(addColumnSQL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveWeatherData(WeatherData data) {
        String insertSQL = "INSERT INTO weather(location, temperature, description, icon, windSpeed, humidity, pressure, timestamp) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, data.getLocation());
            pstmt.setDouble(2, data.getTemperature());
            pstmt.setString(3, data.getDescription());
            pstmt.setString(4, data.getIcon());
            pstmt.setDouble(5, data.getWindSpeed());
            pstmt.setDouble(6, data.getHumidity());
            pstmt.setDouble(7, data.getPressure());
            pstmt.setString(8, data.getTimestamp()); // Dodano pole timestamp
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<WeatherData> getAllWeatherData() {
        List<WeatherData> weatherDataList = new ArrayList<>();
        String selectSQL = "SELECT location, temperature, description, icon, windSpeed, humidity, pressure, timestamp FROM weather";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                String location = rs.getString("location");
                double temperature = rs.getDouble("temperature");
                String description = rs.getString("description");
                String icon = rs.getString("icon");
                double windSpeed = rs.getDouble("windSpeed");
                double humidity = rs.getDouble("humidity");
                double pressure = rs.getDouble("pressure");
                String timestamp = rs.getString("timestamp"); // Dodano pole timestamp
                WeatherData data = new WeatherData(location, temperature, description, icon, windSpeed, humidity, pressure, timestamp);
                weatherDataList.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return weatherDataList;
    }

    public void clearWeatherData() {
        String deleteSQL = "DELETE FROM weather";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
