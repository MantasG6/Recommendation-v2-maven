package org.ml.examples;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.JFrame;

@Setter
@Getter
@AllArgsConstructor
public class Clusterer {

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
     * Constructs a new Clusterer with default values
     */
    public Clusterer() {
        this.seed = 10;
        this.preserveInstancesOrder = true;
        this.numClusters = 3;
    }

    /**
     * Clusterize data in chosen amount of clusters
     * @throws Exception If provided path does not meet {@link java.net.URI} requirements or provided wrong K value
     */
    public void Clusterize() throws Exception {
        Instances data = getData("/data_sample.arff");
        System.out.println(data.numInstances() + " Number of instances");
        Instances dataTrim = new Instances(data, 1000);
        for (int i = 0; i < 10000; i++) {
            dataTrim.add(data.get(i));
        }
        findOptimalK(dataTrim, 20, true);
    }

    /**
     * Finds and displays optimal K value for KMeans clusterization method using distortion elbow method
     * @param data data to clusterize
     * @param lengthK amount of K values to check
     * @param visualize results visualization
     * @throws Exception If wrong K value is provided
     */
    private void findOptimalK(Instances data, int lengthK, boolean visualize) throws Exception {

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
     * @throws URISyntaxException If provided path does not meet {@link java.net.URI} requirements
     */
    @NotNull
    private Instances getData(String filename) throws IOException, URISyntaxException {
        File file = new File(Objects.requireNonNull(Clusterer.class.getResource(filename)).toURI());
        BufferedReader inputReader = new BufferedReader(new FileReader(file));
        Instances instances = new Instances(inputReader);
        inputReader.close();
        return instances;
    }

}
