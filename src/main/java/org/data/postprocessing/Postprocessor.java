package org.data.postprocessing;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class to group data based on determined groups / classes / clusters
 */
@Getter
@Setter
public class Postprocessor {

    /**
     * Name of an input file
     */
    String inputFileName;
    /**
     * Name of an input file labeled with groups
     */
    String outputFileName;
    /**
     * True if input file contains header
     */
    Boolean inputHeader;
    /**
     * Header to include in first line of output file (empty by default)
     */
    String outputHeader;
    /**
     * Delimiter that separates columns in input and output files, default is comma (,)
     */
    String delimiter;

    /**
     * InputFileName Setter pointing to Project Resources
     * @param inputFileName Input file name
     */
    public void setInputFileName(String inputFileName) {
        this.inputFileName = "src/main/resources/" + inputFileName;
    }

    /**
     * OutputFileName Setter pointing to Project Resources
     * @param outputFileName Output file name
     */
    public void setOutputFileName(String outputFileName) {
        this.outputFileName = "src/main/resources/" + outputFileName;
    }

    /**
     * Constructor pointing files to Project Resources
     * @param inputFileName Name of an input file
     * @param assignmentsFileName Name of a file with assigned groups for each input line
     * @param outputFileName Name of an input file labeled with groups
     */
    public Postprocessor(String inputFileName, String outputFileName, boolean inputHeader) {
        this.inputFileName = "src/main/resources/" + inputFileName;
        this.outputFileName = "src/main/resources/" + outputFileName;
        this.inputHeader = inputHeader;
        this.delimiter = ",";
        this.outputHeader = "";
    }

    /**
     * Assign groups to each line of input data
     * @param assignmentsFileName Assignments file
     * @throws IOException If opening or reading any of the files fail
     */
    public void AssignData(String assignmentsFileName) throws IOException {
        BufferedReader brInput = new BufferedReader(new FileReader(this.inputFileName));
        BufferedReader brAssignments = new BufferedReader(new FileReader("src/main/resources/" + assignmentsFileName));
        PrintWriter pw = new PrintWriter(this.outputFileName);

        // write output header
        if (!outputHeader.isEmpty()) {
            pw.println(outputHeader);
        }
        // skip input header
        if (this.inputHeader) {
            brInput.readLine();
        }
        String inputLine = brInput.readLine();
        while(inputLine != null) {
            String[] features = inputLine.split(Pattern.quote(delimiter));
            String assignment = brAssignments.readLine();
            List<String> newLineArray = new ArrayList<>(Arrays.asList(features));
            newLineArray.add(assignment);
            pw.println(String.join(delimiter, newLineArray));
            inputLine = brInput.readLine();
        }
        pw.close();
    }

    /**
     * Find averages of selected features for each group
     * @param columnsToAverage Columns of numerical features
     * @throws IOException If opening or reading the input file fails
     */
    public void FindAverages(List<Integer> columnsToAverage) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(this.inputFileName));
        List<String> featureNames = new ArrayList<>();
        // skip input header and save for output
        if (this.inputHeader) {
            String header = br.readLine();
            String[] headerSplit = header.split(Pattern.quote(delimiter));
            for (Integer column : columnsToAverage) {
                featureNames.add(headerSplit[column]);
            }
        }
        String line = br.readLine();
        HashMap<Integer, List<Double>> groups = new HashMap<>();
        int counter = 0;
        while (line != null) {
            List<Double> features = new ArrayList<>();
            String[] lineSplit = line.split(Pattern.quote(delimiter));
            int group = Integer.parseInt(lineSplit[lineSplit.length-1]);
            for (Integer column : columnsToAverage) {
                features.add(Double.parseDouble(lineSplit[column]));
            }
            if (groups.containsKey(group)) {
                List<Double> sumFeatures = IntStream.range(0, features.size())
                        .mapToObj(i -> groups.get(group).get(i) + features.get(i))
                        .collect(Collectors.toList());
                groups.put(group, sumFeatures);
            } else {
                groups.put(group, features);
            }
            counter++;
            line = br.readLine();
        }
        for (Integer key : groups.keySet()) {
            int finalCounter = counter;
            List<Double> avgFeatures = groups.get(key).stream()
                    .map(sum -> sum / finalCounter)
                    .collect(Collectors.toList());
            groups.put(key, avgFeatures);
        }
        printTable(groups, featureNames);
        printFile(groups, featureNames);
    }

    /**
     * Print results to output as a table
     * @param map Groups and features mapped
     * @param header Feature names
     */
    private void printTable(HashMap<Integer, List<Double>> map, List<String> header) {
        System.out.printf("| %-5s |", "GROUP");
        for (String featureName : header) {
            System.out.printf(" %-20s |", featureName + " Average");
        }
        System.out.printf("%n");
        for (Map.Entry<Integer, List<Double>> entry : map.entrySet()) {
            System.out.printf("| %5d |", entry.getKey());
            for (Double feature : entry.getValue()) {
                System.out.printf(" %20.2f |", feature);
            }
            System.out.printf("%n");
        }
    }

    /**
     * Print results to a file as a table
     * @param map Groups and features mapped
     * @param header Feature names
     * @throws FileNotFoundException If the output file is not found
     */
    private void printFile(HashMap<Integer, List<Double>> map, List<String> header) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(this.outputFileName);
        pw.printf("| %-5s |", "GROUP");
        for (String featureName : header) {
            pw.printf(" %-20s |", featureName + " Average");
        }
        pw.printf("%n");
        for (Map.Entry<Integer, List<Double>> entry : map.entrySet()) {
            pw.printf("| %5d |", entry.getKey());
            for (Double feature : entry.getValue()) {
                pw.printf(" %20.2f |", feature);
            }
            pw.printf("%n");
        }
        pw.close();
    }

}
