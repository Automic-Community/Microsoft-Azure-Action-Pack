/**
 * 
 */
package com.automic.azure.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;

import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;
import com.automic.azure.utility.CommonUtil;
import com.automic.azure.utility.ConsoleWriter;
import com.sun.jersey.api.client.ClientHandlerException;

import static com.automic.azure.utility.CommonUtil.print;

/**
 * Main Class is the insertion point of Azure interaction api when called from AE implementation. It
 * delegates the parameters to appropriate action and returns a response code based on output of
 * action.
 * 
 * Following response code are returned by java program 0 - Successful response from Azure API 1 -
 * An exception occurred/Error in response from Azure API 2 - Connection timeout while calling Azure
 * API
 * 
 */
public final class AzureClient {

  private static final Logger LOGGER = LogManager.getLogger(AzureClient.class);

  private static final int RESPONSE_OK = 0;
  private static final int RESPONSE_NOT_OK = 1;
  private static final int RESPONSE_CONNECT_TIMEOUT = 2;

  private static final String RESPONSE_ERROR = "ERROR";

  private static final String ERRORMSG = "Please check the input parameters. For more details refer java logs";

  private static final String CONNECTION_TIMEOUT = "Connection Timeout.";
  private static final String UNABLE_TO_CONNECT = "Unable to connect.";

  private AzureClient() {
  }

  /**
   * Main method which will start the execution of an action on Azure. This method will call the
   * AzureClientHelper class which will trigger the execution of specific action and then if action
   * fails this main method will handle the failed scenario and print the error message and system
   * will exit with the respective response code.
   * 
   * @param args
   *          array of Arguments
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      print("No arguments received...", LOGGER, StandardLevel.OFF);
      ConsoleWriter.flush();
      System.exit(1);
    }

    int responseCode = RESPONSE_NOT_OK;

    try {
      AzureClientHelper.executeAction(args);
      responseCode = RESPONSE_OK;
    } catch (ClientHandlerException e) {
      LOGGER.error("Action  FAILED ,possible reason :: ", e);
      responseCode = clientHandlerExceptionHandling(e);
    } catch (AzureException e) {
      print(CommonUtil.formatMessage(RESPONSE_ERROR, e.getMessage()), LOGGER, StandardLevel.OFF);
    } catch (Exception e) {
      LOGGER.error("Action  FAILED ,possible reason :: ", e);
      print(CommonUtil.formatMessage(RESPONSE_ERROR, ExceptionConstants.GENERIC_ERROR_MSG), LOGGER,
          StandardLevel.OFF);
    }

    if (responseCode != RESPONSE_OK) {
      print(CommonUtil.formatMessage(RESPONSE_ERROR, ERRORMSG), LOGGER, StandardLevel.OFF);
    }

    LOGGER.info("@@@@@@@ Execution ends for action  with response code : " + responseCode);
    ConsoleWriter.flush();
    System.exit(responseCode);

  }

  /**
   * Method that returns response code based on whether call to Azure API resulted in connection
   * timeout or any other exception. A response code of 2 means connection timeout else for all
   * other exceptions the response code is 1.
   * 
   * @param e
   *          instance of {@link ClientHandlerException}
   * @return the response code
   */
  private static int clientHandlerExceptionHandling(ClientHandlerException e) {
    int responseCode = RESPONSE_NOT_OK;
    Throwable th = e.getCause();

    if (th != null) {
      if (th instanceof java.net.SocketTimeoutException) {
        print(CommonUtil.formatMessage(RESPONSE_ERROR, CONNECTION_TIMEOUT), LOGGER,
            StandardLevel.OFF);
        responseCode = RESPONSE_CONNECT_TIMEOUT;
      } else if (th instanceof java.net.ConnectException) {
        print(CommonUtil.formatMessage(RESPONSE_ERROR, UNABLE_TO_CONNECT), LOGGER,
            StandardLevel.OFF);

      } else {
        String errMsg = e.getMessage() != null && !e.getMessage().isEmpty() ? e.getMessage()
            : ExceptionConstants.GENERIC_ERROR_MSG;
        print(CommonUtil.formatMessage(RESPONSE_ERROR, errMsg), LOGGER, StandardLevel.OFF);

      }
    } else {
      print(CommonUtil.formatMessage(RESPONSE_ERROR, ExceptionConstants.GENERIC_ERROR_MSG), LOGGER,
          StandardLevel.OFF);

    }
    return responseCode;
  }

}
