package org.ml.examples;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Standardize;

import javax.swing.JFrame;

/**
 * Class containing ML methods for KMeans clustering
 */
@Setter
@Getter
@AllArgsConstructor
public class Clusterer {

    /**
     * Input data file name
     */
    String inputFileName;

    /**
     * Random numbers seed to keep the results consistent {@code default = 10}
     */
    int seed;

    /**
     * Choose to preserve or randomize instances order {@code default = true}
     */
    boolean preserveInstancesOrder;

    /**
     * Choose number of clusters {@code default = 3}
     */
    int numClusters;

    /**
     * Assigned classes to each row of input data
     */
    int[] assignments;

    /**
     * Standardize feature values {@code default = false}
     */
    boolean standardize;

    /**
     * Constructs a new Clusterer with default values
     */
    public Clusterer(String inputFileName) {
        this.inputFileName = inputFileName;
        this.seed = 10;
        this.preserveInstancesOrder = true;
        this.numClusters = 3;
        this.standardize = false;
    }

    /**
     * Clusterize data in chosen amount of clusters
     * @throws Exception If provided path does not meet {@link java.net.URI} requirements or provided wrong K value
     */
    public void Clusterize() throws Exception {
        Instances data = getData(this.inputFileName);

        if (this.standardize) {
            Standardize standardize = new Standardize();
            standardize.setInputFormat(data);
            data = Filter.useFilter(data, standardize);
        }

        SimpleKMeans kMeans = new SimpleKMeans();
        kMeans.setSeed(this.seed);
        kMeans.setPreserveInstancesOrder(this.preserveInstancesOrder);
        kMeans.setNumClusters(this.numClusters);
        kMeans.buildClusterer(data);
        assignments = kMeans.getAssignments();
    }

    /**
     * Finds and displays optimal K value for KMeans clusterization method using distortion elbow method
     * @param lengthK amount of K values to check
     * @param visualize results visualization
     * @throws Exception If wrong K value is provided
     */
    public void findOptimalK(int lengthK, boolean visualize) throws Exception {
        Instances data = getData(this.inputFileName);

        if (this.standardize) {
            Standardize standardize = new Standardize();
            standardize.setInputFormat(data);
            data = Filter.useFilter(data, standardize);
        }

        HashMap<Integer, Double> distortions = new HashMap<>();

        for (int k = 1; k < lengthK; k++) {
            // Building and fitting the model
            SimpleKMeans kMeans = new SimpleKMeans();
            kMeans.setSeed(this.seed);
            kMeans.setPreserveInstancesOrder(this.preserveInstancesOrder);
            kMeans.setNumClusters(k);
            kMeans.buildClusterer(data);

            double distortion = 0.0;
            for (int i = 0; i < data.numInstances(); i++) {
                distortion += minEuclideanDistance(data.get(i), kMeans.getClusterCentroids());
            }
            distortion /= data.numInstances();

            distortions.put(k, distortion);
        }

        for (Map.Entry<Integer, Double> entry : distortions.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        if (visualize) {
            XYSeries series = new XYSeries("Distortion");
            for (int k = 1; k < lengthK; k++) {
                series.add(k, distortions.get(k));
            }
            XYDataset dataset = new XYSeriesCollection(series);

            // Create the chart
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "The Elbow Method using Distortion",
                    "Values of K",
                    "Distortion",
                    dataset,
                    PlotOrientation.VERTICAL,
                    false, true, false
            );

            // Display the chart
            ChartPanel chartPanel = new ChartPanel(chart);
            JFrame frame = new JFrame("Elbow Method Plot");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(chartPanel);
            frame.pack();
            frame.setVisible(true);
        }
    }

    /**
     * Minimum distance between instance and a list of instances
     * @param point instance
     * @param centers list of instances
     * @return minimum distance
     */
    private double minEuclideanDistance(Instance point, @NotNull Instances centers) {
        double minDistance = Double.MAX_VALUE;
        for (Instance center : centers) {
            double distance = 0.0;
            for (int i = 0; i < point.numValues(); i++) {
                distance += Math.pow(point.value(i) - center.value(i), 2);
            }
            distance = Math.sqrt(distance);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }


    /**
     * Get data in correct format for Weka
     * @param filename path to file
     * @return data in Instances format
     * @throws IOException If provided path is wrong or does not exist
     */
    private Instances getData(String filename) throws IOException {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("src/main/resources/" + filename));
        return loader.getDataSet();
    }

}
