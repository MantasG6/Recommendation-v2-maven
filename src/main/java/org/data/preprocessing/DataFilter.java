package org.data.preprocessing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.main.RoRecord;

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
     * Delimiter that separates columns in input and output files, default is comma (,)
     */
    String delimiter;
    /**
     * Header for new files (empty by default)
     */
    String header;

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
        this.inputFileName = inputFileName;
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
        this.inputFileName = inputFileName;
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
        pw.println(header);
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
        pw.println(header);
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
        pw.println(header);
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
     * @param indexVoice Index of voice usage column
     * @param indexSMS Index of SMS usage column
     * @throws IOException If opening or reading the input file fails
     */
    public void AggregateData(Integer indexRT, Integer indexMSISDN,
                              Integer indexVoice, Integer indexSMS) throws IOException {
        HashSet<RoRecord> aggregatedData = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        PrintWriter pw = new PrintWriter(outputFileName);
        pw.println(header);
        String line = br.readLine();
        while (line != null) {
            String[] lineSplit = line.split(Pattern.quote(delimiter));
            Integer RT = Integer.valueOf(lineSplit[indexRT]);
            String MSISDN = lineSplit[indexMSISDN];

            // Create a new empty RoRecord for the MSISDN
            RoRecord record = new RoRecord(0L, 0L, 0L, 0L, MSISDN);

            // Set usage
            long usage = usage(RT, lineSplit, indexVoice, indexSMS);

            // Update aggregated data
            if (aggregatedData.contains(record)) {
                record.appendUsage(RT, usage);
            } else {
                record.setUsage(RT, usage);
                aggregatedData.add(record);
            }
            line = br.readLine();
        }
        // Output aggregated data
        for (RoRecord r : aggregatedData) {
            pw.println(r.toString());
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
    private long usage(Integer RT, String[] recordData, Integer indexVoice, Integer indexSMS) {
        switch (RT) {
            case 2:
                return recordData.length <= indexVoice ||
                    recordData[indexVoice].isEmpty() ?
                    0L : Long.parseLong(recordData[indexVoice]);
            case 3:
                return recordData.length <= indexSMS ||
                        recordData[indexSMS].isEmpty() ?
                        0L : Long.parseLong(recordData[indexSMS]);
            default:
                return 0L;
        }
    }
}
