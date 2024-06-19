package org.example.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
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
    private WeatherDataDAO weatherDataDAO;
    private Image backgroundImage;

    public WeatherApp() {
        logger.info("Starting Weather app...");
        setTitle("Weather App");
        setSize(600, 400); // Zwiększenie rozmiaru okna
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Load background image
        backgroundImage = new ImageIcon("src/main/resources/cumulus.jpg").getImage();

        // Initialize DAO
        weatherDataDAO = new WeatherDataDAO();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw gradient background
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                Color color1 = new Color(135, 206, 235);
                Color color2 = new Color(25, 25, 112);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };
        panel.setLayout(new GridBagLayout()); // Użycie GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        locationField = new JTextField(15);
        panel.add(new JLabel("Enter location:"), gbc);
        gbc.gridx++;
        panel.add(locationField, gbc);

        JButton getWeatherButton = new JButton("Get Weather");
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        getWeatherButton.setBackground(new Color(70, 130, 180));
        getWeatherButton.setForeground(Color.WHITE);
        getWeatherButton.setFocusPainted(false);
        panel.add(getWeatherButton, gbc);

        getWeatherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String location = locationField.getText();
                fetchWeather(location);
            }
        });

        locationLabel = new JLabel("Location: ");
        temperatureLabel = new JLabel("Temperature: ");
        descriptionLabel = new JLabel("Description: ");
        iconLabel = new JLabel();

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(locationLabel, gbc);
        gbc.gridy++;
        panel.add(temperatureLabel, gbc);
        gbc.gridy++;
        panel.add(descriptionLabel, gbc);
        gbc.gridy++;
        panel.add(iconLabel, gbc);

        JButton showChartButton = new JButton("Pokaż Wykres");
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(showChartButton, gbc);

        showChartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<WeatherData> weatherDataList = weatherDataDAO.getAllWeatherData();
                WeatherChart chart = new WeatherChart("Wykres Pogodowy", weatherDataList);
                chart.pack();
                chart.setVisible(true);
            }
        });

        add(panel, BorderLayout.CENTER);
    }

    private void fetchWeather(String location) {
        new Thread(() -> {
            try {
                WeatherProvider weatherProvider = new OpenWeatherMapService();
                WeatherData currentWeather = weatherProvider.getCurrentWeather(location);
                List<WeatherData> hourlyWeather = weatherProvider.getWeather(location);

                weatherDataDAO.saveWeatherData(currentWeather);
                for (WeatherData weatherData : hourlyWeather) {
                    weatherDataDAO.saveWeatherData(weatherData);
                }

                SwingUtilities.invokeLater(() -> updateUI(currentWeather));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> showError(e.getMessage()));
            }
        }).start();
    }

    private void updateUI(WeatherData data) {
        locationLabel.setText("Location: " + data.getLocation());
        temperatureLabel.setText("Temperature: " + data.getTemperature() + " °C");
        descriptionLabel.setText("Description: " + data.getDescription());

        String iconUrl = "http://openweathermap.org/img/wn/" + data.getIcon() + "@2x.png";
        try {
            iconLabel.setIcon(new ImageIcon(new ImageIcon(new URL(iconUrl)).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
