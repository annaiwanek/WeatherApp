package org.example.ui;

import com.formdev.flatlaf.FlatLightLaf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.db.WeatherDataDAO;
import org.example.model.WeatherData;
import org.example.service.OpenWeatherMapService;
import org.example.service.WeatherProvider;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.*;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class WeatherApp extends JFrame {
    private static final Logger logger = LogManager.getLogger(WeatherApp.class);
    private JTextField locationField;
    private JLabel locationLabel;
    private JLabel temperatureLabel;
    private JLabel descriptionLabel;
    private JLabel iconLabel;
    private JLabel dateLabel;
    private WeatherDataDAO weatherDataDAO;
    private ChartPanel chartPanel;
    private ChartPanel forecastChartPanel;
    private JLabel maxTempLabel;
    private JLabel minTempLabel;
    private JLabel avgHumidityLabel;
    private JComboBox<String> unitsComboBox;
    private javax.swing.Timer timer;
    private boolean isMetric = true;
    private String currentLocation = "";
    private JXMapViewer mapViewer;
    private Set<WeatherWaypoint> waypoints = new HashSet<>();
    private JButton getWeatherButton;

    private static final String GEO_API_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";

    private WeatherData currentWeatherData;

    public WeatherApp() {
        logger.info("Starting Weather app...");

        // Set FlatLaf look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("TextComponent.arc", 5);
            UIManager.put("Component.arc", 5);
            UIManager.put("Button.arc", 5);
            UIManager.put("TabbedPane.showContentSeparator", true);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.thumbArc", 999);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Weather App");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize DAO
        weatherDataDAO = new WeatherDataDAO();

        GradientPanel topPanel = new GradientPanel();
        topPanel.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false); // Set transparent background
        inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5)); // Zwiększ odstępy między komponentami
        JLabel locationInputLabel = new JLabel("Enter location:");
        locationInputLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        locationField = new JTextField(15);
        locationField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputPanel.add(locationInputLabel);
        inputPanel.add(locationField);

        getWeatherButton = new JButton("Get Weather");
        getWeatherButton.setBackground(new Color(70, 130, 180));
        getWeatherButton.setForeground(Color.WHITE);
        getWeatherButton.setFocusPainted(false);
        getWeatherButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputPanel.add(getWeatherButton);

        // Add action listener to locationField for Enter key
        locationField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getWeatherButton.doClick();
            }
        });

        getWeatherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentLocation = locationField.getText();
                fetchWeather(currentLocation);
                startRealTimeUpdates();
            }
        });

        // Add the date label to the input panel with a fixed larger gap
        dateLabel = new JLabel("Date: ");
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        inputPanel.add(Box.createHorizontalStrut(40)); // Add a larger fixed gap between the button and date label
        inputPanel.add(dateLabel);

        JPanel unitsPanel = new JPanel();
        unitsPanel.setOpaque(false); // Set transparent background
        unitsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JLabel unitsInputLabel = new JLabel("Select units:");
        unitsInputLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        unitsComboBox = new JComboBox<>(new String[]{"Metric", "Imperial"});
        unitsComboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
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
        weatherInfoPanel.setOpaque(false); // Set transparent background
        weatherInfoPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        locationLabel = new JLabel("Location: ");
        locationLabel.setFont(new Font("SansSerif", Font.BOLD, 16));  // Pogrubiona czcionka
        weatherInfoPanel.add(locationLabel, gbc);
        gbc.gridx++;
        temperatureLabel = new JLabel("Temperature: ");
        temperatureLabel.setFont(new Font("SansSerif", Font.BOLD, 16));  // Pogrubiona czcionka
        weatherInfoPanel.add(temperatureLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        descriptionLabel = new JLabel("Description: ");
        descriptionLabel.setFont(new Font("SansSerif", Font.BOLD, 16));  // Pogrubiona czcionka
        weatherInfoPanel.add(descriptionLabel, gbc);
        gbc.gridx++;
        iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(100, 100)); // Adjust the size of the weather icon
        weatherInfoPanel.add(iconLabel, gbc);

        topPanel.add(weatherInfoPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        GradientPanel summaryPanel = new GradientPanel(new Color(135, 206, 235), new Color(255, 255, 255));
        summaryPanel.setLayout(new GridLayout(1, 3));
        maxTempLabel = new JLabel("Max Temperature: ");
        maxTempLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        minTempLabel = new JLabel("Min Temperature: ");
        minTempLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        avgHumidityLabel = new JLabel("Average Humidity: ");
        avgHumidityLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

        summaryPanel.add(maxTempLabel);
        summaryPanel.add(minTempLabel);
        summaryPanel.add(avgHumidityLabel);

        add(summaryPanel, BorderLayout.SOUTH);

        // Initialize the chart panel
        chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        forecastChartPanel = new ChartPanel(null);
        forecastChartPanel.setPreferredSize(new Dimension(600, 400));

        // Add padding between chart panels
        GradientPanel chartContainerPanel = new GradientPanel(new Color(135, 206, 235), new Color(255, 255, 255)); // Matching the gradient color
        chartContainerPanel.setLayout(new GridLayout(2, 1, 0, 10)); // Thinner separator
        chartContainerPanel.add(chartPanel);
        chartContainerPanel.add(forecastChartPanel);

        add(chartContainerPanel, BorderLayout.CENTER);

        // Initialize the map panel
        initializeMap();
        JScrollPane mapScrollPane = new JScrollPane(mapViewer);
        mapScrollPane.setPreferredSize(new Dimension(600, 400)); // Adjust the size as needed
        add(mapScrollPane, BorderLayout.EAST);

        // Fetch default weather for Warsaw
        fetchWeather("Warsaw");
        startDateUpdater();  // Start the date updater
    }

    private void initializeMap() {
        mapViewer = new JXMapViewer();
        TileFactoryInfo info = new TileFactoryInfo(1, 15, 17,
                256, true, true, "http://tile.openstreetmap.org",
                "x", "y", "z") {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                int z = 17 - zoom;
                return this.baseURL + "/" + z + "/" + x + "/" + y + ".png";
            }
        };
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setZoom(4);
        mapViewer.setAddressLocation(new GeoPosition(52.2297, 21.0122)); // Default location (Warsaw, Poland)

        logger.info("Map initialized and set to default location (Warsaw, Poland)");

        // Add interactions
        PanMouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));

        // Add mouse motion listener for tooltips
        mapViewer.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved(e);
            }
        });
    }

    private void handleMouseMoved(MouseEvent e) {
        Point mousePoint = e.getPoint();
        GeoPosition geoPosition = mapViewer.convertPointToGeoPosition(mousePoint);

        for (WeatherWaypoint waypoint : waypoints) {
            Point2D waypointPoint = mapViewer.getTileFactory().geoToPixel(waypoint.getPosition(), mapViewer.getZoom());
            if (waypointPoint.distance(mousePoint) < 20) { // 20 pixels threshold
                String tooltipText = String.format("<html><b>%s</b><br/>Temp: %.1f %s<br/>Wind: %.2f m/s<br/>Humidity: %.1f%%</html>",
                        waypoint.getWeatherData().getDescription(),
                        roundToHalf(waypoint.getWeatherData().getTemperature()),
                        isMetric ? "°C" : "°F",
                        waypoint.getWeatherData().getWindSpeed(),
                        roundToOneDecimalPlace(waypoint.getWeatherData().getHumidity()));
                mapViewer.setToolTipText(tooltipText);
                ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(mapViewer, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger()));
                return;
            }
        }
        mapViewer.setToolTipText(null);
    }

    private void fetchWeather(String location) {
        new Thread(() -> {
            try {
                WeatherProvider weatherProvider = new OpenWeatherMapService(isMetric);
                currentWeatherData = weatherProvider.getCurrentWeather(location);
                List<WeatherData> hourlyWeather = weatherProvider.getWeather(location);

                weatherDataDAO.clearWeatherData(); // Clear existing data
                weatherDataDAO.saveWeatherData(currentWeatherData);
                for (WeatherData weatherData : hourlyWeather) {
                    weatherDataDAO.saveWeatherData(weatherData);
                }

                GeoPosition geoPosition = fetchCoordinates(location);

                SwingUtilities.invokeLater(() -> {
                    updateUI(currentWeatherData);
                    updateChart(weatherDataDAO.getAllWeatherData());
                    updateForecastChart(hourlyWeather);
                    updateSummary(currentWeatherData, hourlyWeather);
                    updateMap(geoPosition, currentWeatherData);
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

        timer = new javax.swing.Timer(60000, new ActionListener() { // Update every minute (60000 ms)
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
        temperatureLabel.setText("Temperature: " + roundToHalf(data.getTemperature()) + " " + (isMetric ? "°C" : "°F"));
        descriptionLabel.setText("Description: " + data.getDescription());

        String iconUrl = "http://openweathermap.org/img/wn/" + data.getIcon() + "@2x.png";
        logger.info("Icon URL: " + iconUrl);

        try {
            URL url = new URL(iconUrl);
            ImageIcon icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)); // Adjusted size
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                iconLabel.setIcon(icon);
                logger.info("Icon loaded successfully from URL: " + iconUrl);
            } else {
                logger.error("Failed to load icon from URL: " + iconUrl);
                iconLabel.setIcon(null);
            }
        } catch (MalformedURLException e) {
            logger.error("Malformed URL for weather icon: " + iconUrl, e);
            iconLabel.setIcon(null);
        } catch (Exception e) {
            logger.error("Error loading weather icon from URL: " + iconUrl, e);
            iconLabel.setIcon(null);
        }
    }

    private XYDataset createDataset(List<WeatherData> weatherDataList) {
        TimeSeries temperatureSeries = new TimeSeries("Temperature");
        TimeSeries windSpeedSeries = new TimeSeries("Wind Speed");
        TimeSeries humiditySeries = new TimeSeries("Humidity");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (WeatherData data : weatherDataList) {
            if (data.getTimestamp() != null && !data.getTimestamp().equals("N/A")) {
                try {
                    Date date = sdf.parse(data.getTimestamp());
                    temperatureSeries.addOrUpdate(new Minute(date), roundToHalf(data.getTemperature()));
                    windSpeedSeries.addOrUpdate(new Minute(date), data.getWindSpeed());
                    humiditySeries.addOrUpdate(new Minute(date), roundToOneDecimalPlace(data.getHumidity()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(temperatureSeries);
        dataset.addSeries(windSpeedSeries);
        dataset.addSeries(humiditySeries);

        return dataset;
    }

    private XYDataset createForecastDataset(List<WeatherData> weatherDataList) {
        TimeSeries temperatureSeries = new TimeSeries("Temperature");
        TimeSeries windSpeedSeries = new TimeSeries("Wind Speed");
        TimeSeries humiditySeries = new TimeSeries("Humidity");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (WeatherData data : weatherDataList) {
            if (data.getTimestamp() != null && !data.getTimestamp().equals("N/A")) {
                try {
                    Date date = sdf.parse(data.getTimestamp());
                    temperatureSeries.addOrUpdate(new Minute(date), roundToHalf(data.getTemperature()));
                    windSpeedSeries.addOrUpdate(new Minute(date), data.getWindSpeed());
                    humiditySeries.addOrUpdate(new Minute(date), roundToOneDecimalPlace(data.getHumidity()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(temperatureSeries);
        dataset.addSeries(windSpeedSeries);
        dataset.addSeries(humiditySeries);

        return dataset;
    }

    private void updateChart(List<WeatherData> weatherDataList) {
        XYDataset dataset = createDataset(weatherDataList);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Current Weather",
                "Time",
                "Value",
                dataset,
                true, true, false);

        chart.setBackgroundPaint(Color.white);
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 18));

        // Dodanie stylizacji tytułu wykresu
        TextTitle chartTitle = chart.getTitle();
        chartTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        chartTitle.setPaint(new Color(31, 121, 170));
        chartTitle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        chartTitle.setVerticalAlignment(VerticalAlignment.BOTTOM);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new GradientPaint(0, 0, new Color(135, 206, 235), 0, getHeight(), new Color(255, 255, 255)));  // Gradient background
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(70, 130, 180)); // SteelBlue for Temperature
        renderer.setSeriesPaint(1, new Color(34, 139, 34)); // ForestGreen for Wind Speed
        renderer.setSeriesPaint(2, new Color(255, 140, 0)); // DarkOrange for Humidity
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesLinesVisible(2, true);
        plot.setRenderer(renderer);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        rangeAxis.setRange(0, 100);  // Set range from 0 to 100
        rangeAxis.setTickUnit(new NumberTickUnit(10));  // Set tick unit to 10

        DateAxis domainAxis = new DateAxis("Time");
        domainAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        domainAxis.setTickUnit(new DateTickUnit(DateTickUnitType.HOUR, 2)); // Wyświetlanie co godzinę
        domainAxis.setLabelFont(new Font("Arial", Font.PLAIN, 12));

        // Ustawienie minimalnej daty na osi czasu na następną pełną godzinę
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 1); // Przeskoczenie do następnej pełnej godziny
        Date roundedTime = calendar.getTime();
        domainAxis.setMinimumDate(roundedTime);

        // Ustawienie maksymalnej daty na osi czasu na 24 godziny od minimalnej daty
        calendar.add(Calendar.HOUR_OF_DAY, 24);
        Date endTime = calendar.getTime();
        domainAxis.setMaximumDate(endTime);

        plot.setDomainAxis(domainAxis);

        chartPanel.setChart(chart);
    }

    private void updateForecastChart(List<WeatherData> forecastWeatherDataList) {
        XYDataset forecastDataset = createForecastDataset(forecastWeatherDataList);

        JFreeChart forecastChart = ChartFactory.createTimeSeriesChart(
                "Weather Forecast",
                "Date",
                "Value",
                forecastDataset,
                true, true, false);

        forecastChart.setBackgroundPaint(Color.white);
        forecastChart.getTitle().setFont(new Font("Arial", Font.BOLD, 18));

        // Dodanie stylizacji tytułu wykresu
        TextTitle forecastChartTitle = forecastChart.getTitle();
        forecastChartTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        forecastChartTitle.setPaint(new Color(31, 121, 170));
        forecastChartTitle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        forecastChartTitle.setVerticalAlignment(VerticalAlignment.BOTTOM);

        XYPlot forecastPlot = (XYPlot) forecastChart.getPlot();
        forecastPlot.setBackgroundPaint(new GradientPaint(0, 0, new Color(135, 206, 235), 0, getHeight(), new Color(255, 255, 255)));  // Gradient background
        forecastPlot.setDomainGridlinePaint(Color.white);
        forecastPlot.setRangeGridlinePaint(Color.white);

        XYLineAndShapeRenderer forecastRenderer = new XYLineAndShapeRenderer();
        forecastRenderer.setSeriesPaint(0, new Color(70, 130, 180)); // SteelBlue for Temperature
        forecastRenderer.setSeriesPaint(1, new Color(34, 139, 34)); // ForestGreen for Wind Speed
        forecastRenderer.setSeriesPaint(2, new Color(255, 140, 0)); // DarkOrange for Humidity
        forecastRenderer.setSeriesStroke(0, new BasicStroke(2.0f));
        forecastRenderer.setSeriesStroke(1, new BasicStroke(2.0f));
        forecastRenderer.setSeriesStroke(2, new BasicStroke(2.0f));
        forecastPlot.setRenderer(forecastRenderer);

        NumberAxis forecastRangeAxis = (NumberAxis) forecastPlot.getRangeAxis();
        forecastRangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        forecastRangeAxis.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        forecastRangeAxis.setRange(0, 100);  // Set range from 0 to 100
        forecastRangeAxis.setTickUnit(new NumberTickUnit(10));  // Set tick unit to 10

        DateAxis forecastDomainAxis = new DateAxis("Date");
        forecastDomainAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
        forecastDomainAxis.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        forecastPlot.setDomainAxis(forecastDomainAxis);

        forecastChartPanel.setChart(forecastChart);
    }

    private void updateMap(GeoPosition geoPosition, WeatherData weatherData) {
        mapViewer.setCenterPosition(geoPosition);
        mapViewer.setZoom(6);

        // Add a waypoint for the weather data
        waypoints.clear();
        waypoints.add(new WeatherWaypoint(weatherData, geoPosition));

        WaypointPainter<WeatherWaypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);
        waypointPainter.setRenderer(new WeatherWaypointRenderer(isMetric));

        List<Painter<JXMapViewer>> painters = new java.util.ArrayList<>();
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);

        logger.info("Map updated with new location and waypoint");
    }

    private GeoPosition fetchCoordinates(String location) throws Exception {
        String apiKey = "22235b26b7ac6a135126512bf6a8d8e9"; // Replace "YOUR_API_KEY" with your actual API key
        String urlString = String.format(GEO_API_URL, location, apiKey);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            JSONObject json = new JSONObject(content.toString());
            double latitude = json.getJSONObject("coord").getDouble("lat");
            double longitude = json.getJSONObject("coord").getDouble("lon");

            return new GeoPosition(latitude, longitude);
        } else {
            throw new Exception("Failed to fetch coordinates: " + responseCode);
        }
    }

    private void updateSummary(WeatherData currentWeather, List<WeatherData> hourlyWeather) {
        double maxTemp = currentWeather.getTemperature();
        double minTemp = currentWeather.getTemperature();
        double totalHumidity = currentWeather.getHumidity();

        for (WeatherData data : hourlyWeather) {
            if (data.getTemperature() > maxTemp) {
                maxTemp = data.getTemperature();
            }
            if (data.getTemperature() < minTemp) {
                minTemp = data.getTemperature();
            }
        }

        double avgHumidity = totalHumidity;

        maxTempLabel.setText("Max Temperature: " + roundToHalf(maxTemp) + " " + (isMetric ? "°C" : "°F"));
        minTempLabel.setText("Min Temperature: " + roundToHalf(minTemp) + " " + (isMetric ? "°C" : "°F"));
        avgHumidityLabel.setText("Average Humidity: " + roundToOneDecimalPlace(avgHumidity) + " %");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private double roundToHalf(double value) {
        return Math.round(value * 2) / 2.0;
    }

    private double roundToOneDecimalPlace(double value) {
        return Math.round(value * 10) / 10.0;
    }

    // Getters for testing
    public JTextField getLocationField() {
        return locationField;
    }

    public JButton getGetWeatherButton() {
        return getWeatherButton;
    }

    public JComboBox<String> getUnitsComboBox() {
        return unitsComboBox;
    }

    public JLabel getLocationLabel() {
        return locationLabel;
    }

    public JLabel getTemperatureLabel() {
        return temperatureLabel;
    }

    public JLabel getDescriptionLabel() {
        return descriptionLabel;
    }

    public JLabel getIconLabel() {
        return iconLabel;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeatherApp app = new WeatherApp();
            app.setVisible(true);
        });
    }

    // Klasa GradientPanel, która maluje gradient na swoim tle
    class GradientPanel extends JPanel {
        private final Color color1;
        private final Color color2;

        public GradientPanel(Color color1, Color color2) {
            this.color1 = color1;
            this.color2 = color2;
        }

        public GradientPanel() {
            this(new Color(70, 130, 180), new Color(255, 255, 255)); // Default gradient
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);
        }
    }

    private void startDateUpdater() {
        javax.swing.Timer dateTimer = new javax.swing.Timer(1000, new ActionListener() { // Update every second
            @Override
            public void actionPerformed(ActionEvent e) {
                updateDate();
            }
        });
        dateTimer.start();
    }

    private void updateDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = sdf.format(new Date());
        dateLabel.setText("Date: " + currentDate);
    }
}
