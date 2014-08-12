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

	public static Map<WKey, Object> getWaitCommitDataMap() {
		waitCommitReadLock.lock();
		try{
			Map<WKey, Object> inCommitDataMap = waitCommitDataMap;
			waitCommitDataMap = new ConcurrentHashMap<WKey, Object>();
			return inCommitDataMap;
		} finally {
			waitCommitReadLock.unlock();
		}
	}

	protected void commitModifyData() {
		waitCommitWriteLock.lock();
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
			waitCommitWriteLock.unlock();
		}
	}
}
