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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;

public class WeatherApp extends JFrame {
    private static Logger logger = LogManager.getLogger(WeatherApp.class);
    private JTextField locationField;
    private JLabel locationLabel;
    private JLabel temperatureLabel;
    private JLabel descriptionLabel;
    private JLabel iconLabel;
    private WeatherDataDAO weatherDataDAO;
    private ChartPanel chartPanel;

    public WeatherApp() {
        logger.info("Starting Weather app...");
        setTitle("Weather App");
        setSize(800, 600); // Zwiększenie rozmiaru okna
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

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

        add(panel, BorderLayout.NORTH);

        // Initialize the chart panel
        chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        add(chartPanel, BorderLayout.CENTER);
    }

    private void fetchWeather(String location) {
        new Thread(() -> {
            try {
                WeatherProvider weatherProvider = new OpenWeatherMapService();
                WeatherData currentWeather = weatherProvider.getCurrentWeather(location);
                List<WeatherData> hourlyWeather = weatherProvider.getWeather(location);

                weatherDataDAO.clearWeatherData(); // Clear existing data
                weatherDataDAO.saveWeatherData(currentWeather);
                for (WeatherData weatherData : hourlyWeather) {
                    weatherDataDAO.saveWeatherData(weatherData);
                }

                SwingUtilities.invokeLater(() -> {
                    updateUI(currentWeather);
                    updateChart(weatherDataDAO.getAllWeatherData());
                });
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

    private void updateChart(List<WeatherData> weatherDataList) {
        XYSeriesCollection dataset = createDataset(weatherDataList);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Dane pogodowe",
                "Czas",
                "Wartość",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED); // Temperature
        renderer.setSeriesPaint(1, Color.BLUE); // Wind Speed
        renderer.setSeriesPaint(2, Color.GREEN); // Humidity
        renderer.setSeriesPaint(3, Color.YELLOW); // Pressure
        plot.setRenderer(renderer);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setVerticalTickLabels(true);

        chartPanel.setChart(chart);
    }

    private XYSeriesCollection createDataset(List<WeatherData> weatherDataList) {
        XYSeries temperatureSeries = new XYSeries("Temperatura");
        XYSeries windSpeedSeries = new XYSeries("Prędkość wiatru");
        XYSeries humiditySeries = new XYSeries("Wilgotność");
        XYSeries pressureSeries = new XYSeries("Ciśnienie");

        int index = 0;
        for (WeatherData data : weatherDataList) {
            if (data.getTimestamp() != null) {
                temperatureSeries.add(index, data.getTemperature());
                windSpeedSeries.add(index, data.getWindSpeed());
                humiditySeries.add(index, data.getHumidity());
                pressureSeries.add(index, data.getPressure());
                index++;
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(temperatureSeries);
        dataset.addSeries(windSpeedSeries);
        dataset.addSeries(humiditySeries);
        dataset.addSeries(pressureSeries);

        return dataset;
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
