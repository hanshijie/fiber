package fiber.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import fiber.common.TaskPool;
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

public class Transaction {
	
	public final static class Logger {
		private final Transaction txn;
		public Logger(Transaction txn) {
			this.txn = txn;
		}
		
		private static final class LogInfo {
			public final int level;
			public final String msg;
			LogInfo(int l, String f) {
				this.level = l;
				this.msg = f;
			}
		}
		private static final int log_level = Const.log_level;
		private ArrayList<LogInfo> logs = new ArrayList<LogInfo>();		
		public void debug(String fmt, Object... objects) {
			if(log_level <= LOG_DEBUG) {
				logs.add(new LogInfo(LOG_DEBUG, String.format(fmt, objects)));
			}
		}
		
		public void trace(String fmt, Object... objects) {
			if(log_level <= LOG_TRACE) {
				logs.add(new LogInfo(LOG_TRACE, String.format(fmt, objects)));
			}
		}
		
		public void info(String fmt, Object... objects) {
			if(log_level <= LOG_INFO) {
				logs.add(new LogInfo(LOG_INFO, String.format(fmt, objects)));
			}
		}
		
		public void notice(String fmt, Object... objects) {
			if(log_level <= LOG_NOTICE) {
				logs.add(new LogInfo(LOG_NOTICE, String.format(fmt, objects)));
			}
		}
		
		public void warn(String fmt, Object... objects) {
			if(log_level <= LOG_WARN) {
				logs.add(new LogInfo(LOG_WARN, String.format(fmt, objects)));
			}
		}
		
		public void err(String fmt, Object... objects) {
			if(log_level <= LOG_ERR) {
				logs.add(new LogInfo(LOG_ERR, String.format(fmt, objects)));
			}
		}
		
		public void alert(String fmt, Object... objects) {
			if(log_level <= LOG_ALERT) {
				logs.add(new LogInfo(LOG_ALERT, String.format(fmt, objects)));
			}
		}
		
		public void fatal(String fmt, Object... objects) {
			if(log_level <= LOG_FATAL) {
				logs.add(new LogInfo(LOG_FATAL, String.format(fmt, objects)));
			}
		}
		
		public void clear() {
			logs.clear();
		}

		public void commit() {
			String txnStr = this.txn.toString();
			for(LogInfo log : this.logs) {
				Log.logSimple(log.level, txnStr + log.msg);
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
		
		static final class Job4 implements Runnable {
			private final long delay;
			private final Runnable task;
			public Job4(Runnable task, long delay) {
				this.delay = delay;
				this.task = task;
			}
			@Override
			public void run() {
				TaskPool.schedule(task, delay, TimeUnit.MILLISECONDS);
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
		
		public void schedule(Runnable task, long milliseconddelay) {
			this.jobs.add(new Job4(task, milliseconddelay));
		}
		
		public void scheduleSecond(Runnable task, long secondDelay) {
			this.jobs.add(new Job4(task, secondDelay * 1000));
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
	
	private final static AtomicLong TXN_ID = new AtomicLong(0);
	private long txnid;
	public Transaction() {
		this.dataMap = new HashMap<WKey, WValue>();
		this.logger = new Logger(this);
		this.lockSet = new TreeSet<Integer>();
		this.dispatcher = new Dispatcher();
		this.txnid = 0;
	}
	
	public final Logger getLogger() {
		return this.logger;
	}
	
	public final Dispatcher getDispatcher() {
		return this.dispatcher;
	}
	
	public final long getTxnid() {
		return this.txnid;
	}

	private final void clearDatas() {
		this.dataMap.clear();
		this.logger.clear();
		this.dispatcher.clear();
	}
	
	public final void prepare() {
		this.txnid = TXN_ID.incrementAndGet();
		//this.clearDatas();
	}
	
	public void commit() throws ConflictException {
		Log.trace("%s commit. start.", this);
		this.lock();
		for(WValue value : this.dataMap.values()) {
			if(value.isConflict() || value.getTvalue().isShrink()) {
				// 一般来说,检查到冲突后会redo,出于优化考虑
				// 不释放锁.
				Log.trace("%s confliction detected!", this);
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
		Log.trace("%s commit. end.", this);
	}
	
	public void rollback() {
		this.clearDatas();
		Log.trace("%s rollback", this);
	}
	
	public void end() {
		this.clearDatas();
		this.unlock();
		Log.trace("%s end", this);
	}
	
	@Override
	public final String toString() {
		return String.format("[txnid:%s]", this.getTxnid());
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
			Log.info("{key=%s, value=%s}", e.getKey(), e.getValue());
		}
	}
	
	private final static ReadWriteLock waitCommitRWLock = new ReentrantReadWriteLock();
	private final static Lock waitCommitReadLock = waitCommitRWLock.readLock();
	private final static Lock waitCommitWriteLock = waitCommitRWLock.writeLock();
	private static ConcurrentHashMap<WKey, WValue> waitCommitDataMap = new ConcurrentHashMap<WKey, WValue>();
	private static ConcurrentHashMap<WKey, WValue> inCommitDataMap = null;

	public static Map<WKey, WValue> getWaitCommitDataMap() {
		Lock lock = waitCommitWriteLock;
		lock.lock();
		try{
			if(inCommitDataMap != null) {
				Log.warn("Transation.getWaitCommitDataMap. inCommitDataMap not commit succ? retry.");
				return inCommitDataMap;
			}
			inCommitDataMap = waitCommitDataMap;
			waitCommitDataMap = new ConcurrentHashMap<WKey, WValue>();
			Log.notice("=====> new inCommitDataMap. size:%d", inCommitDataMap.size());
			return inCommitDataMap;
		} finally {
			lock.unlock();
		}
	}
	
	public static void doneCommit() {
		assert(inCommitDataMap != null);
		Lock lock = waitCommitReadLock;
		lock.lock();
		try {
			inCommitDataMap = null;
			Log.notice("=====> commit finish.");
		} finally {
			lock.unlock();
		}
	}
	
	public static boolean isDirty(WKey key) {
		Lock lock = waitCommitReadLock;
		lock.lock();
		try {
			return waitCommitDataMap.contains(key) || (inCommitDataMap != null && inCommitDataMap.contains(key));
		} finally {
			lock.unlock();
		}
	}

	protected void commitModifyData() {
		Lock lock = waitCommitReadLock;
		lock.lock();
		try{
			for(Map.Entry<WKey, WValue> e : this.getDataMap().entrySet()) {
				WKey key = e.getKey();
				WValue value = e.getValue();
				if(value.isModify() && key.getTable().isPersist()) {
					waitCommitDataMap.put(key, value);
					Log.trace("waitCommitMap.put [%s]=>{origin:%s, cur:%s}", key, value.getOriginValue(), value.getCurValue());
				}
			}
		} finally {
			lock.unlock();
		}
	}
	
	private final static ThreadLocal<Transaction> contexts = new ThreadLocal<Transaction>() {
		@Override
		public Transaction initialValue() {
			return new Transaction();
		}
	};
	
	public static Transaction get() {
		return contexts.get();
	}

}
