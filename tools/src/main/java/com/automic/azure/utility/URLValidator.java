package com.automic.azure.utility;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;

/**
 * Utility class to validate URLs
 * 
 *
 */
public final class URLValidator {

  private static final Logger LOGGER = LogManager.getLogger(URLValidator.class);

  private static final int PORT_RANGE_START = 0;
  private static final int PORT_RANGE_END = 65535;

  private URLValidator() {
  }

  /**
   * Method to validate Azure URL. It validates connection protocol, Port numbers if specified. Also
   * throws a {@link MalformedURLException} if URL is invalid.
   * 
   * @param azureUrl
   *          String representing Azure URL
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

    return (uri != null) && checkPort(uri.getPort(), azureUrl) && checkProtocol(uri.getScheme());
  }

  private static boolean checkPort(int port, String azureUrl) {

    return (port == -1 && azureUrl.indexOf("-1") == -1) || port >= PORT_RANGE_START
        || port <= PORT_RANGE_END;

  }

  private static boolean checkProtocol(String protocol) {
    return "http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol);
  }

}
