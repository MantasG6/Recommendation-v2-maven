package org.data.preprocessing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.main.RoRecord;
import org.main.Record;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Class containing various filters that can be applied to data
 */
@NoArgsConstructor
@Getter
@Setter
public class DataFilter {

    /**
     * Input file name
     * (Must be located in Project Resources)
     */
    String inputFileName;
    /**
     * Output file name
     * (Will be located in Project Resources)
     */
    String outputFileName;
    /**
     * Delimiter that separates columns in input and output files {@code default = ","}
     */
    String delimiter;
    /**
     * Header for new files {@code default = ""}
     */
    String header;
    /**
     * Aggregated data with all usages added up for each subscriber (grouped by MSISDN)
     */
    HashMap<String,Record> aggregatedData;

    /**
     * InputFileName Setter pointing to Project Resources
     * @param fileName Input file name
     */
    public void setInputFileName(String fileName) {
        this.inputFileName = "src/main/resources/" + fileName;
    }
    /**
     * OutputFileName Setter pointing to Project Resources
     * @param fileName Output file name
     */
    public void setOutputFileName(String fileName) {
        this.outputFileName = "src/main/resources/" + fileName;
    }

    /**
     * Constructor pointing output file to resources
     * @param inputFileName Name of the input file in Project Resources
     * @param outputFileName Name of output file
     * @param delimiter Delimiter that separates columns in input and output files
     */
    public DataFilter(String inputFileName, String outputFileName, String delimiter) {
        this.inputFileName = "src/main/resources/" + inputFileName;
        this.outputFileName = "src/main/resources/" + outputFileName;
        this.delimiter = delimiter;
        this.header = "";
    }

    /**
     * Constructor pointing output file to resources and setting delimiter to default (,)
     * @param inputFileName Name of the input file in Project Resources
     * @param outputFileName Name of output file
     */
    public DataFilter(String inputFileName, String outputFileName) {
        this.inputFileName = "src/main/resources/" + inputFileName;
        this.outputFileName = "src/main/resources/" + outputFileName;
        this.delimiter = ",";
        this.header = "";
    }

