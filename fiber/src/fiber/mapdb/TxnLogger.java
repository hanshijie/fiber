package fiber.mapdb;

import static fiber.io.Log.*;

import java.util.ArrayList;

import fiber.io.Const;
import fiber.io.Log;


public final class TxnLogger {
	private static final class LogInfo {
		public final String level;
		public final String msg;
		LogInfo(String l, String f) {
			this.level = l;
			this.msg = f;
		}
	}
	private ArrayList<LogInfo> logs = new ArrayList<LogInfo>();		
	public void debug(String fmt, Object... objects) {
		if(Const.log_level <= LOG_DEBUG) {
			logs.add(new LogInfo("debug", String.format(fmt, objects)));
		}
	}
	
	public void info(String fmt, Object... objects) {
		if(Const.log_level <= LOG_INFO) {
			logs.add(new LogInfo("info", String.format(fmt, objects)));
		}
	}
	
	
	public void trace(String fmt, Object... objects) {
		if(Const.log_level <= LOG_TRACE) {
			logs.add(new LogInfo("trace", String.format(fmt, objects)));
		}
	}
	
	public void notice(String fmt, Object... objects) {
		if(Const.log_level <= LOG_NOTICE) {
			logs.add(new LogInfo("notice", String.format(fmt, objects)));
		}
	}
	
	public void warn(String fmt, Object... objects) {
		if(Const.log_level <= LOG_WARN) {
			logs.add(new LogInfo("warn", String.format(fmt, objects)));
		}
	}
	
	public void err(String fmt, Object... objects) {
		if(Const.log_level <= LOG_ERR) {
			logs.add(new LogInfo("err", String.format(fmt, objects)));
		}
	}
	
	public void alert(String fmt, Object... objects) {
		if(Const.log_level <= LOG_ALERT) {
			logs.add(new LogInfo("alert", String.format(fmt, objects)));
		}
	}
	
	public void fatal(String fmt, Object... objects) {
		if(Const.log_level <= LOG_FATAL) {
			logs.add(new LogInfo("fatal", String.format(fmt, objects)));
		}
	}
	
	public void clear() {
		logs.clear();
	}

	public void commit() {
		for(LogInfo log : this.logs) {
			Log.logSimple(log.level, log.msg);
		}
	}

}
