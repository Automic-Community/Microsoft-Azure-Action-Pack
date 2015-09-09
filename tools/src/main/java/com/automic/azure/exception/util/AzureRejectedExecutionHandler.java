package com.automic.azure.exception.util;
   
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import com.automic.azure.exception.AzureRuntimeException;
import com.automic.azure.util.AzureThreadPoolExecutor;

/**
 * 
 * Class which could be set as the {@link RejectedExecutionHandler} of a {@link ThreadPoolExecutor}. It throws an
 * {@link AzureRuntimeException} with the message in case a task is rejected by an executor.
 * 
 *
 */
public class AzureRejectedExecutionHandler implements RejectedExecutionHandler {
   

    public AzureRejectedExecutionHandler() {
        
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        String errorMsg = null;
        //
        if (executor instanceof AzureThreadPoolExecutor) {
            Throwable taskException = ((AzureThreadPoolExecutor) executor).getTaskException();
            if (taskException != null) {
                errorMsg = taskException.getMessage();
            }
        }
        if (errorMsg == null) {
            errorMsg = String.format("Error while submitting task %s to executor %s", r, executor);
        }

        throw new AzureRuntimeException(errorMsg);
    }
}
