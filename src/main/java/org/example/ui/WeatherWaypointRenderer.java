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

    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer map, WeatherWaypoint waypoint) {
        Point2D point = map.getTileFactory().geoToPixel(waypoint.getPosition(), map.getZoom());
        Point drawPoint = new Point((int) point.getX(), (int) point.getY());

        String text = String.format("%s\nTemp: %.2f°C\nWind: %.2f m/s\nHumidity: %.2f%%",
                waypoint.getWeatherData().getDescription(),
                waypoint.getWeatherData().getTemperature(),
                waypoint.getWeatherData().getWindSpeed(),
                waypoint.getWeatherData().getHumidity());

        g.setFont(font);
        g.setColor(Color.BLACK);
        g.drawString(text, drawPoint.x + 5, drawPoint.y);

        try {
            URL url = new URL("http://openweathermap.org/img/wn/" + waypoint.getWeatherData().getIcon() + "@2x.png");
            ImageIcon icon = new ImageIcon(url);
            g.drawImage(icon.getImage(), drawPoint.x - icon.getIconWidth() / 2, drawPoint.y - icon.getIconHeight(), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
