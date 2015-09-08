package com.automic.azure.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
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
 * 
 * The General behavior of the Executor is to shutdown forcefully if any of the worker threads throw an exception.
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

    private Semaphore semaphore;

    /**
     * Constructor to create a fixed sized thread pool and waits for a fixed time before timing out when terminating
     * 
     * @param threadPoolSize
     *            size of thread pool
     * @param waitTimeout
     *            time to wait for pool to terminate
     * @param threadFactory
     *            factory to create threads for this executor
     * @param timeUnits
     *            timeout unites eg. TimeUnit.MILLISECONDS
     */
    public AzureThreadPoolExecutor(int threadPoolSize, int queueSize, int waitTimeout, ThreadFactory threadFactory,
            TimeUnit timeUnit) {
        super(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueSize),
                threadFactory);
        this.waitTimeout = waitTimeout;
        this.timeUnit = timeUnit;

        // and those queued up
        semaphore = new Semaphore(threadPoolSize + queueSize);
    }

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
    public AzureThreadPoolExecutor(int threadPoolSize, int queueSize, int waitTimeout, TimeUnit timeUnit) {
        super(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueSize));
        this.waitTimeout = waitTimeout;
        this.timeUnit = timeUnit;

        // and those queued up
        semaphore = new Semaphore(threadPoolSize + queueSize);
    }

    @Override
    protected final void beforeExecute(Thread t, Runnable r) {
        startTime.set(System.currentTimeMillis());
        super.beforeExecute(t, r);
    }

    /**
     * Executes the given task. This method will block when the semaphore has no permits i.e. when the queue has reached
     * its capacity.
     */
    @Override
    public void execute(final Runnable task) {
        boolean acquired = false;
        do {
            try {
                semaphore.acquire();
                acquired = true;
            } catch (final InterruptedException e) {
                // wait for semaphore to
            }
        } while (!acquired);

        try {
            super.execute(task);
        } catch (final RejectedExecutionException e) {
            semaphore.release();
            throw e;
        }
    }

    @Override
    protected final void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t != null) {
            this.shutdownNow();
            //throw new AzureRuntimeException("Error in task " + r);
        }
        long endTime = System.currentTimeMillis();
        long taskTime = endTime - startTime.get();
        numTasks.incrementAndGet();
        totalTime.addAndGet(taskTime);
        semaphore.release();
    }

    @Override
    protected final void terminated() {
        try {
            LOGGER.info(String.format("Terminated: Total time=%d milliseconds and Total tasks=%s", totalTime.get(),
                    numTasks.get()));
        } finally {
            super.terminated();
        }
    }

    /**
     * method to shutdown and wait for all the threads to complete. This is a blocking call and the calling method will
     * wait till all the running threads and queued tasks are not complete.
     * 
     * @throws InterruptedException
     */
    public final void shutDownAndTerminate() throws InterruptedException {
        LOGGER.info("Terminating Executor and waiting for threads to complete");
        super.shutdown();
        super.awaitTermination(waitTimeout, timeUnit);
    }
}
