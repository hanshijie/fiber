package fiber.db;

import fiber.db.Transaction.Dispatcher;
import static fiber.io.Log.log;

public abstract class Procedure implements Runnable {	
	protected Transaction txn;
	protected Dispatcher net;
	public final void run() {
		try {
			this.txn = Transaction.get();
			this.txn.prepare();
			this.net = txn.getDispatcher();
			
			for(int i = 0 ;  ; i++) {
				try {
					execute();
					this.txn.commit();
					try {
						this.onDone();
					} catch(Exception e) {
						log.error("{}. {}.onDone Exception.", this.txn, this);
						log.error("", e);
					}
					return;
				}
				catch(ConflictException ce) {
					if(i == 0) {
						// 如果是第一次失败,不释放锁,重做
						this.txn.rollbackHoldLocks();
					} else {
						// 如果意外地第二次也失败了(当且仅当每次执行需要获取的锁不同,这种事情相当罕见)
						// 释放所有锁,休眠一段时间,重做
						this.txn.rollback();
						Thread.sleep(i);
					}
				}
			}
		} catch (RetException ret) {
			this.onRetError(ret.getRetcode(), ret.getContent());
		} catch(Exception e) {
			// 如果在script engine里触发的exception,可能会被重新包装过.故.
			Throwable t = e.getCause();
			if(t instanceof RetException) {
				RetException ret = (RetException)t;
				this.onRetError(ret.getRetcode(), ret.getContent());
			} else {
				this.onException(e);
			}
		} finally {
			this.txn.end();
		}
	}
	
	abstract protected void execute() throws Exception;
	abstract protected void onRetError(int retcode, Object content);
	protected void onDone() {}
	
	protected void onException(Exception e) {
		log.error("{} {}", this.txn, this);
		log.error("", e);
	}

}
