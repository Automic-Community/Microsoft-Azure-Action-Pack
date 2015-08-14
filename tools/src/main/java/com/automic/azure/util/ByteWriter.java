package com.automic.azure.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;

/**
 * 
 * Utility class to write bytes to a Stream
 *
 */
public class ByteWriter {

    private static final Logger LOGGER = LogManager.getLogger(ByteWriter.class);
    
    private BufferedOutputStream bos = null;    

    public ByteWriter(OutputStream output) {
        bos = new BufferedOutputStream(output, Constants.IO_BUFFER_SIZE);
    }

    
    /**
     * Method to get associated output stream.
     */
    public OutputStream getStream() {
        return bos;
    }
    
    /**
     * Method to write bytes to Stream
     * 
     * @param bytes
     * @throws AzureException
     */
    public void write(byte[] bytes) throws AzureException {
        write(bytes, 0, bytes.length);
    }

    /**
     * Method to write specific part of byte array to Stream
     * 
     * @param bytes
     * @param offset
     * @param length
     * @throws AzureException
     */
    public void write(byte[] bytes, int offset, int length) throws AzureException {
        try {
            bos.write(bytes, offset, length);
        } catch (IOException e) {
            LOGGER.error(ExceptionConstants.UNABLE_TO_WRITEFILE, e);
            throw new AzureException(ExceptionConstants.UNABLE_TO_WRITEFILE, e);
        }
    }

    /**
     * Method to write a String to stream
     * 
     * @param field
     * @throws AzureException
     */
    public void write(String field) throws AzureException {
        write(field.getBytes());
    }

    /**
     * Method to write a new line character to stream
     * 
     * @throws AzureException
     */
    public void writeNewLine() throws AzureException {
        write(System.lineSeparator());
    }

    /**
     * Close the underlying stream
     * 
     * @throws AzureException
     */
    public void close() throws AzureException {
        try {
            if (bos != null) {
                bos.close();
            } else {
                LOGGER.error("Stream null!! Unable to close stream");
                throw new AzureException(ExceptionConstants.UNABLE_TO_CLOSE_STREAM);
            }

        } catch (IOException e) {
            LOGGER.error(ExceptionConstants.UNABLE_TO_CLOSE_STREAM, e);
            throw new AzureException(ExceptionConstants.UNABLE_TO_CLOSE_STREAM, e);
        }
    }

    /**
     * Method to flush to stream
     * 
     * @throws AzureException
     */
    public void flush() throws AzureException {
        if (bos != null) {
            try {
                bos.flush();
            } catch (IOException e) {
                LOGGER.error(ExceptionConstants.UNABLE_TO_FLUSH_STREAM, e);
                throw new AzureException(ExceptionConstants.UNABLE_TO_FLUSH_STREAM, e);
            }
        }
    }

}
