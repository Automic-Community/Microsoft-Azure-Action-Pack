/**
 * 
 */
package com.automic.azure.util;

import java.util.concurrent.ThreadFactory;

import com.automic.azure.exception.util.AzureUncaughtExceptionHandler;

/**
 * This Thread factory is used with an {@link ThreadPoolExecutor} and provides threads with
 * {@link AzureUncaughtExceptionHandler} as the UncaughtExceptionHandler.
 *
 */
public class AzureThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setUncaughtExceptionHandler(new AzureUncaughtExceptionHandler());
        return thread;
    }

}
