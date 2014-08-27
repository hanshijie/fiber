package fiber.app.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import fiber.io.Log;
import fiber.mapdb.WKey;
import fiber.mapdb.WValue;

public class Transaction extends fiber.mapdb.Transaction {
	private final static ThreadLocal<Transaction> contexts = new ThreadLocal<Transaction>() {
		@Override
		public Transaction initialValue() {
			return new Transaction();
		}
	};
	
	public static Transaction get() {
		return contexts.get();
	}
	
	private final static ReadWriteLock waitCommitRWLock = new ReentrantReadWriteLock();
	private final static Lock waitCommitReadLock = waitCommitRWLock.readLock();
	private final static Lock waitCommitWriteLock = waitCommitRWLock.writeLock();
	private static ConcurrentHashMap<WKey, Object> waitCommitDataMap = new ConcurrentHashMap<WKey, Object>();
	private static ConcurrentHashMap<WKey, Object> inCommitDataMap = null;

	public static Map<WKey, Object> getWaitCommitDataMap() {
		Lock lock = waitCommitWriteLock;
		lock.lock();
		try{
			assert(inCommitDataMap == null);
			if(inCommitDataMap != null) return inCommitDataMap;
			inCommitDataMap = waitCommitDataMap;
			waitCommitDataMap = new ConcurrentHashMap<WKey, Object>();
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
					waitCommitDataMap.put(key, value.getCurValue());
					Log.info("waitCommitMap.put %s=>%s", key, value.getCurValue());
				}
			}
		} finally {
			lock.unlock();
		}
	}
}
