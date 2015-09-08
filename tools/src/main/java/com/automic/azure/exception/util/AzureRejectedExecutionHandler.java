package com.automic.azure.exception.util;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import com.automic.azure.exception.AzureRuntimeException;

/**
 * 
 * Class which could be set as the {@link RejectedExecutionHandler} of a {@link ThreadPoolExecutor}. It throws an
 * {@link AzureRuntimeException} with the use defined message in case a task is rejected by an executor.
 * 
 *
 */
public class AzureRejectedExecutionHandler implements RejectedExecutionHandler {

    private String errorMsg;

    /**
     * @param errorMsg
     */
    public AzureRejectedExecutionHandler(String errorMsg) {
        super();
        this.errorMsg = errorMsg;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        //
        if (errorMsg == null) {
            errorMsg = String.format("Error while submitting task %s to executor %s", r, executor);
        }
        throw new AzureRuntimeException(errorMsg);
    }
}
