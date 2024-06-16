package org.example.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.example.service.WeatherService;
import org.example.model.WeatherData;

public class WeatherApp extends JFrame {
    private JTextField locationField;
    private JLabel locationLabel;
    private JLabel temperatureLabel;
    private JLabel descriptionLabel;
    private JLabel iconLabel;

    public WeatherApp() {
        setTitle("Weather App");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));

        locationField = new JTextField();
        panel.add(new JLabel("Enter location:"));
        panel.add(locationField);

        JButton getWeatherButton = new JButton("Get Weather");
        getWeatherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String location = locationField.getText();
                fetchWeather(location);
            }
        });
        panel.add(getWeatherButton);

        locationLabel = new JLabel("Location: ");
        temperatureLabel = new JLabel("Temperature: ");
        descriptionLabel = new JLabel("Description: ");
        iconLabel = new JLabel();

        panel.add(locationLabel);
        panel.add(temperatureLabel);
        panel.add(descriptionLabel);
        panel.add(iconLabel);

        add(panel, BorderLayout.CENTER);
    }

    private void fetchWeather(String location) {
        new Thread(() -> {
            try {
                WeatherData data = new WeatherService().getWeather(location);
                SwingUtilities.invokeLater(() -> updateUI(data));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> showError(e.getMessage()));
            }
        }).start();
    }

    private void updateUI(WeatherData data) {
        locationLabel.setText("Location: " + data.getLocation());
        temperatureLabel.setText("Temperature: " + data.getTemperature() + " °C");
        descriptionLabel.setText("Description: " + data.getDescription());
        iconLabel.setIcon(new ImageIcon(new ImageIcon("http://openweathermap.org/img/wn/" + data.getIcon() + "@2x.png").getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeatherApp app = new WeatherApp();
            app.setVisible(true);
        });
    }
}
