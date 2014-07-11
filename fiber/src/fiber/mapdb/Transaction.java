package fiber.mapdb;

import java.util.HashMap;
import fiber.io.Log;

public class Transaction {
	private final static ThreadLocal<Transaction> contexts = new ThreadLocal<Transaction>() {
		@Override
		public Transaction initialValue() {
			return new Transaction();
		}
	};
	
	public static Transaction get() {
		return contexts.get();
	}
	
	private final HashMap<TKey, TValue> dataMap;
	private final TxnLogger logger;	
	public Transaction() {
		this.dataMap = new HashMap<TKey, TValue>();
		this.logger = new TxnLogger();
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
		Database.instance.update(this.dataMap);
		this.logger.commit();
		Log.info("%s commit. end.", this);
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
	
	public final TValue getData(TKey key) {
		return this.dataMap.get(key);
	}
	
	public final void putData(TKey key, TValue value) {
		this.dataMap.put(key, value);
	}
	
	public final HashMap<TKey, TValue> getDataMap() {
		return this.dataMap;
	}
	
	/**
	 * 由于经常性的不一致性导致redo,需要尽量减少redo次数,
	 * 一个非常有效的优化是, 如果redo, 不释放此次已经锁定的lock,
	 * 绝大多数情况下(>99%),redo里使用到的lock应该和当前已经锁定的lock完全相同,
	 * 那么由于已经事先持有锁,本次操作肯定会成功.
	 */
	public void lock() {
		
	}
	
	public final void unlock() {

	}
	
	public static void main(String[] argv) {
		Transaction txn = Transaction.get();
		txn.lock();
		txn.rollback();
		txn.unlock();
	}
}
