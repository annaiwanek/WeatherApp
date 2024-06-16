package org.example;

import javax.swing.SwingUtilities;
import org.example.ui.WeatherApp;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeatherApp app = new WeatherApp();
            app.setVisible(true);
        });
    }
}
