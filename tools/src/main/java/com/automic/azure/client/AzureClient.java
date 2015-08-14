/**
 * 
 */
package com.automic.azure.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.exception.util.ExceptionHandler;
import com.automic.azure.util.ConsoleWriter;

/**
 * Main Class is the insertion point of Azure interaction api when called from AE implementation. It delegates the
 * parameters to appropriate action and returns a response code based on output of action.
 * 
 * Following response code are returned by java program 0 - Successful response from Azure API 1 - An exception
 * occurred/Error in response from Azure API 2 - Connection timeout while calling Azure API
 * 
 */
public final class AzureClient {

    private static final Logger LOGGER = LogManager.getLogger(AzureClient.class);

    private static final int RESPONSE_OK = 0;

    private AzureClient() {
    }

    /**
     * Main method which will start the execution of an action on Azure. This method will call the AzureClientHelper
     * class which will trigger the execution of specific action and then if action fails this main method will handle
     * the failed scenario and print the error message and system will exit with the respective response code.
     * 
     * @param args
     *            array of Arguments
     */
    public static void main(String[] args) {      
        int responseCode = RESPONSE_OK;
        try {
            AzureClientHelper.executeAction(args);
        } catch (Exception e) {
            responseCode = ExceptionHandler.handleException(e);
        } finally {
            ConsoleWriter.flush();
        }
        LOGGER.info("@@@@@@@ Execution ends for action  with response code : " + responseCode);
        System.exit(responseCode);
    }

}
