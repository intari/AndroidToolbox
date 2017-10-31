package net.intari.AndroidToolbox;


/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 04.10.16.
 * Executes tasks on background
 * initiall version based off standard tutorial https://developer.android.com/training/multiple-threads/index.html
 *
 **/


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class BackgroundExecutionManager {
    /*
     * Gets the number of available cores
     * (not always the same as the maximum number of cores)
     * TODO:what about leaving one core for GUI thread?
     */
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
    // A queue of Runnables
    private static final BlockingQueue<Runnable> workQueue=new LinkedBlockingQueue<Runnable>();
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    // Creates a thread pool manager
    private static ThreadPoolExecutor workThreadPool = new ThreadPoolExecutor(
            NUMBER_OF_CORES,       // Initial pool size
            NUMBER_OF_CORES,       // Max pool size
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            workQueue);


    /**
     * Runs runnable on current work thread pool (subject to queue)
     * @param r runnable to run
     */
    public static void runOnBackgroundThread(Runnable r) {
        workThreadPool.execute(r);
    }

    /**
     * Pool used with scheduling. it's assumed usage will be light because use we can use all CPUs
     * see also https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledExecutorService.html
     *
     */
    private static ScheduledExecutorService scheduledPool = new ScheduledThreadPoolExecutor(NUMBER_OF_CORES);

    /**
     * Creates and executes a ScheduledFuture that becomes enabled after the
     * given delay.
     *
     * @param callable the function to execute
     * @param delay the time from now to delay execution
     * @param unit the time unit of the delay parameter
     * @return a ScheduledFuture that can be used to extract result or cancel
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if callable is null
     */
    public static <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return scheduledPool.schedule(callable,delay,unit);
    }

    /**
     * Creates and executes a periodic action that becomes enabled first
     * after the given initial delay, and subsequently with the given
     * period; that is executions will commence after
     * {@code initialDelay} then {@code initialDelay+period}, then
     * {@code initialDelay + 2 * period}, and so on.
     * If any execution of the task
     * encounters an exception, subsequent executions are suppressed.
     * Otherwise, the task will only terminate via cancellation or
     * termination of the executor.  If any execution of this task
     * takes longer than its period, then subsequent executions
     * may start late, but will not concurrently execute.
     *
     * @param command the task to execute
     * @param initialDelay the time to delay first execution
     * @param period the period between successive executions
     * @param unit the time unit of the initialDelay and period parameters
     * @return a ScheduledFuture representing pending completion of
     *         the task, and whose {@code get()} method will throw an
     *         exception upon cancellation
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if command is null
     * @throws IllegalArgumentException if period less than or equal to zero
     */
    public  static ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                          long initialDelay,
                                                          long period,
                                                          TimeUnit unit) {
        return scheduledPool.scheduleAtFixedRate(command,initialDelay,period,unit);
    }

    /**
     * Creates and executes a periodic action that becomes enabled first
     * after the given initial delay, and subsequently with the
     * given delay between the termination of one execution and the
     * commencement of the next.  If any execution of the task
     * encounters an exception, subsequent executions are suppressed.
     * Otherwise, the task will only terminate via cancellation or
     * termination of the executor.
     *
     * @param command the task to execute
     * @param initialDelay the time to delay first execution
     * @param delay the delay between the termination of one
     * execution and the commencement of the next
     * @param unit the time unit of the initialDelay and delay parameters
     * @return a ScheduledFuture representing pending completion of
     *         the task, and whose {@code get()} method will throw an
     *         exception upon cancellation
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if command is null
     * @throws IllegalArgumentException if delay less than or equal to zero
     */
    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                            long initialDelay,
                                                            long delay,
                                                            TimeUnit unit) {
        return scheduledPool.scheduleWithFixedDelay(command,initialDelay,delay,unit);
    }
}
