import org.data.postprocessing.Postprocessor;
import org.data.preprocessing.CorrelationMatrixHeatmap;
import org.data.preprocessing.DataFilter;
import org.ml.examples.Clusterer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Arrays;

public class Run {
    public static void main(String[] args) throws Exception {
        DataPreprocessing();
//        FindOptimalK();
//        GetAssignments();
//        DataPostprocessing();
    }

    /**
     * Preprocess data for model learning
     * @throws URISyntaxException If provided path for merger does not meet {@link java.net.URI} requirements
     * @throws IOException If opening or reading the input file fails
     */
    public static void DataPreprocessing() throws URISyntaxException, IOException {
//        DataMerger merger = new DataMerger("/MON/", "MONMerged.csv", true);
//        merger.Merge();
//        merger.setDirectoryPath("/MGR/");
//        merger.setOutputFilePath("MGRMerged.csv");
//        merger.Merge();
//        merger.setDirectoryPath("/Ro/");
//        merger.setOutputFilePath("RoMerged.csv");
//        merger.setContainsHeader(false);
//        merger.Merge();
        DataFilter filter = new DataFilter("RoMerged.csv", "RoFiltered_1.csv", "|");
        // Filter RT, MSISDN, Voice, SMS, Cash usage columns
//        filter.FilterColumns(Arrays.asList(46, 0, 6, 26, 43, 33));

//        filter.setInputFileName("RoFiltered_1.csv");
//        filter.setOutputFileName("RoFiltered_2.csv");
//        filter.FilterSuccess(0);

//        filter.setInputFileName("RoFiltered_2.csv");
//        filter.setOutputFileName("RoAggregated.csv");
//        filter.AggregateRoData(1,2,3,4,5);
//
//        filter.setInputFileName("MONMerged.csv");
//        filter.setOutputFileName("MONFiltered.csv");
//        filter.setDelimiter("|");
//        filter.FilterColumns(Arrays.asList(2, 8));
//
//        filter.setOutputFileName("Joint.csv");
//        filter.JoinMONData("MONFiltered.csv",0,1,true);
//
//        filter.setOutputFileName("JointFiltered.csv");
//        filter.FilterZeroUsage();
//
//        filter.setInputFileName("JointFiltered.csv");
//        filter.setOutputFileName("Final.csv");
//        filter.setDelimiter(",");
//        filter.setHeader("Voice,SMS,MonthlyPurchases");
//        filter.FilterColumns(Arrays.asList(1,2,4));

//        // Check correlation
//        filter.setInputFileName("Final.csv");
//        double[][] correlationMatrix = filter.CorrelationMatrix(true, true);
//        CorrelationMatrixHeatmap heatmap = new CorrelationMatrixHeatmap("Usage correlation heatmap", correlationMatrix);
//        heatmap.pack();
//        heatmap.setVisible(true);

    }

    public static void FindOptimalK() throws Exception {
        Clusterer clusterer = new Clusterer("Final.csv");
        clusterer.setStandardize(true);
        clusterer.findOptimalK(15, true);
    }

    public static void GetAssignments() throws Exception {
        Clusterer clusterer = new Clusterer("Final.csv");
        clusterer.setStandardize(true);
        clusterer.setNumClusters(6);
        clusterer.Clusterize();
        int[] assignments = clusterer.getAssignments();
        PrintWriter pw = new PrintWriter("src/main/resources/assignments.csv");
        for (int assignment : assignments) {
            pw.println(assignment);
        }
        pw.close();
    }

    public static void DataPostprocessing() throws IOException {
        Postprocessor postprocessor = new Postprocessor("JointFiltered.csv", "Grouped.csv", false);
        postprocessor.setOutputHeader("MSISDN,VoiceUsage,SMSUsage,CashUsage,MonthlyPurchases,Group");
        postprocessor.AssignData("assignments.csv");

        postprocessor.setInputFileName("Grouped.csv");
        postprocessor.setOutputFileName("Averages.csv");
        postprocessor.setInputHeader(true);
        postprocessor.FindAverages(Arrays.asList(1, 2, 3, 4));

        postprocessor.setOutputFileName("Max.csv");
        postprocessor.FindMinMax(Arrays.asList(1, 2, 3, 4), 1);

        postprocessor.setOutputFileName("Min.csv");
        postprocessor.FindMinMax(Arrays.asList(1, 2, 3, 4), 0);

        postprocessor.setOutputFileName("Amounts.csv");
        postprocessor.FindAmounts();
    }
}