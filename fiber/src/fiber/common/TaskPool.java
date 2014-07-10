package fiber.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fiber.io.Const;
import fiber.io.Log;

public class TaskPool {
	private final static ExecutorService normalExecutor = Executors.newCachedThreadPool();
	private final static ScheduledExecutorService normalScheduleExecutor = Executors.newScheduledThreadPool(Const.schedule_thread_pool_size);

	public static void execute(Runnable task) {
		normalExecutor.execute(task);
	}
	
	/**
	 * delay 以毫秒为单位 
	 */
	public static void schedule(Runnable task, long delay) {
		Log.notice("schedule %s %d", task, delay);
		normalScheduleExecutor.schedule(task, delay, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * delay 以秒为单位 
	 */
	public static void scheduleSecond(Runnable task, long delay) {
		Log.notice("scheduleSchedule %s %d", task, delay);
		normalScheduleExecutor.schedule(task, delay, TimeUnit.SECONDS);
		
	}
	
	/**
	 * delay 以毫秒为单位 
	 */
	public static void scheduleAtFixedRate(Runnable task, long initialDelay, long period) {
		Log.notice("schedule %s %d %d", task, initialDelay, period);
		TaskPool.normalScheduleExecutor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * delay 以秒为单位 
	 */
	public static void scheduleSecondAtFixedRate(Runnable task, long initialDelay, long period) {
		Log.notice("scheduleSecondAt %s %d %d", task, initialDelay, period);
		TaskPool.normalScheduleExecutor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
	}

}
