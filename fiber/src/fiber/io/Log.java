package fiber.io;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class Log {
	public final static int LOG_ALL 	= 0;  	// 所有日志
	public final static int LOG_DEBUG	= 1; 	// 低级别的调试日志 (生产环境中不打印)
	public final static int LOG_TRACE 	= 2;  	// 高级别的调试日志(生产环境中不打印)
	public final static int LOG_INFO 	= 3;	// 标准记录(生产环境中打印)
	public final static int LOG_NOTICE 	= 4; 	// 标准记录,但比较重要
	public final static int LOG_WARN 	= 5; 	// 需要注意的事件
	public final static int LOG_ERR 	= 6; 	// 普通错误!
	public final static int LOG_ALERT 	= 7;  	// 严重错误,必须处理.
	public final static int LOG_FATAL 	= 8;    // 致命错误.最高危险级别.
	public final static int LOG_NONE 	= LOG_FATAL + 1; // 不打印任何日志
	
	private final static String[] LevelStrs = new String[] {"", "debug", "trace", " info", "notic", " warn", "  err", "alert", "fatal", ""};
	private final static int log_level = Const.log_level;
	
	public static void debug(String fmt, Object... objects) {
		if(log_level <= LOG_DEBUG) {
			log(LOG_DEBUG, fmt, objects);
		}
	}
	
	public static void trace(String fmt, Object... objects) {
		if(log_level <= LOG_TRACE) {
			log(LOG_TRACE, fmt, objects);
		}
	}
	
	
	public static void info(String fmt, Object... objects) {
		if(log_level <= LOG_INFO) {
			log(LOG_INFO, fmt, objects);
		}
	}
	
	public static void notice(String fmt, Object... objects) {
		if(log_level <= LOG_NOTICE) {
			log(LOG_NOTICE, fmt, objects);
		}
	}
	
	public static void warn(String fmt, Object... objects) {
		if(log_level <= LOG_WARN) {
			log(LOG_WARN, fmt, objects);
		}
	}
	
	public static void err(String fmt, Object... objects) {
		if(log_level <= LOG_ERR) {
			log(LOG_ERR, fmt, objects);
		}
	}
	
	public static void alert(String fmt, Object... objects) {
		if(log_level <= LOG_ALERT) {
			log(LOG_ALERT, fmt, objects);
		}
	}
	
	public static void fatal(String fmt, Object... objects) {
		if(log_level <= LOG_FATAL) {
			log(LOG_FATAL, fmt, objects);
		}
	}
	
	public static String etos(Exception e) {
		StringBuilder s = new StringBuilder();
		for(StackTraceElement line : e.getStackTrace()) {
			s.append(line.toString());
			s.append("\n");
		}
		return s.toString();	
	}
	
	public static interface Printer {
		void print(int level, String fmt, Object... objects);
		void print(int level, String content);
	}
	
	public static Printer printer = new Printer() {
		private final SimpleDateFormat timeFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
		private volatile int now = 0;
		private volatile String strFormatDayTimeNow = "";
		@Override
		public final void print(int level, String fmt, Object... objects) {
			updateTime();
			System.out.printf(strFormatDayTimeNow + "[" + LevelStrs[level] + "]" + fmt + "\n", objects);
		}
		
		@Override 
		public final void print(int level, String content) {
			updateTime();
			System.out.print(strFormatDayTimeNow + "[" + LevelStrs[level] + "]" + content + "\n");
		}
		
		private final void updateTime() {
			int cur = Timer.currentTime();
			if(cur != now) {
				// 没严格加锁.反正错了也无妨
				now = cur;
				strFormatDayTimeNow = timeFormat.format(new Date());
			}
		}
	};
	
	public static void setPrinter(Printer printer) {
		Log.printer = printer;
	}

	public static void log(int level, String fmt, Object... objects) {
		printer.print(level, fmt, objects);
	}
	
	public static void logSimple(int level, String msg) {
		printer.print(level, msg);
	}
}
