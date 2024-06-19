package org.example.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.title.TextTitle;
import org.example.model.WeatherData;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WeatherChart extends JFrame {

    public WeatherChart(String title, List<WeatherData> weatherDataList) {
        super(title);

        // Create dataset
        XYSeriesCollection dataset = createDataset(weatherDataList);

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Dane pogodowe",
                "Czas",
                "Wartość",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Customize the chart
        chart.setBackgroundPaint(Color.white);

        // Customize the plot
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        // Customize the renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED); // Temperature
        renderer.setSeriesPaint(1, Color.BLUE); // Wind Speed
        renderer.setSeriesPaint(2, Color.GREEN); // Humidity
        renderer.setSeriesPaint(3, Color.YELLOW); // Pressure
        plot.setRenderer(renderer);

        // Customize the range axis
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // Customize the domain axis
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setVerticalTickLabels(true);

        // Add the chart to a panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        setContentPane(chartPanel);
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
}
