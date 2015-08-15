package com.automic.azure.util;

import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.exception.AzureException;

/**
 * This class writes content to standard console
 *
 * @author anuragupadhyay
 *
 */
public final class ConsoleWriter {

    private static final Logger LOGGER = LogManager.getLogger(ConsoleWriter.class);
    private static final ByteWriter WRITER = new ByteWriter(System.out);

    private ConsoleWriter() {
    }

    /**
     * Method to write object to console
     *
     * @param content
     */
    public static void write(Object content) {
        String temp = content != null ? content.toString() : "null";
        try {
            WRITER.write(temp);
        } catch (AzureException ae) {
            LOGGER.error(ae.getMessage());
        }
    }

    /**
     * Method to write a newline to console
     */
    public static void newLine() {
        write(System.lineSeparator());
    }

    /**
     * Method to write an Object to console and followed by newline.
     *
     * @param content
     */
    public static void writeln(Object content) {
        write(content);
        newLine();
    }

    /**
     * Method to get associated output stream.
     */
    public static OutputStream getStream() {
        return WRITER.getStream();
    }

    /**
     * Method to flush to console
     */
    public static void flush() {
        try {
            WRITER.flush();
        } catch (AzureException ae) {
            LOGGER.error(ae.getMessage());
        }
    }

}
