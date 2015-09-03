package com.automic.azure.exception.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.exception.AzureRuntimeException;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.ConsoleWriter;

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

    public static int handleException(Exception ex) {
        int responseCode = RESPONSE_NOT_OK;
        String errorMsg;
        Throwable th = ex;
        while (th.getCause() != null) {
            th = th.getCause();
        }
        if (th instanceof AzureException || th instanceof AzureRuntimeException) {
            errorMsg = th.getMessage();
        } else {
            LOGGER.error(ExceptionConstants.GENERIC_ERROR_MSG, ex);
            errorMsg = th.getMessage();
            if (th instanceof java.net.SocketTimeoutException) {
                errorMsg = CONNECTION_TIMEOUT;
                responseCode = RESPONSE_CONNECT_TIMEOUT;
            } else if (th instanceof java.net.ConnectException) {
                errorMsg = UNABLE_TO_CONNECT;
            }
        }
        ConsoleWriter.writeln(CommonUtil.formatErrorMessage((errorMsg == null) ? "System Error Occured" : errorMsg));
        ConsoleWriter.writeln(CommonUtil.formatErrorMessage(ERRORMSG));
        return responseCode;
    }

}
