/**
 * 
 */
package com.automic.azure.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class which denotes
 *
 */
public class AgentSystemFile {

    private static final Logger LOGGER = LogManager.getLogger(AgentSystemFile.class);

    /**
     * system File
     */
    private final Path systemFile;

    /**
     * Size of the File
     */
    private long fileSize;

    /**
     * 
     */
    public AgentSystemFile(String filePath) {
        //
        this.systemFile = Paths.get(filePath);
        try {
            this.fileSize = Files.size(this.systemFile);
        } catch (IOException e) {
            LOGGER.error("Error while calculating the file size for " + filePath);
            this.fileSize = 0;
        }
    }

    public boolean fileExists() {
        if (Files.exists(systemFile)) {
            return true;
        }
        return false;
    }

    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(systemFile);
    }

}
