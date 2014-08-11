package fiber.mapdb;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import fiber.bean.BInteger;
import fiber.common.LockPool;
import fiber.io.Log;

public class Transaction {
	/*
	private final static ThreadLocal<Transaction> contexts = new ThreadLocal<Transaction>() {
		@Override
		public Transaction initialValue() {
			return new Transaction();
		}
	};
	
	
	public static Transaction get() {
		return contexts.get();
	}
	*/
	private final HashMap<TKey, WValue> dataMap;
	private final TxnLogger logger;	
	private final TreeSet<Integer> lockSet;
	public Transaction() {
		this.dataMap = new HashMap<TKey, WValue>();
		this.logger = new TxnLogger();
		this.lockSet = new TreeSet<Integer>();
	}
	
	public final TxnLogger getLogger() {
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
			if(value.isConflict()) {
				// 一般来说,检查到冲突后会redo,出于优化考虑
				// 不释放锁.
				Log.info("%s confliction detected!", this);
				throw ConflictException.INSTANCE;
			}
		}
		for(Map.Entry<TKey, WValue> e : this.dataMap.entrySet()) {
			TKey key = e.getKey();
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
	
	public final WValue getData(TKey key) {
		return this.dataMap.get(key);
	}
	
	public final void putData(TKey key, WValue value) {
		this.dataMap.put(key, value);
	}
	
	public final HashMap<TKey, WValue> getDataMap() {
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
			for(TKey key : this.dataMap.keySet()) {
				int lockid = lp.lockid(key.hashCode());
				this.lockSet.add(lockid);
			}
			doLock();
		} else {
			for(TKey key : this.dataMap.keySet()) {
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
	
	public static void main(String[] argv) {
		LockPool.init(133);
		Transaction txn = new Transaction();
		Table table = new Table(1, 100, 1000000);
		int N = 100;
		for(int i = N ; i > 0 ; i--) {
			txn.putData(new TKey(table, new BInteger(i)), new WValue(new TValue(), null, null));
		}
		txn.lock();
		txn.rollback();
		txn.unlock();
	}
}
