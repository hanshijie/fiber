package fiber.mapdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import fiber.common.LockPool;
import fiber.io.Bean;
import fiber.io.BeanCodec;
import fiber.io.ClientManager;
import fiber.io.Const;
import fiber.io.IOSession;
import fiber.io.Log;
import fiber.io.Octets;
import fiber.io.RpcBean;
import fiber.io.RpcHandler;
import static fiber.io.Log.*;

public class AbstractTransaction {
	
	public final static class Logger {
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
	
	public static final class Dispatcher {
		static final class Job1 implements Runnable {
			private final IOSession session;
			private final Octets data;
			public Job1(IOSession s, Bean<?> bean) {
				this.session = s;
				this.data = BeanCodec.encode(bean);
			}
			@Override
			public void run() {
				session.write(data);
			}
		}
		
		static final class Job2 implements Runnable {
			private final ClientManager manager;
			private final Octets data;
			public Job2(ClientManager m, Bean<?> bean) {
				this.manager = m;
				this.data = BeanCodec.encode(bean);
			}
			@Override
			public void run() {
				IOSession session = manager.getOnlySession();
				if(session != null) {
					session.write(data);
				} else {
					Log.warn("ClientManager:%s send fail. data:%s", manager, data.dump());
				}
			}
		}
		
		static final class Job3<A extends Bean<A>, R extends Bean<R>> implements Runnable {
			private final ClientManager manager;
			private final RpcBean<A, R> rpcbean;
			private final RpcHandler<A, R> handler;
			public Job3(ClientManager m, RpcBean<A, R> rpcbean, RpcHandler<A, R> handler) {
				this.manager = m;
				this.rpcbean = rpcbean;
				this.handler = handler;
			}
			@Override
			public void run() {
				if(handler != null) {
					this.manager.sendRpc(rpcbean, handler);
				} else {
					this.manager.sendRpc(rpcbean);
				}
			}
		}
		
		private final ArrayList<Runnable> jobs = new ArrayList<Runnable>();
		
		public void send(IOSession session, Bean<?> bean) {
			this.jobs.add(new Job1(session, bean));
		}
		
		public void send(ClientManager manager, Bean<?> bean) {
			this.jobs.add(new Job2(manager, bean));
		}
		
		public <A extends Bean<A>, R extends Bean<R>> void sendRpc(ClientManager manager, RpcBean<A, R> rpcbean) {
			this.jobs.add(new Job3<A, R>(manager, rpcbean, null));
		}
		
		public <A extends Bean<A>, R extends Bean<R>> void sendRpc(ClientManager manager, RpcBean<A, R> rpcbean, RpcHandler<A, R> handler) {
			this.jobs.add(new Job3<A, R>(manager, rpcbean, handler));
		}
		
		public void clear() {
			this.jobs.clear();
		}
		
		public void commit() {
			for(Runnable job : this.jobs) {
				job.run();
			}
			this.jobs.clear();
		}
	}

	
	private final HashMap<WKey, WValue> dataMap;
	private final Logger logger;	
	private final Dispatcher dispatcher;
	private final TreeSet<Integer> lockSet;
	public AbstractTransaction() {
		this.dataMap = new HashMap<WKey, WValue>();
		this.logger = new Logger();
		this.lockSet = new TreeSet<Integer>();
		this.dispatcher = new Dispatcher();
	}
	
	public final Logger getLogger() {
		return this.logger;
	}
	
	public final Dispatcher getDispatcher() {
		return this.dispatcher;
	}

	private final void clearDatas() {
		this.dataMap.clear();
		this.logger.clear();
		this.dispatcher.clear();
	}
	
	public final void prepare() {
		//this.clearDatas();
	}
	
	public void commit() throws ConflictException {
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
		commitModifyData();
		this.logger.commit();
		this.dispatcher.commit();
		this.unlock();
		Log.info("%s commit. end.", this);
	}
	
	protected void commitModifyData() {
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
