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
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Day;
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

    private static final String GEO_API_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";

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

        // Initialize the map panel
        initializeMap();
        JScrollPane mapScrollPane = new JScrollPane(mapViewer);
        mapScrollPane.setPreferredSize(new Dimension(600, 400)); // Adjust the size as needed
        add(mapScrollPane, BorderLayout.EAST);

        // Fetch default weather for Warsaw
        fetchWeather("Warsaw");
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
                String tooltipText = String.format("<html><b>%s</b><br/>Temp: %.2f°C<br/>Wind: %.2f m/s<br/>Humidity: %.2f%%</html>",
                        waypoint.getWeatherData().getDescription(),
                        waypoint.getWeatherData().getTemperature(),
                        waypoint.getWeatherData().getWindSpeed(),
                        waypoint.getWeatherData().getHumidity());
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
                WeatherData currentWeather = weatherProvider.getCurrentWeather(location);
                List<WeatherData> hourlyWeather = weatherProvider.getWeather(location);
                List<WeatherData> currentDayWeather = filterCurrentDayData(hourlyWeather);
                List<WeatherData> forecastWeather = filterForecastData(hourlyWeather);

                weatherDataDAO.clearWeatherData(); // Clear existing data
                weatherDataDAO.saveWeatherData(currentWeather);
                for (WeatherData weatherData : hourlyWeather) {
                    weatherDataDAO.saveWeatherData(weatherData);
                }

                GeoPosition geoPosition = fetchCoordinates(location);

                SwingUtilities.invokeLater(() -> {
                    updateUI(currentWeather);
                    updateChart(currentDayWeather);
                    updateForecastChart(forecastWeather);
                    updateSummary(weatherDataDAO.getAllWeatherData());
                    updateMap(geoPosition, currentWeather);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> showError(e.getMessage()));
            }
        }).start();
    }

    private List<WeatherData> filterCurrentDayData(List<WeatherData> weatherDataList) {
        List<WeatherData> filteredCurrentDay = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Pobierz czas bieżący zaokrąglony w dół do pełnej godziny
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 1); // Start from the next full hour
        Date nextHour = calendar.getTime();

        for (WeatherData data : weatherDataList) {
            try {
                Date date = sdf.parse(data.getTimestamp());

                // Sprawdź, czy czas jest między następną pełną godziną a końcem dnia
                if (!date.before(nextHour) && date.before(ceilingDate(calendar.getTime(), Calendar.DAY_OF_MONTH))) {
                    filteredCurrentDay.add(data);

                    // Przesuń 'nextHour' do następnej godziny, aby uwzględnić tylko co godzinę
                    calendar.setTime(nextHour);
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                    nextHour = calendar.getTime();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return filteredCurrentDay;
    }

    private List<WeatherData> filterForecastData(List<WeatherData> weatherDataList) {
        List<WeatherData> filteredForecast = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1); // Start from tomorrow
        Date startDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 5); // Include the next 5 days
        Date endDate = calendar.getTime();

        for (WeatherData data : weatherDataList) {
            try {
                Date date = sdf.parse(data.getTimestamp());
                if (!date.before(startDate) && date.before(endDate)) {
                    filteredForecast.add(data);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return filteredForecast;
    }

    private Date ceilingDate(Date date, int field) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        switch (field) {
            case Calendar.DAY_OF_MONTH:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                break;
            // Add other cases if needed
        }
        return calendar.getTime();
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
        temperatureLabel.setText("Temperature: " + data.getTemperature() + " " + (isMetric ? "°C" : "°F"));
        descriptionLabel.setText("Description: " + data.getDescription());

        String iconUrl = "http://openweathermap.org/img/wn/" + data.getIcon() + "@2x.png";
        logger.info("Icon URL: " + iconUrl);

        try {
            URL url = new URL(iconUrl);
            ImageIcon icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
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
                    temperatureSeries.addOrUpdate(new Minute(date), data.getTemperature());
                    windSpeedSeries.addOrUpdate(new Minute(date), data.getWindSpeed());
                    humiditySeries.addOrUpdate(new Minute(date), data.getHumidity());
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
        TimeSeries forecastTemperatureSeries = new TimeSeries("Forecast Temperature");
        TimeSeries forecastWindSpeedSeries = new TimeSeries("Forecast Wind Speed");
        TimeSeries forecastHumiditySeries = new TimeSeries("Forecast Humidity");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (WeatherData data : weatherDataList) {
            if (data.getTimestamp() != null && !data.getTimestamp().equals("N/A")) {
                try {
                    Date date = sdf.parse(data.getTimestamp());
                    forecastTemperatureSeries.addOrUpdate(new Day(date), data.getTemperature());
                    forecastWindSpeedSeries.addOrUpdate(new Day(date), data.getWindSpeed());
                    forecastHumiditySeries.addOrUpdate(new Day(date), data.getHumidity());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(forecastTemperatureSeries);
        dataset.addSeries(forecastWindSpeedSeries);
        dataset.addSeries(forecastHumiditySeries);

        return dataset;
    }

    private void updateChart(List<WeatherData> weatherDataList) {
        XYDataset dataset = createDataset(weatherDataList);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Weather Data",
                "Time",
                "Value",
                dataset,
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

        DateAxis domainAxis = new DateAxis("Time");
        domainAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        domainAxis.setTickUnit(new DateTickUnit(DateTickUnitType.MINUTE, 30)); // Wyswietlanie co 30 minut
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

        XYPlot forecastPlot = (XYPlot) forecastChart.getPlot();
        forecastPlot.setBackgroundPaint(Color.lightGray);
        forecastPlot.setDomainGridlinePaint(Color.white);
        forecastPlot.setRangeGridlinePaint(Color.white);

        XYLineAndShapeRenderer forecastRenderer = new XYLineAndShapeRenderer();
        forecastRenderer.setSeriesPaint(0, Color.MAGENTA); // Temperature in forecast
        forecastRenderer.setSeriesPaint(1, Color.CYAN); // Wind Speed in forecast
        forecastRenderer.setSeriesPaint(2, Color.ORANGE); // Humidity in forecast
        forecastRenderer.setSeriesStroke(0, new BasicStroke(2.0f));
        forecastRenderer.setSeriesStroke(1, new BasicStroke(2.0f));
        forecastRenderer.setSeriesStroke(2, new BasicStroke(2.0f));
        forecastPlot.setRenderer(forecastRenderer);

        NumberAxis forecastRangeAxis = (NumberAxis) forecastPlot.getRangeAxis();
        forecastRangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        DateAxis forecastDomainAxis = new DateAxis("Date");
        forecastDomainAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
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
        waypointPainter.setRenderer(new WeatherWaypointRenderer());

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
