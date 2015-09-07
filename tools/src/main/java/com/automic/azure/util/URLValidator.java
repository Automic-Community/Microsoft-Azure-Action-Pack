package com.automic.azure.util;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;

/**
 * Utility class to validate URLs
 * 
 * 
 */
public final class URLValidator {

    private static final Logger LOGGER = LogManager.getLogger(URLValidator.class);

    private URLValidator() {
    }

    /**
     * Method to validate Azure URL. It validates connection protocol, Port numbers if specified. Also throws a
     * {@link MalformedURLException} if URL is invalid.
     * 
     * @param azureUrl
     *            String representing Azure URL
     * @return true if URL is valid else false
     */
    public static boolean validateURL(String azureUrl) throws AzureException {
        URI uri = null;

        try {
            uri = URI.create(azureUrl);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error while validating azure url :: ", e);
            throw new AzureException(String.format("%s ,%s",
                    String.format(ExceptionConstants.INVALID_AZURE_URL, azureUrl), e.getMessage()));

        }

        return (uri != null);
    }
}
