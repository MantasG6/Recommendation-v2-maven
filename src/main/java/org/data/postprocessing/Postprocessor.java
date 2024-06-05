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
     * Header to include in first line of output file {@code default = ""}
     */
    String outputHeader;
    /**
     * Delimiter that separates columns in input and output files {@code default = ","}
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
     * @param outputFileName Name of an input file labeled with groups
     * @param inputHeader {@code TRUE} if input contains header in the first line
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
        printTable(groups, featureNames, "Avg");
        printFile(groups, featureNames, "Avg");
    }

    /**
     * Find minimum or maximum values of selected features for each group
     * @param columnsToMinMax Columns of numerical features
     * @param identifier {@code 0} to find minimum values, {@code 1} to find maximum values
     * @throws IOException If opening or reading the input file fails
     */
    public void FindMinMax(List<Integer> columnsToMinMax, Integer identifier) throws IOException {
        String statName = identifier == 0 ? "Min" : "Max";
        BufferedReader br = new BufferedReader(new FileReader(this.inputFileName));
        List<String> featureNames = new ArrayList<>();
        // skip input header and save for output
        if (this.inputHeader) {
            String header = br.readLine();
            String[] headerSplit = header.split(Pattern.quote(delimiter));
            for (Integer column : columnsToMinMax) {
                featureNames.add(headerSplit[column]);
            }
        }
        String line = br.readLine();
        HashMap<Integer, List<Long>> groups = new HashMap<>();
        while (line != null) {
            List<Long> features = new ArrayList<>();
            String[] lineSplit = line.split(Pattern.quote(delimiter));
            int group = Integer.parseInt(lineSplit[lineSplit.length-1]);
            for (Integer column : columnsToMinMax) {
                features.add(Long.parseLong(lineSplit[column]));
            }
            if (groups.containsKey(group)) {
                List<Long> maxFeatures = IntStream.range(0, features.size())
                        .mapToObj(i ->
                        {
                            if (identifier == 0) {
                                return Math.min(groups.get(group).get(i), features.get(i));
                            } else {
                                return Math.max(groups.get(group).get(i), features.get(i));
                            }
                        })
                        .collect(Collectors.toList());
                groups.put(group, maxFeatures);
            } else {
                groups.put(group, features);
            }
            line = br.readLine();
        }
        printTable(groups, featureNames, statName);
        printFile(groups, featureNames, statName);
    }

    public void FindAmounts() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(this.inputFileName));
        List<String> featureNames = List.of("Records");
        // skip input header
        if (this.inputHeader) {
            br.readLine();
        }
        String line = br.readLine();
        HashMap<Integer, List<Integer>> groups = new HashMap<>();
        while (line != null) {
            List<Integer> features = new ArrayList<>();
            String[] lineSplit = line.split(Pattern.quote(delimiter));
            int group = Integer.parseInt(lineSplit[lineSplit.length-1]);
            if (groups.containsKey(group)) {
                int count = groups.get(group).get(0) + 1;
                features.add(count);
                groups.put(group, features);
            } else {
                features.add(1);
                groups.put(group, features);
            }
            line = br.readLine();
        }
        printTable(groups, featureNames, "Amount");
        printFile(groups, featureNames, "Amount");
    }

    /**
     * Print results to output as a table
     * @param map Groups and features mapped
     * @param header Feature names
     * @param statName Statistic name
     */
    private void printTable(HashMap<Integer, ? extends  List<? extends Number>> map, List<String> header, String statName) {
        System.out.printf("| %-5s |", "GROUP");
        for (String featureName : header) {
            System.out.printf(" %-20s |", featureName + " " + statName);
        }
        System.out.printf("%n");
        for (Map.Entry<Integer,? extends List<? extends Number>> entry : map.entrySet()) {
            System.out.printf("| %5d |", entry.getKey());
            for (Number feature : entry.getValue()) {
                if (feature instanceof Double) {
                    System.out.printf(" %20.5f |", feature);
                } else if (feature instanceof Long) {
                    System.out.printf(" %20d |", feature);
                } else if (feature instanceof Integer) {
                    System.out.printf(" %20d |", feature);
                }
            }
            System.out.printf("%n");
        }
    }

    /**
     * Print results to a file as a table
     * @param map Groups and features mapped
     * @param header Feature names
     * @param statName Statistic name
     * @throws FileNotFoundException If the output file is not found
     */
    private void printFile(HashMap<Integer, ? extends  List<? extends Number>> map, List<String> header, String statName) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(this.outputFileName);
        pw.printf("| %-5s |", "GROUP");
        for (String featureName : header) {
            pw.printf(" %-20s |", featureName + " " + statName);
        }
        pw.printf("%n");
        for (Map.Entry<Integer,? extends List<? extends Number>> entry : map.entrySet()) {
            pw.printf("| %5d |", entry.getKey());
            for (Number feature : entry.getValue()) {
                if (feature instanceof Double) {
                    pw.printf(" %20.5f |", feature);
                } else if (feature instanceof Long){
                    pw.printf(" %20d |", feature);
                } else if (feature instanceof Integer) {
                    pw.printf(" %20d |", feature);
                }
            }
            pw.printf("%n");
        }
        pw.close();
    }

}
