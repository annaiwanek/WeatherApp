package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class WeatherDatabase {
    private static final String URL = "jdbc:sqlite:weather.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
