package org.example.ui;

import com.formdev.flatlaf.FlatLightLaf;
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
    private ChartPanel forecastChartPanel;
    private JLabel maxTempLabel;
    private JLabel minTempLabel;
    private JLabel avgHumidityLabel;
    private JComboBox<String> unitsComboBox;
    private Timer timer;
    private boolean isMetric = true; // Default to metric units
    private String currentLocation = ""; // Store current location for real-time updates

    public WeatherApp() {
        logger.info("Starting Weather app...");

        // Set FlatLaf look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Weather App");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize DAO
        weatherDataDAO = new WeatherDataDAO();

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel locationInputLabel = new JLabel("Enter location:");
        locationField = new JTextField(15);
        inputPanel.add(locationInputLabel);
        inputPanel.add(locationField);

        JButton getWeatherButton = new JButton("Get Weather");
        getWeatherButton.setBackground(new Color(70, 130, 180));
        getWeatherButton.setForeground(Color.WHITE);
        getWeatherButton.setFocusPainted(false);
        inputPanel.add(getWeatherButton);

        getWeatherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentLocation = locationField.getText();
                fetchWeather(currentLocation);
                startRealTimeUpdates();
            }
        });

        JPanel unitsPanel = new JPanel();
        unitsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JLabel unitsInputLabel = new JLabel("Select units:");
        unitsComboBox = new JComboBox<>(new String[]{"Metric", "Imperial"});
        unitsPanel.add(unitsInputLabel);
        unitsPanel.add(unitsComboBox);

        unitsComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedUnit = (String) unitsComboBox.getSelectedItem();
                isMetric = "Metric".equals(selectedUnit);
                if (!currentLocation.isEmpty()) {
                    fetchWeather(currentLocation); // Refetch weather data with the new units
                }
            }
        });

        topPanel.add(inputPanel, BorderLayout.WEST);
        topPanel.add(unitsPanel, BorderLayout.EAST);

        JPanel weatherInfoPanel = new JPanel();
        weatherInfoPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        locationLabel = new JLabel("Location: ");
        weatherInfoPanel.add(locationLabel, gbc);
        gbc.gridx++;
        temperatureLabel = new JLabel("Temperature: ");
        weatherInfoPanel.add(temperatureLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        descriptionLabel = new JLabel("Description: ");
        weatherInfoPanel.add(descriptionLabel, gbc);
        gbc.gridx++;
        iconLabel = new JLabel();
        weatherInfoPanel.add(iconLabel, gbc);

        topPanel.add(weatherInfoPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new GridLayout(1, 3));
        maxTempLabel = new JLabel("Max Temperature: ");
        minTempLabel = new JLabel("Min Temperature: ");
        avgHumidityLabel = new JLabel("Average Humidity: ");

        summaryPanel.add(maxTempLabel);
        summaryPanel.add(minTempLabel);
        summaryPanel.add(avgHumidityLabel);

        add(summaryPanel, BorderLayout.SOUTH);

        // Initialize the chart panel
        chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        forecastChartPanel = new ChartPanel(null);
        forecastChartPanel.setPreferredSize(new Dimension(600, 400));

        JPanel chartContainerPanel = new JPanel();
        chartContainerPanel.setLayout(new GridLayout(2, 1));
        chartContainerPanel.add(chartPanel);
        chartContainerPanel.add(forecastChartPanel);

        add(chartContainerPanel, BorderLayout.CENTER);
    }

    private void fetchWeather(String location) {
        new Thread(() -> {
            try {
                WeatherProvider weatherProvider = new OpenWeatherMapService(isMetric);
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
                    updateForecastChart(hourlyWeather);
                    updateSummary(weatherDataDAO.getAllWeatherData());
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> showError(e.getMessage()));
            }
        }).start();
    }

    private void startRealTimeUpdates() {
        if (timer != null) {
            timer.stop();
        }

        timer = new Timer(60000, new ActionListener() { // Update every minute (60000 ms)
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!currentLocation.isEmpty()) {
                    fetchWeather(currentLocation);
                }
            }
        });
        timer.start();
    }

    private void updateUI(WeatherData data) {
        locationLabel.setText("Location: " + data.getLocation());
        temperatureLabel.setText("Temperature: " + data.getTemperature() + " " + (isMetric ? "°C" : "°F"));
        descriptionLabel.setText("Description: " + data.getDescription());

        String iconUrl = "http://openweathermap.org/img/wn/" + data.getIcon() + "@2x.png";
        try {
            iconLabel.setIcon(new ImageIcon(new ImageIcon(new URL(iconUrl)).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void updateChart(List<WeatherData> weatherDataList) {
        XYSeriesCollection dataset = createDataset(weatherDataList);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Weather Data",
                "Time",
                "Value",
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
        plot.setRenderer(renderer);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setVerticalTickLabels(true);

        chartPanel.setChart(chart);
    }

    private void updateForecastChart(List<WeatherData> weatherDataList) {
        XYSeriesCollection dataset = createForecastDataset(weatherDataList);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Weather Forecast",
                "Time",
                "Value",
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
        plot.setRenderer(renderer);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setVerticalTickLabels(true);

        forecastChartPanel.setChart(chart);
    }

    private XYSeriesCollection createDataset(List<WeatherData> weatherDataList) {
        XYSeries temperatureSeries = new XYSeries("Temperature");
        XYSeries windSpeedSeries = new XYSeries("Wind Speed");
        XYSeries humiditySeries = new XYSeries("Humidity");

        int index = 0;
        for (WeatherData data : weatherDataList) {
            if (data.getTimestamp() != null) {
                temperatureSeries.add(index, data.getTemperature());
                windSpeedSeries.add(index, data.getWindSpeed());
                humiditySeries.add(index, data.getHumidity());
                index++;
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(temperatureSeries);
        dataset.addSeries(windSpeedSeries);
        dataset.addSeries(humiditySeries);

        return dataset;
    }

    private XYSeriesCollection createForecastDataset(List<WeatherData> weatherDataList) {
        XYSeries temperatureSeries = new XYSeries("Temperature");
        XYSeries windSpeedSeries = new XYSeries("Wind Speed");
        XYSeries humiditySeries = new XYSeries("Humidity");

        int index = 0;
        for (WeatherData data : weatherDataList) {
            if (data.getTimestamp() != null) {
                temperatureSeries.add(index, data.getTemperature());
                windSpeedSeries.add(index, data.getWindSpeed());
                humiditySeries.add(index, data.getHumidity());
                index++;
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(temperatureSeries);
        dataset.addSeries(windSpeedSeries);
        dataset.addSeries(humiditySeries);

        return dataset;
    }

    private void updateSummary(List<WeatherData> weatherDataList) {
        double maxTemp = Double.MIN_VALUE;
        double minTemp = Double.MAX_VALUE;
        double totalHumidity = 0;

        for (WeatherData data : weatherDataList) {
            if (data.getTemperature() > maxTemp) {
                maxTemp = data.getTemperature();
            }
            if (data.getTemperature() < minTemp) {
                minTemp = data.getTemperature();
            }
            totalHumidity += data.getHumidity();
        }

        double avgHumidity = totalHumidity / weatherDataList.size();

        maxTempLabel.setText("Max Temperature: " + maxTemp + " " + (isMetric ? "°C" : "°F"));
        minTempLabel.setText("Min Temperature: " + minTemp + " " + (isMetric ? "°C" : "°F"));
        avgHumidityLabel.setText("Average Humidity: " + avgHumidity + " %");
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
