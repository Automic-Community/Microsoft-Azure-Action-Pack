/**
 * 
 */
package com.automic.azure.exception.util;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Exception handler
 *
 */
public class AzureUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private static final Logger LOGGER = LogManager.getLogger(AzureUncaughtExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        //
        LOGGER.error("uncaught exception in thread: " + t, e);
    }

}
