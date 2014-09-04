package fiber.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import fiber.io.Const;
import static fiber.io.Log.log;

public class TaskPool {
	// 当出现所有线程都已阻塞,并且等待某些未执行的任务执行结束才能继续执行的情形,
	// 如果只有一种固定大小的线程池,将会发生永久饥饿.
	// 故要么使用非固定大小的线程池CacheThreadPool,
	// 要么使用两个不同策略的线程池.
	// 考虑意外情况下数据库崩溃或者网络出问题,而取数据是同步操作,会导致大量线程阻塞
	// 如果使用CacheThreadPool,在极端情况下,可能导致创建过多的线程而崩溃.
	// 考虑再三,选择了每二个方案.

	// 比较关键,而且不会阻塞或者可以保证只阻塞很短时间的任务
	// 使用noblockExecutor调度.
	private static final ExecutorService noblockExecutor = Executors.newFixedThreadPool(Const.getProperty("noblock_thread_num", Runtime.getRuntime().availableProcessors()));
	private static final ExecutorService blockExecutor = Executors.newFixedThreadPool(Const.getProperty("block_thread_num", 64));
	private static final ScheduledExecutorService normalScheduleExecutor = Executors.newScheduledThreadPool(Const.getProperty("schedule_thread_num", 2));
	
	public static void execute(Runnable task) {
		blockExecutor.execute(task);
	}
	
	public static void executeNoBlock(Runnable task) {
		noblockExecutor.execute(task);
	}
	
	private final static Marker SCHEDULE = MarkerFactory.getMarker("SCHEDULE");
	public static void schedule(Runnable task, long delay, TimeUnit unit) {
		log.debug(SCHEDULE, "task:{} delay:{} timeunit:{}", task, delay, unit);
		normalScheduleExecutor.schedule(task, delay, unit);
	}
	
	public static void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
		log.debug(SCHEDULE, "task:{} initDelay:{} period:{} timeunit:{}", task, initialDelay, period, unit);
		TaskPool.normalScheduleExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
	}

}
