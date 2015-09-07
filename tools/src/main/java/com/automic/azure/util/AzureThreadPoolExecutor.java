package com.automic.azure.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * <p>
 * Thread Pool Executor which provides features like
 * <ul>
 * <li>a fixed size thread pool
 * <li>logging when all threads have been executed</li>
 * <li>shutdown and wait for termination feature. Call {@link AzureThreadPoolExecutor#shutDownAndTerminate}
 * <li>
 * </ul>
 * </p>
 * 
 */
public final class AzureThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger LOGGER = LogManager.getLogger(AzureThreadPoolExecutor.class);

    private final ThreadLocal<Long> startTime = new ThreadLocal<Long>();
    private final AtomicLong totalTime = new AtomicLong();
    private final AtomicLong numTasks = new AtomicLong();
    private int waitTimeout;
    private TimeUnit timeUnit;

    /**
     * Constructor to create a fixed sized thread pool and waits for a fixed time before timing out when terminating
     * 
     * @param threadPoolSize
     *            size of thread pool
     * @param waitTimeout
     *            time to wait for pool to terminate
     * @param timeUnits
     *            timeout unites eg. TimeUnit.MILLISECONDS
     */
    public AzureThreadPoolExecutor(int threadPoolSize, int waitTimeout, TimeUnit timeUnit) {
        super(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        this.waitTimeout = waitTimeout;
        this.timeUnit = timeUnit;
    }

    protected final void beforeExecute(Thread t, Runnable r) {
        startTime.set(System.currentTimeMillis());
        super.beforeExecute(t, r);
    }

    protected final void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        long endTime = System.currentTimeMillis();
        long taskTime = endTime - startTime.get();
        numTasks.incrementAndGet();
        totalTime.addAndGet(taskTime);
    }

    protected final void terminated() {
        try {
            LOGGER.info(String.format("Terminated: Total time=%d milliseconds and Total tasks=%s", totalTime.get(),
                    numTasks.get()));
        } finally {
            super.terminated();
        }
    }

    /**
     * method to shutdown and wait for all the threads to complete
     * 
     * @throws InterruptedException
     */
    public final void shutDownAndTerminate() throws InterruptedException {
        LOGGER.info("Terminating Executor and waiting for threads to complete");
        super.shutdown();
        super.awaitTermination(waitTimeout, timeUnit);
    }
}
