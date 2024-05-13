package org.data.preprocessing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 *  Class to merge multiple file contents into a single file
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DataMerger {

    /**
     * Path to directory with all the files to merge
     */
    String directoryPath;

    /**
     * Output file path
     */
    String outputFilePath;

    /**
     * True if files contain header in the first line
     */
    Boolean containsHeader;

    /**
     * Merge all the files in a provided directory
     * @throws URISyntaxException If provided path does not meet {@link java.net.URI} requirements
     * @throws IOException If file in the directory is not found or file cannot be read
     */
    public void Merge() throws URISyntaxException, IOException {
        File dir = new File(Objects.requireNonNull(DataMerger.class.getResource(directoryPath)).toURI());
        File[] directoryListing = dir.listFiles();
        if (directoryListing == null) {
            System.out.println(directoryPath + " is not a correct directory");
            return;
        }

        PrintWriter pw = new PrintWriter("src/main/resources/" + outputFilePath);

        for (int i = 0; i < directoryListing.length; i++) {
            System.out.println("Merging " + directoryListing[i]);
            BufferedReader br = new BufferedReader(new FileReader(directoryListing[i]));
            String line = br.readLine();
            // Leave the header from the first file and skip from all the following
            if (containsHeader && i != 0) {
                line = br.readLine();
            }
            while (line != null) {
                pw.println(line);
                line = br.readLine();
            }
        }
        pw.close();
        System.out.println("Merging " + directoryPath +
                " to " + outputFilePath + " Completed");
    }
}
