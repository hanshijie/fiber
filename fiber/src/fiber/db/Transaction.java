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
import fiber.io.IOSession;
import static fiber.io.Log.log;
import fiber.io.Octets;
import fiber.io.RpcBean;
import fiber.io.RpcHandler;
import fiber.io.Timer;

public class Transaction {
	
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
	private final Dispatcher dispatcher;
	private final TreeSet<Integer> lockSet;
	
	private long cacheTxnid = 0;
	private int cacheNow = 0;
	
	private final static AtomicLong TXN_ID = new AtomicLong(0);
	private long txnid;
	public Transaction() {
		this.dataMap = new HashMap<WKey, WValue>();
		this.lockSet = new TreeSet<Integer>();
		this.dispatcher = new Dispatcher();
		this.txnid = 0;
	}
	
	public final Dispatcher getDispatcher() {
		return this.dispatcher;
	}
	
	public final long getTxnid() {
		return this.txnid;
	}

	private final void clearDatas() {
		this.dataMap.clear();
		this.dispatcher.clear();
	}
	
	/** 
	 * @return 获得缓存的当前时间now. now在整个事务期间不变.
	 * 这个特性在很多情形下很有用.如果直接使用Timer.currentTime(),
	 * 在处理请求的过程的得到的now有可能前后不一致,
	 * 导致一些难以发现的bug. 
	 */
	public final int cacheCurrentTime() {
		if(this.cacheTxnid != this.txnid) {
			this.cacheTxnid = this.txnid;
			this.cacheNow = Timer.currentTime();
		}
		return this.cacheNow;
	}
	
	public final void prepare() {
		this.txnid = TXN_ID.incrementAndGet();
		//this.clearDatas();
	}
	
	public void commit() throws ConflictException {
		log.debug("{} commit. start.", this);
		this.lock();
		for(WValue value : this.dataMap.values()) {
			if(value.isConflict() || value.getTvalue().isShrink()) {
				// 一般来说,检查到冲突后会redo,出于优化考虑
				// 不释放锁.
				log.debug("{} confliction detected!", this);
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
		this.dispatcher.commit();
		this.unlock();
		log.debug("{} commit. end.", this);
	}
	
	public void rollback() {
		this.clearDatas();
		this.unlock();
		log.info("{} rollback", this);
		this.txnid = TXN_ID.incrementAndGet();
	}
	
	/*
	 * 与rollback的区别为 是否保持上次事务执行过程中使用的锁
	 */
	public void rollbackHoldLocks() {
		this.clearDatas();
		log.info("{} rollbackHoldLocks", this);
		this.txnid = TXN_ID.incrementAndGet();		
	}
	
	public void end() {
		this.clearDatas();
		this.unlock();
		log.debug("{} end", this);
	}
	
	public final void ret(int retcode) throws RetException {
		RetException.trigger(retcode);
	}
	
	public final void ret(int retcode, Object content) throws RetException {
		RetException.trigger(retcode, content);
	}
	
	@Override
	public final String toString() {
		return "[TXN:" + this.getTxnid() + "]";
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
			log.info("{} dump. dataMap {key={}, value={}}", this, e.getKey(), e.getValue());
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
				log.warn("Transaction.commit inCommitDataMap not commit succ? retry.");
				return inCommitDataMap;
			}
			inCommitDataMap = waitCommitDataMap;
			waitCommitDataMap = new ConcurrentHashMap<WKey, WValue>();
			log.info("Transaction.commit new inCommitDataMap. size:{}", inCommitDataMap.size());
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
			log.info("Transaction.commit done finish.");
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
	
	public static boolean getDirtyData(WKey key, TValue value) {
		Lock lock = waitCommitReadLock;
		lock.lock();
		try{
			WValue wvalue = waitCommitDataMap.get(key);
			if(wvalue == null && inCommitDataMap != null) {
				wvalue = inCommitDataMap.get(key);
			}
			if(wvalue != null) {
				value.setValue(wvalue.getCurValue());
				return true;
			} else {
				return false;
			}
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
					log.debug("waitCommitMap.put [{}]=>{origin:{}, cur:{}}", key, value.getOriginValue(), value.getCurValue());
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
