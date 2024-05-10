import org.data.preprocessing.DataFilter;
import org.data.preprocessing.DataMerger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

public class Run {
    public static void main(String[] args) throws Exception {
        DataFilter roFilter = new DataFilter("RoFiltered_2.csv", "RoAggregated.csv", "|");
        roFilter.AggregateData(1, 4);
    }

    public static void DataPreprocessing() throws URISyntaxException, IOException {
        DataMerger merger = new DataMerger("/MON/", "MONMerged.csv", true);
        merger.Merge();
        merger.setDirectoryPath("/MGR/");
        merger.setOutputFilePath("MGRMerged.csv");
        merger.Merge();
        merger.setDirectoryPath("/Ro/");
        merger.setOutputFilePath("RoMerged.csv");
        merger.setContainsHeader(false);
        merger.Merge();
        DataFilter roFilter = new DataFilter("RoMerged.csv", "RoFiltered_1.csv", "|");
        roFilter.FilterColumns(Arrays.asList(0, 6, 44, 46, 54, 55));
        roFilter.setInputFileName("RoFiltered_1.csv");
        roFilter.setOutputFileName("RoFiltered_2.csv");
        roFilter.FilterSuccess(3);
        roFilter.setInputFileName("RoFiltered_2.csv");
        roFilter.setOutputFileName("RoAggregated.csv");
        roFilter.AggregateData(1, 4);
    }
}