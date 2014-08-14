package fiber.io;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class Log {
	public final static int LOG_ALL 	= 0;  	// 所有日志
	public final static int LOG_DEBUG	= 1; 	// 低级别的调试日志 (生产环境中不打印)
	public final static int LOG_INFO 	= 2;  	// 高级别的调试日志(生产环境中不打印)
	public final static int LOG_TRACE 	= 3;	// 标准记录(生产环境中打印)
	public final static int LOG_NOTICE 	= 5; 	// 标准记录,但比较重要
	public final static int LOG_WARN 	= 6; 	// 需要注意的事件
	public final static int LOG_ERR 	= 7; 	// 普通错误!
	public final static int LOG_ALERT 	= 8;  	// 严重错误,必须处理.
	public final static int LOG_FATAL 	= 9;    // 致命错误.最高危险级别.
	public final static int LOG_NONE 	= LOG_FATAL + 1; // 不打印任何日志
	
	public static void debug(String fmt, Object... objects) {
		if(Const.log_level <= LOG_DEBUG) {
			log("debug", fmt, objects);
		}
	}
	
	public static void info(String fmt, Object... objects) {
		if(Const.log_level <= LOG_INFO) {
			log("info", fmt, objects);
		}
	}
	
	
	public static void trace(String fmt, Object... objects) {
		if(Const.log_level <= LOG_TRACE) {
			log("trace", fmt, objects);
		}
	}
	
	public static void notice(String fmt, Object... objects) {
		if(Const.log_level <= LOG_NOTICE) {
			log("notice", fmt, objects);
		}
	}
	
	public static void warn(String fmt, Object... objects) {
		if(Const.log_level <= LOG_WARN) {
			log("warn", fmt, objects);
		}
	}
	
	public static void err(String fmt, Object... objects) {
		if(Const.log_level <= LOG_ERR) {
			log("err", fmt, objects);
		}
	}
	
	public static void alert(String fmt, Object... objects) {
		if(Const.log_level <= LOG_ALERT) {
			log("alert", fmt, objects);
		}
	}
	
	public static void fatal(String fmt, Object... objects) {
		if(Const.log_level <= LOG_FATAL) {
			log("fatal", fmt, objects);
		}
	}
	
	public static interface Printer {
		void print(String prefix, String fmt, Object... objects);
		void print(String prefix, String content);
	}
	
	public static Printer printer = new Printer() {
		private final SimpleDateFormat timeFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
		private volatile int now = 0;
		private volatile String strFormatDayTimeNow = "";
		@Override
		public final void print(String prefix, String fmt, Object... objects) {
			updateTime();
			System.out.printf(strFormatDayTimeNow + "[" + prefix + "]" + fmt + "\n", objects);
		}
		
		@Override 
		public final void print(String prefix, String content) {
			updateTime();
			System.out.print(strFormatDayTimeNow + "[" + prefix + "]" + content + "\n");
		}
		
		private final void updateTime() {
			int cur = (int)(System.currentTimeMillis() / 1000);
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

	public static void log(String prefix, String fmt, Object... objects) {
		printer.print(prefix, fmt, objects);
	}
	
	public static void logSimple(String prefix, String msg) {
		printer.print(prefix, msg);
	}
}