    /**
     * Keep the required columns and create a file with filtered columns
     * @param columnsToKeep Columns to keep
     * @throws IOException If opening or reading the input file fails
     */
    public void FilterColumns(List<Integer> columnsToKeep) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        PrintWriter pw = new PrintWriter(outputFileName);
        if (!header.isEmpty()) {
            pw.println(header);
        }
        String line = br.readLine();
        while (line != null) {
            String[] lineSplit = line.split(Pattern.quote(delimiter));
            List<String> newLineArray = new ArrayList<>();
            for (Integer column : columnsToKeep) {
                newLineArray.add(lineSplit[column]);
            }
            pw.println(String.join(delimiter, newLineArray));
            line = br.readLine();
        }
        pw.close();
    }

    /**
     * Filter data with success (2001) result code
     * @param indexRC Index of result code column
     * @throws IOException If opening or reading the input file fails
     */
    public void FilterSuccess(Integer indexRC) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        PrintWriter pw = new PrintWriter(outputFileName);
        if (!header.isEmpty()) {
            pw.println(header);
        }
        String line = br.readLine();
        while (line != null) {
            String[] lineSplit = line.split(Pattern.quote(delimiter));
            if (lineSplit[indexRC].equals("2001")) {
                pw.println(line);
            }
            line = br.readLine();
        }
        pw.close();
    }

    /**
     * Filter data for only voice and sms usage records
     * @param indexRT Record Type
     * @throws IOException If opening or reading the input file fails
     */
    public void FilterVoiceSMS(Integer indexRT) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        PrintWriter pw = new PrintWriter(outputFileName);
        if (!header.isEmpty()) {
            pw.println(header);
        }
        String line = br.readLine();
        while (line != null) {
            String[] lineSplit = line.split(Pattern.quote(delimiter));
            if (lineSplit[indexRT].matches("[23]")) {
                pw.println(line);
            }
            line = br.readLine();
        }
        pw.close();
    }

    /**
     * Aggregate Ro CDR records and group based on usages
     * @param indexRT Index of Record Type column
     * @param indexMSISDN Index of MSISDN column
     * @param indexVoice Index of Voice usage column
     * @param indexSMS Index of SMS usage column
     * @param indexCash Index of Cash usage column
     * @throws IOException If opening or reading the input file fails
     */
    public void AggregateRoData(Integer indexRT, Integer indexMSISDN,
                              Integer indexVoice, Integer indexSMS, Integer indexCash) throws IOException {
        aggregatedData = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        PrintWriter pw = new PrintWriter(outputFileName);
        if (!header.isEmpty()) {
            pw.println(header);
        }
        String line = br.readLine();
        while (line != null) {
            String[] lineSplit = line.split(Pattern.quote(delimiter));
            Integer RT = Integer.valueOf(lineSplit[indexRT]);
            String MSISDN = lineSplit[indexMSISDN];

            // Create a new empty Record for the MSISDN
            Record record = new Record(0L, 0L, 0L, 0L, 0L, MSISDN);

            // Set usage
            long usage = usage(RT, lineSplit, indexVoice, indexSMS, indexCash);

            // Update aggregated data
            if (aggregatedData.containsKey(MSISDN)) {
                record = aggregatedData.get(MSISDN);
                record.appendUsage(RT, usage);
                aggregatedData.put(MSISDN, record);
            } else {
                record.setUsage(RT, usage);
                aggregatedData.put(MSISDN, record);
            }
            line = br.readLine();
        }
        // Output aggregated data
        for (Map.Entry<String, Record> entry : aggregatedData.entrySet()) {
            pw.println(entry.getValue().toString());
        }
        pw.close();
    }

    /**
     * Add Monthly Purchases data to aggregated and filtered Ro data (requires {@link #AggregateRoData(Integer, Integer, Integer, Integer, Integer) Ro Aggregation} to be executed first)
     * @param filename File name of filtered MON CDR containing information about monthly purchases
     * @param indexMSISDN Index of MSISDN column
     * @param indexMP Index of Monthly Purchases column
     * @param containsHeader {@code TRUE} if file contains header in the first line
     * @throws IOException If opening or reading the MON file fails
     */
    public void JoinMONData(String filename, Integer indexMSISDN, Integer indexMP, boolean containsHeader) throws IOException {
        if (aggregatedData == null || aggregatedData.isEmpty()) {
            System.out.println("Aggregation is required!");
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader("src/main/resources/" + filename));
        PrintWriter pw = new PrintWriter(outputFileName);
        if (!header.isEmpty()) {
            pw.println(header);
        }
        // Skip header
        if (containsHeader) {
            br.readLine();
        }
        String line = br.readLine();
        while (line != null) {
            String[] lineSplit = line.split(Pattern.quote(delimiter));
            String MSISDN = lineSplit[indexMSISDN];

            // Set usage
            long MP = lineSplit.length <= indexMP ||
                    lineSplit[indexMP].isEmpty() ?
                    0L : Long.parseLong(lineSplit[indexMP]);

            // Create a new empty RoRecord for the MSISDN
            Record record = new Record(0L, 0L, 0L, 0L, MP, MSISDN);

            // Update aggregated data
            if (aggregatedData.containsKey(MSISDN)) {
                record = aggregatedData.get(MSISDN);
                record.setMonthlyPurchases(record.getMonthlyPurchases() + MP);
                aggregatedData.put(MSISDN, record);
            } else {
                aggregatedData.put(MSISDN, record);
            }
            line = br.readLine();
        }
        // Output joint data
        for (Map.Entry<String, Record> entry : aggregatedData.entrySet()) {
            pw.println(entry.getValue().toString());
        }
        pw.close();
    }

    public void FilterZeroUsage() throws FileNotFoundException {
        if (aggregatedData == null || aggregatedData.isEmpty()) {
            System.out.println("Aggregation is required!");
            return;
        }
        aggregatedData.values().removeIf(Record::isZeroUsage);
        PrintWriter pw = new PrintWriter(outputFileName);
        // Output filtered data
        for (Map.Entry<String, Record> entry : aggregatedData.entrySet()) {
            pw.println(entry.getValue().toString());
        }
        pw.close();
    }

    /**
     * Find usage based on RT
     * @param RT Record Type
     * @param recordData Record data
     * @param indexVoice Index of voice usage column
     * @param indexSMS Index of SMS usage column
     * @return Usage based on RT
     */
    private long usage(Integer RT, String[] recordData, Integer indexVoice, Integer indexSMS, Integer indexCash) {
        switch (RT) {
            case 2:
                return recordData.length <= indexVoice ||
                    recordData[indexVoice].isEmpty() ?
                    0L : Long.parseLong(recordData[indexVoice]);
            case 3:
                return recordData.length <= indexSMS ||
                        recordData[indexSMS].isEmpty() ?
                        0L : Long.parseLong(recordData[indexSMS]);
            case 5:
                return recordData.length <= indexCash ||
                        recordData[indexCash].isEmpty() ?
                        0L : roundCash(recordData[indexCash]);
            default:
                return 0L;
        }
    }

    /**
     * Round the cash feature value
     * @param input Cash feature value
     * @return Rounded cash feature value
     */
    private long roundCash(String input) {
        double preRounded = Double.parseDouble(input);
        return Math.round(preRounded);
    }
}
