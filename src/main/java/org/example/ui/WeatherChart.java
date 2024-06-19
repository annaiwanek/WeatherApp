package org.example.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.PlotOrientation; // Dodaj ten import
import org.example.model.WeatherData;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WeatherChart extends JFrame {
    public WeatherChart(String title, List<WeatherData> weatherDataList) {
        super(title);

        // Utwórz dataset
        DefaultCategoryDataset dataset = createDataset(weatherDataList);

        // Utwórz wykres
        JFreeChart chart = ChartFactory.createLineChart(
                "Dane pogodowe",
                "Czas",
                "Wartość",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Dostosuj wykres
        chart.setBackgroundPaint(Color.white);

        // Dodaj wykres do panelu
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
    }

    private DefaultCategoryDataset createDataset(List<WeatherData> weatherDataList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (WeatherData data : weatherDataList) {
            dataset.addValue(data.getTemperature(), "Temperatura", data.getLocation());
            dataset.addValue(data.getWindSpeed(), "Prędkość wiatru", data.getLocation());
            dataset.addValue(data.getHumidity(), "Wilgotność", data.getLocation()); // Dodano pole humidity
            dataset.addValue(data.getPressure(), "Ciśnienie", data.getLocation()); // Dodano pole pressure
        }

        return dataset;
    }
}
