package org.example.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.service.OpenWeatherMapService;
import org.example.service.WeatherProvider;
import org.example.model.WeatherData;
import org.example.db.WeatherDataDAO;

public class WeatherApp extends JFrame {
    private static Logger logger = LogManager.getLogger(WeatherApp.class);
    private JTextField locationField;
    private JLabel locationLabel;
    private JLabel temperatureLabel;
    private JLabel descriptionLabel;
    private JLabel iconLabel;
    private JButton showChartButton;
    private JButton clearDataButton;
    private WeatherDataDAO weatherDataDAO;

    public WeatherApp() {
        logger.info("Starting Weather app...");
        setTitle("Weather App");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Inicjalizacja DAO
        weatherDataDAO = new WeatherDataDAO();

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(8, 1));

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

        showChartButton = new JButton("Show Chart");
        showChartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showChart();
            }
        });
        panel.add(showChartButton);

        clearDataButton = new JButton("Clear Data");
        clearDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearWeatherData();
            }
        });
        panel.add(clearDataButton);

        add(panel, BorderLayout.CENTER);
    }

    private void fetchWeather(String location) {
        new Thread(() -> {
            try {
                WeatherProvider weatherProvider = new OpenWeatherMapService();
                WeatherData data = weatherProvider.getWeather(location);
                weatherDataDAO.saveWeatherData(data);
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

    private void showChart() {
        List<WeatherData> weatherDataList = weatherDataDAO.getAllWeatherData();
        SwingUtilities.invokeLater(() -> {
            WeatherChart chart = new WeatherChart("Weather Chart", weatherDataList);
            chart.setSize(800, 600);
            chart.setLocationRelativeTo(null);
            chart.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            chart.setVisible(true);
        });
    }

    private void clearWeatherData() {
        weatherDataDAO.clearWeatherData();
        JOptionPane.showMessageDialog(this, "Weather data cleared.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeatherApp app = new WeatherApp();
            app.setVisible(true);
        });
    }
}
