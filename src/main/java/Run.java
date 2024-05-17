import org.data.postprocessing.Postprocessor;
import org.data.preprocessing.DataFilter;
import org.ml.examples.Clusterer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Arrays;

public class Run {
    public static void main(String[] args) throws Exception {
//        DataPreprocessing();
//        FindOptimalK();
        GetAssignments();
        DataPostprocessing();
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
        DataFilter roFilter = new DataFilter("RoMerged.csv", "RoFiltered_1.csv", "|");
        roFilter.FilterColumns(Arrays.asList(46, 0, 6, 26, 43, 33));
        roFilter.setInputFileName("RoFiltered_1.csv");
        roFilter.setOutputFileName("RoFiltered_2.csv");
        roFilter.FilterSuccess(0);
        roFilter.setInputFileName("RoFiltered_2.csv");
        roFilter.setOutputFileName("RoFiltered_3.csv");
        roFilter.FilterVoiceSMS(1);
        roFilter.setInputFileName("RoFiltered_2.csv");
        roFilter.setOutputFileName("RoAggregated.csv");
        roFilter.AggregateData(1,2,3,4, 5);
//        DataFilter roFilter = new DataFilter("RoAggregated.csv", "RoFinal.csv", ",");
        roFilter.setInputFileName("RoAggregated.csv");
        roFilter.setOutputFileName("RoFinal.csv");
        roFilter.setDelimiter(",");
        roFilter.setHeader("Voice,SMS,Cash");
        roFilter.FilterColumns(Arrays.asList(1,2,3));
    }

    public static void FindOptimalK() throws Exception {
        Clusterer clusterer = new Clusterer("RoFinal.csv");
        clusterer.findOptimalK(10, true);
    }

    public static void GetAssignments() throws Exception {
        Clusterer clusterer = new Clusterer("RoFinal.csv");
        clusterer.setNumClusters(4);
        clusterer.Clusterize();
        int[] assignments = clusterer.getAssignments();
        PrintWriter pw = new PrintWriter("src/main/resources/assignments.csv");
        for (int assignment : assignments) {
            pw.println(assignment);
        }
        pw.close();
    }

    public static void DataPostprocessing() throws IOException {
        Postprocessor postprocessor = new Postprocessor("RoAggregated.csv", "RoGrouped.csv", false);
        postprocessor.setOutputHeader("MSISDN,VoiceUsage,SMSUsage,CashUsage,Group");
        postprocessor.AssignData("assignments.csv");
//        Postprocessor postprocessor = new Postprocessor("RoGrouped.csv", "RoResult.csv", true);
        postprocessor.setInputFileName("RoGrouped.csv");
        postprocessor.setOutputFileName("RoAverages.csv");
        postprocessor.setInputHeader(true);
        postprocessor.FindAverages(Arrays.asList(1, 2, 3));
        postprocessor.setOutputFileName("RoMax.csv");
        postprocessor.FindMinMax(Arrays.asList(1, 2, 3), 1);
        postprocessor.setOutputFileName("RoMin.csv");
        postprocessor.FindMinMax(Arrays.asList(1, 2, 3), 0);
    }
}