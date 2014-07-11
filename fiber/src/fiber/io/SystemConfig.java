package fiber.io;

public final class SystemConfig {
	public int logLevel = Log.LOG_DEBUG;
	
	public int lockPoolSize = 1024;
	
	public int scheduleThreadPoolSize = 2;
	
	public int statisticInterval = 5;
	
	public boolean statisticBeans = true;
	public boolean statisticTasks = true;
	public boolean statisticReadBytes = false;	
	public boolean unmarshalExceptionVerbose = false;
	public int rpcCheckTimeoutInterval = 1;
	public int reconnectInitBackoff = 1;
	public int reconnectMaxBackoff = 64;
	
	public SystemConfig() {	}
	private static final SystemConfig instance = new SystemConfig();
	public static SystemConfig getInstance() { return instance; }
	
	private static int limit(int value, int low, int high) {
		assert(low <= high);
		if(value < low) {
			value = low;
		} else if(value > high) {
			value = high;
		}
		return value;
	}

	public int getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}
	public int getLockLoolSize() {
		return lockPoolSize;
	}
	public void setLockPoolSize(int lockLoolSize) {
		this.lockPoolSize = limit(lockLoolSize, 64, 10240);
	}
	public int getScheduleThreadPoolSize() {
		return scheduleThreadPoolSize;
	}
	public void setScheduleThreadPoolSize(int scheduleThreadPoolSize) {
		this.scheduleThreadPoolSize = limit(scheduleThreadPoolSize, 1, 64);
	}
	public int getStatisticInterval() {
		return statisticInterval;
	}

	public void setStatisticInterval(int statisticInterval) {
		this.statisticInterval = limit(statisticInterval, 1, Integer.MAX_VALUE);
	}

	public boolean isStatisticBeans() {
		return statisticBeans;
	}

	public void setStatisticBeans(boolean statisticBeans) {
		this.statisticBeans = statisticBeans;
	}

	public boolean isStatisticTasks() {
		return statisticTasks;
	}
	public void setStatisticTasks(boolean isStatisticTasks) {
		this.statisticTasks = isStatisticTasks;
	}
	public boolean isStatisticReadBytes() {
		return statisticReadBytes;
	}
	public void setStatisticReadBytes(boolean isStatisticReadBytes) {
		this.statisticReadBytes = isStatisticReadBytes;
	}
	public boolean isUnmarshalExceptionVerbose() {
		return unmarshalExceptionVerbose;
	}
	public void setUnmarshalExceptionVerbose(boolean unmarshalExceptionVerbose) {
		this.unmarshalExceptionVerbose = unmarshalExceptionVerbose;
	}
	public int getRpcCheckTimeoutInterval() {
		return rpcCheckTimeoutInterval;
	}
	public void setRpcCheckTimeoutInterval(int rpcCheckTimeoutInterval) {
		this.rpcCheckTimeoutInterval = limit(rpcCheckTimeoutInterval, 1, 60);
	}
	public int getReconnectInitBackoff() {
		return reconnectInitBackoff;
	}
	public void setReconnectInitBackoff(int reconnectInitBackoff) {
		this.reconnectInitBackoff = limit(reconnectInitBackoff, 1, Integer.MAX_VALUE);
	}
	public int getReconnectMaxBackoff() {
		return reconnectMaxBackoff;
	}
	public void setReconnectMaxBackoff(int reconnectMaxBackoff) {
		this.reconnectMaxBackoff = limit(reconnectMaxBackoff, 1, Integer.MAX_VALUE);
	}


	
}
