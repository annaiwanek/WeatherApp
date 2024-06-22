package org.example.ui;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.WaypointRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URL;

public class WeatherWaypointRenderer implements WaypointRenderer<WeatherWaypoint> {

    private final Font font = new Font("Arial", Font.PLAIN, 12);
    private final boolean isMetric;

    public WeatherWaypointRenderer(boolean isMetric) {
        this.isMetric = isMetric;
    }

    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer map, WeatherWaypoint waypoint) {
        Point2D point = map.getTileFactory().geoToPixel(waypoint.getPosition(), map.getZoom());
        Point drawPoint = new Point((int) point.getX(), (int) point.getY());

        String description = waypoint.getWeatherData().getDescription();
        String temperature = "Temp: " + roundToHalf(waypoint.getWeatherData().getTemperature()) + (isMetric ? "°C" : "°F");
        String windSpeed = "Wind: " + waypoint.getWeatherData().getWindSpeed() + " m/s";
        String humidity = "Humidity: " + waypoint.getWeatherData().getHumidity() + "%";

        g.setFont(font);
        g.setColor(Color.BLACK);
        FontMetrics fm = g.getFontMetrics();
        int maxWidth = Math.max(Math.max(fm.stringWidth(description), fm.stringWidth(temperature)), Math.max(fm.stringWidth(windSpeed), fm.stringWidth(humidity)));
        int textHeight = fm.getHeight() * 4; // 4 lines of text

        int boxX = drawPoint.x + 30; // Shift the box further to the right
        int boxY = drawPoint.y - textHeight - 30; // Shift the box further up

        g.setColor(new Color(255, 255, 255, 200));
        g.fillRoundRect(boxX, boxY, maxWidth + 20, textHeight + 20, 10, 10);
        g.setColor(Color.BLACK);
        g.drawRoundRect(boxX, boxY, maxWidth + 20, textHeight + 20, 10, 10);

        int textX = boxX + 10;
        int textY = boxY + fm.getAscent() + 10;

        g.drawString(description, textX, textY);
        g.drawString(temperature, textX, textY + fm.getHeight());
        g.drawString(windSpeed, textX, textY + fm.getHeight() * 2);
        g.drawString(humidity, textX, textY + fm.getHeight() * 3);

        try {
            URL url = new URL("http://openweathermap.org/img/wn/" + waypoint.getWeatherData().getIcon() + "@2x.png");
            ImageIcon icon = new ImageIcon(url);
            g.drawImage(icon.getImage(), drawPoint.x - icon.getIconWidth() / 2, drawPoint.y - icon.getIconHeight(), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double roundToHalf(double value) {
        return Math.round(value * 2) / 2.0;
    }
}
