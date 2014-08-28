package fiber.app.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import fiber.io.Log;
import fiber.mapdb.WKey;
import fiber.mapdb.WValue;

public class Transaction extends fiber.mapdb.AbstractTransaction {
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
	private static ConcurrentHashMap<WKey, WValue> waitCommitDataMap = new ConcurrentHashMap<WKey, WValue>();
	private static ConcurrentHashMap<WKey, WValue> inCommitDataMap = null;

	public static Map<WKey, WValue> getWaitCommitDataMap() {
		Lock lock = waitCommitWriteLock;
		lock.lock();
		try{
			if(inCommitDataMap != null) {
				Log.alert("Transation.getWaitCommitDataMap. inCommitDataMap not commit succ? retry.");
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
	
	static void doneCommit() {
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
					Log.info("waitCommitMap.put [%s]=>{origin:%s, cur:%s}", key, value.getOriginValue(), value.getCurValue());
				}
			}
		} finally {
			lock.unlock();
		}
	}
	
}
