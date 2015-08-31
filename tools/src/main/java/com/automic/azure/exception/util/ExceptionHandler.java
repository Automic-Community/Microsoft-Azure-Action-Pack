package com.automic.azure.exception.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.ConsoleWriter;
import com.sun.jersey.api.client.ClientHandlerException;

/**
 * This class handles some specific cases like connection timeout/unable to connect and return the response code. In
 * addition to this, it also writes the exception message to console.
 */

public class ExceptionHandler {

    private static final Logger LOGGER = LogManager.getLogger(ExceptionHandler.class);

    private static final int RESPONSE_NOT_OK = 1;
    private static final int RESPONSE_CONNECT_TIMEOUT = 2;

    private static final String ERRORMSG = "Please check the input parameters. For more details refer java logs";
    private static final String CONNECTION_TIMEOUT = "Connection Timeout.";
    private static final String UNABLE_TO_CONNECT = "Unable to connect.";
    private static final String UNABLE_TO_CONNECT_HOST = "Unable to connect to host: ";

    public static int handleException(Exception ex) {
        int responseCode = RESPONSE_NOT_OK;
        String errorMsg;
        if (ex instanceof AzureException) {
            errorMsg = ex.getMessage();
        } else {
            LOGGER.error(ExceptionConstants.GENERIC_ERROR_MSG, ex);
            if (ex instanceof ClientHandlerException) {
                Throwable th = ex.getCause();
                if (th instanceof java.net.SocketTimeoutException) {
                    errorMsg = CONNECTION_TIMEOUT;
                    responseCode = RESPONSE_CONNECT_TIMEOUT;
                } else if (th instanceof java.net.ConnectException) {
                    errorMsg = UNABLE_TO_CONNECT;
                } else if (th instanceof java.net.UnknownHostException) {
                    errorMsg = UNABLE_TO_CONNECT_HOST + th.getMessage();
                } else if (th != null) {
                    errorMsg = th.getMessage();
                } else {
                    errorMsg = ex.getMessage();
                }
            } else {
                errorMsg = ex.getMessage();

            }
        }
        ConsoleWriter.writeln(CommonUtil.formatErrorMessage((errorMsg == null) ? "System Error" : errorMsg));
        ConsoleWriter.writeln(CommonUtil.formatErrorMessage(ERRORMSG));
        return responseCode;
    }

}
