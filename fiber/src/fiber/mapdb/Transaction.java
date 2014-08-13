package fiber.mapdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import fiber.common.LockPool;
import fiber.io.Const;
import fiber.io.Log;
import static fiber.io.Log.*;

public class Transaction {
	
	final static class Logger {
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

	
	private final HashMap<WKey, WValue> dataMap;
	private final Logger logger;	
	private final TreeSet<Integer> lockSet;
	public Transaction() {
		this.dataMap = new HashMap<WKey, WValue>();
		this.logger = new Logger();
		this.lockSet = new TreeSet<Integer>();
	}
	
	public final Logger getLogger() {
		return this.logger;
	}

	private final void clearDatas() {
		this.dataMap.clear();
		this.logger.clear();
	}
	
	public final void prepare() {
		//this.clearDatas();
	}
	
	public void commit() throws Exception {
		Log.info("%s commit. start.", this);
		this.lock();
		for(WValue value : this.dataMap.values()) {
			if(value.isConflict() || value.getTvalue().isShrink()) {
				// 一般来说,检查到冲突后会redo,出于优化考虑
				// 不释放锁.
				Log.info("%s confliction detected!", this);
				throw ConflictException.INSTANCE;
			}
		}
		for(Map.Entry<WKey, WValue> e : this.dataMap.entrySet()) {
			WKey key = e.getKey();
			WValue value = e.getValue();
			value.commit();
			key.getTable().onUpdate(key, value.getTvalue());
		}
		for(WValue value : this.dataMap.values()) {
			value.commit();
		}
		commitModifyData();
		this.logger.commit();
		this.unlock();
		Log.info("%s commit. end.", this);
	}
	
	protected void commitModifyData() throws Exception {
		// TODO 作为一个完整事务将修改的数据加入到变化表中.
	}
	
	public void rollback() {
		this.clearDatas();
		Log.info("%s rollback", this);
	}
	
	public void end() {
		this.clearDatas();
		this.unlock();
		Log.info("%s end", this);
	}
	
	public final WValue getData(WKey key) {
		return this.dataMap.get(key);
	}
	
	public final void putData(WKey key, WValue value) {
		this.dataMap.put(key, value);
	}
	
	public final HashMap<WKey, WValue> getDataMap() {
		return this.dataMap;
	}
	
	/**
	 * 由于经常性的不一致性导致redo,需要尽量减少redo次数,
	 * 一个非常有效的优化是, 如果redo, 不释放此次已经锁定的lock,
	 * 绝大多数情况下(>99%),redo里使用到的lock应该和当前已经锁定的lock完全相同,
	 * 那么由于已经事先持有锁,本次操作肯定会成功.
	 */
	public void lock() {
		LockPool lp = LockPool.getInstance();
		if(this.lockSet.isEmpty()) {
			for(WKey key : this.dataMap.keySet()) {
				int lockid = lp.lockid(key.hashCode());
				this.lockSet.add(lockid);
			}
			doLock();
		} else {
			for(WKey key : this.dataMap.keySet()) {
				int lockid = lp.lockid(key.hashCode());
				if(!this.lockSet.contains(lockid)) {
					unlock();
					lock();
					return;
				}
			}
		}
	}
	
	public final void unlock() {
		doUnlock();
		this.lockSet.clear();
	}
	
	private void doLock() {
		LockPool.getInstance().lock(this.lockSet);
	}
	
	private void doUnlock() {
		LockPool.getInstance().unlock(this.lockSet);
	}
	
	public void dump() {
		for(Map.Entry<WKey, WValue> e : this.dataMap.entrySet()) {
			Log.trace("{key=%s, value=%s}", e.getKey(), e.getValue());
		}
	}

}
