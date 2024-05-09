import org.data.preprocessing.DataFilter;
import org.data.preprocessing.DataMerger;

import java.util.Arrays;

public class Run {
    public static void main(String[] args) throws Exception {
//        DataMerger merger = new DataMerger("/MON/", "MONMerged.csv", true);
//        merger.Merge();
        DataFilter roFilter = new DataFilter("RoMerged.csv", "RoFiltered_1.csv", "|");
        roFilter.FilterColumns(Arrays.asList(0, 6, 44, 46, 54, 55));
        roFilter.setInputFileName("RoFiltered_1.csv");
        roFilter.setOutputFileName("RoFiltered_2.csv");
        roFilter.FilterSuccess(3);
//        System.out.println("test");
    }
}