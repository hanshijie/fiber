package fiber.io;

public final class Const {
	public final static int statistic_interval;
	public final static boolean statistic_read_bytes;
	public final static boolean statistic_beans;
	public final static boolean statistic_tasks;
	public final static int log_level;
	public final static boolean unmarshal_exception_verbose;
	
	public final static int schedule_thread_pool_size;
	
	public final static int rpc_check_timeout_interval;
	
	public final static int reconnect_init_backoff;
	public final static int reconnect_max_backoff;
	static {
		SystemConfig c = SystemConfig.getInstance();
		statistic_interval = c.getStatisticInterval();
		statistic_beans = c.isStatisticBeans();
		statistic_read_bytes = c.isStatisticReadBytes();
		statistic_tasks = c.isStatisticTasks();
		log_level = c.getLogLevel();
		unmarshal_exception_verbose = c.isUnmarshalExceptionVerbose();
		schedule_thread_pool_size = c.getScheduleThreadPoolSize();
		rpc_check_timeout_interval = c.getRpcCheckTimeoutInterval();
		reconnect_init_backoff = c.getReconnectInitBackoff();
		reconnect_max_backoff = c.getReconnectMaxBackoff();
	}
}
