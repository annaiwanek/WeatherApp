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
                    + "icon TEXT NOT NULL)";
            Statement stmt = conn.createStatement();
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveWeatherData(WeatherData data) {
        String insertSQL = "INSERT INTO weather(location, temperature, description, icon) VALUES(?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, data.getLocation());
            pstmt.setDouble(2, data.getTemperature());
            pstmt.setString(3, data.getDescription());
            pstmt.setString(4, data.getIcon());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<WeatherData> getAllWeatherData() {
        List<WeatherData> weatherDataList = new ArrayList<>();
        String selectSQL = "SELECT location, temperature, description, icon FROM weather";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                String location = rs.getString("location");
                double temperature = rs.getDouble("temperature");
                String description = rs.getString("description");
                String icon = rs.getString("icon");
                weatherDataList.add(new WeatherData(location, temperature, description, icon));
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
