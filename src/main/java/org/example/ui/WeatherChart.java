package org.example.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.example.model.WeatherData;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WeatherChart extends JFrame {

    public WeatherChart(String title, List<WeatherData> weatherDataList) {
        super(title);

        // Create dataset
        DefaultCategoryDataset dataset = createDataset(weatherDataList);

        // Create chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Dane pogodowe",
                "Czas",
                "Wartość",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Customize the chart
        chart.setBackgroundPaint(Color.white);

        // Add the chart to a panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        setContentPane(chartPanel);
    }

    private DefaultCategoryDataset createDataset(List<WeatherData> weatherDataList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (WeatherData data : weatherDataList) {
            if (data.getTimestamp() != null) {
                dataset.addValue(data.getTemperature(), "Temperatura", data.getTimestamp());
                dataset.addValue(data.getWindSpeed(), "Prędkość wiatru", data.getTimestamp());
                dataset.addValue(data.getHumidity(), "Wilgotność", data.getTimestamp());
                dataset.addValue(data.getPressure(), "Ciśnienie", data.getTimestamp());
            }
        }

        return dataset;
    }
}
