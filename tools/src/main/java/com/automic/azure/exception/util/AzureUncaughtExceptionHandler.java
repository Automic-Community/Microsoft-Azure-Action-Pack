/**
 * 
 */
package com.automic.azure.exception.util;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Exception handler to suppress the default behavior of UncaughtExceptionHandler which prints the exception and its
 * stack trace on the console since we do not
 *
 */
public class AzureUncaughtExceptionHandler implements UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // Do nothing

    }

}
