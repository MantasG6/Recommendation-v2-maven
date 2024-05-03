import org.data.preprocessing.DataMerger;

public class Run {
    public static void main(String[] args) throws Exception {
        DataMerger merger = new DataMerger("/MON/", "MONMerged.csv", true);
        merger.Merge();
    }
}