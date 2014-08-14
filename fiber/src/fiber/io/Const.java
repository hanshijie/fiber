package fiber.io;

public final class Const {
	public final static int statistic_interval = getProperty("statistic_interval", 3);
	public final static boolean statistic_read_bytes = getProperty("statistic_read_bytes", 0) != 0 ? true : false;
	public final static boolean statistic_beans = getProperty("statistic_beans", 0) != 0 ? true : false;
	public final static int log_level = getProperty("log_level", Log.LOG_ALL);
	public final static boolean unmarshal_exception_verbose = getProperty("unmarshal_exception_verbose", 0) != 0 ? true : false;
	
	public final static int rpc_check_timeout_interval = getProperty("rpc_check_timeout_interval", 1);
	
	public final static int reconnect_init_backoff = getProperty("reconnect_init_backoff", 1, 1, 30);
	public final static int reconnect_max_backoff = getProperty("reconnect_max_backoff", 1, 1, 300);
	
	public static int getProperty(String name, int defaultValue) {
		String value = System.getProperty(name);
		return value != null ? Integer.parseInt(value) : defaultValue;
	}
	
	public static int getProperty(String name, int defaultValue, int low, int high) {
		int value = getProperty(name, defaultValue);
		if(value < low) return low;
		if(value > high) return high;
		return value;
	}
}
