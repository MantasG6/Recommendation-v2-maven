package org.data.preprocessing;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import java.awt.*;

/**
 * Correlation matrix visualization tool
 */
public class CorrelationMatrixHeatmap extends ApplicationFrame {

    /**
     * Visualization tool constructor
     * @param title The graph title
     * @param correlationMatrix Correlation matrix values
     */
    public CorrelationMatrixHeatmap(String title, double[][] correlationMatrix) {
        super(title);
        XYZDataset dataset = createDataset(correlationMatrix);
        JFreeChart chart = createChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
    }

    /**
     * Create matrix dataset suitable for {@link JFreeChart}
     * @param correlationMatrix Correlation matrix values
     * @return Dataset suitable for {@link JFreeChart}
     */
    private XYZDataset createDataset(double[][] correlationMatrix) {
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        int n = correlationMatrix.length;
        double[] xValues = new double[n * n];
        double[] yValues = new double[n * n];
        double[] zValues = new double[n * n];
        int index = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                xValues[index] = i;
                yValues[index] = j;
                zValues[index] = correlationMatrix[i][j];
                index++;
            }
        }
        dataset.addSeries("Correlation", new double[][]{xValues, yValues, zValues});
        return dataset;
    }

    /**
     * Create the heatmap
     * @param dataset Source dataset for the heatmap (from {@link #createDataset(double[][]) Create Dataset})
     * @return Heatmap chart
     */
    private JFreeChart createChart(XYZDataset dataset) {
        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        XYBlockRenderer renderer = new XYBlockRenderer();
        PaintScale scale = new PaintScale() {
            private final Color lowColor = Color.BLUE;
            private final Color highColor = Color.RED;

            @Override
            public double getLowerBound() {
                return -1.0;
            }

            @Override
            public double getUpperBound() {
                return 1.0;
            }

            @Override
            public Paint getPaint(double value) {
                float ratio = (float) ((value + 1) / 2);
                return new Color(
                        (int) (lowColor.getRed() * (1 - ratio) + highColor.getRed() * ratio),
                        (int) (lowColor.getGreen() * (1 - ratio) + highColor.getGreen() * ratio),
                        (int) (lowColor.getBlue() * (1 - ratio) + highColor.getBlue() * ratio)
                );
            }
        };
        renderer.setPaintScale(scale);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.black);
        plot.setRangeGridlinePaint(Color.black);
        plot.setInsets(new RectangleInsets(5, 5, 5, 5));

        JFreeChart chart = new JFreeChart("Correlation Matrix Heatmap", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        PaintScaleLegend legend = new PaintScaleLegend(scale, new NumberAxis("Correlation"));
        legend.setPosition(RectangleEdge.RIGHT);
        chart.addSubtitle(legend);
        return chart;
    }
}
