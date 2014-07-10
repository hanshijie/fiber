package fiber.common;

import java.util.HashMap;
import java.util.Map;

import fiber.io.Bean;
import fiber.io.Log;
import fiber.mapdb.TKey;

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
	
	private final HashMap<TKey, Bean<?>> dataMap;
	private  final HashMap<Runnable, Long> scheduleTasks;
	private final TxnLogger logger;
	
	public Transaction() {
		this.dataMap = new HashMap<TKey, Bean<?>>();
		this.logger = new TxnLogger();
		this.scheduleTasks = new HashMap<Runnable, Long>();
	}
	
	public final TxnLogger getLogger() {
		return this.logger;
	}

	private final void clearDatas() {
		this.dataMap.clear();
		this.logger.clear();
		this.scheduleTasks.clear();
	}
	
	public final void prepare() {
		//this.clearDatas();
	}
	
	public void commit() throws Exception {
		Log.info("%s commit. start.", this);
		this.lock();
		// 提交数据
		DB.instance.update(this.dataMap);
		// 提交日志
		this.logger.commit();

		// 提交定时器任务
		for (Map.Entry<Runnable, Long> entry : this.scheduleTasks.entrySet()) {
			Runnable task = entry.getKey();
			long delay = entry.getValue();
			if(delay == 0) {
				try {
					task.run();
				} catch (Exception e) {
					Log.alert("Transation.commit. task exception:%s", e);
				}		
			} else {
				TaskPool.schedule(task, delay);
			}
		}
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
	
	public final Object getData(TKey key) {
		return this.dataMap.get(key);
	}
	
	public final Bean<?> putData(TKey key, Bean<?> value) {
		return this.dataMap.put(key, value);
	}
	
	public final HashMap<TKey, Bean<?>> getDataMap() {
		return this.dataMap;
	}
	
	/**
	 * 添加一个延迟回调task.这个task将在本事务成功提交后被调度.
	 * 执行task时,已经脱离了受保护的事务环境,因为如果涉及到受管的bean数据,
	 * 只能作读操作,切不可修改,否则违背了事务的语义.
	 * 回调task中所做事情一般是发协议
	 * 或者其他非事务相关的事情.如果想在回调中做另外一件事务,请
	 * 创建一个 事务TaskQueue.Task, 加入某TaskQueue中.
	 */
	public final void addScheduleTask(Runnable task, long delay) {
		this.scheduleTasks.put(task, delay);
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
