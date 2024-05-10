package org.data.preprocessing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    }

    /**
     * Constructor pointing to output file and setting delimiter to default (,)
     * @param inputFileName Name of the input file in Project Resources
     * @param outputFileName Name of output file
     */
    public DataFilter(String inputFileName, String outputFileName) {
        this.inputFileName = inputFileName;
        this.outputFileName = "src/main/resources/" + outputFileName;
        this.delimiter = ",";
    }

    /**
     * Main method which keeps the required columns and creates a file with filtered columns
     * @param columnsToKeep Columns to keep
     * @throws IOException If opening or reading the input file fails
     */
    public void FilterColumns(List<Integer> columnsToKeep) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(getFile(inputFileName)));
        PrintWriter pw = new PrintWriter(outputFileName);
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
     * Get File object from project resources with provided name
     * @param fileName File name in project resources
     * @return {@link java.io.File} object
     */
    File getFile(String fileName) {
        return new File("src/main/resources/" + fileName);
    }

    /**
     * Filter data with success (2001) result code
     * @param indexRC Index of result code column
     * @throws IOException If opening or reading the input file fails
     */
    public void FilterSuccess(Integer indexRC) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(getFile(inputFileName)));
        PrintWriter pw = new PrintWriter(outputFileName);
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

    public void AggregateData(Integer indexMSISDN, Integer indexAllowance) throws IOException {
        Map<String, Integer> aggregatedData = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(getFile(inputFileName)));
        PrintWriter pw = new PrintWriter(outputFileName);
        String line = br.readLine();
        while (line != null) {
            String[] lineSplit = line.split(Pattern.quote(delimiter));
            String MSISDN = lineSplit[indexMSISDN];
            Integer allowance = lineSplit.length <= indexAllowance ||
                    lineSplit[indexAllowance].isEmpty() ?
                    0 : Integer.parseInt(lineSplit[indexAllowance]);
            // Update aggregated data
            if (aggregatedData.containsKey(MSISDN)) {
                aggregatedData.compute(MSISDN, (k, currentAllowance) -> currentAllowance + allowance);
            } else {
                aggregatedData.put(MSISDN, allowance);
            }
            line = br.readLine();
        }
        // Output aggregated data
        for (Map.Entry<String, Integer> entry : aggregatedData.entrySet()) {
            pw.println(entry.getKey() + delimiter + entry.getValue());
        }
        pw.close();
    }
}
