package org.data.preprocessing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
public class DataMerger {

    @Getter @Setter
    String directoryPath;

    public void Merge() throws URISyntaxException {
        File dir = new File(Objects.requireNonNull(DataMerger.class.getResource(directoryPath)).toURI());
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                // Do something with child
            }
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }
    }
}
