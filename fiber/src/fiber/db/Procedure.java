package fiber.db;

import fiber.db.Transaction.Dispatcher;
import fiber.db.Transaction.Logger;
import fiber.io.Log;

public abstract class Procedure implements Runnable {
	private final int maxRedoCount;
	public Procedure(int maxRedoCount) {
		this.maxRedoCount = maxRedoCount;
	}
	
	public Procedure() {
		this(10);
	}
	
	protected void prepare() {
		this.txn = Transaction.get();
	}
	
	protected final void rollback() {
		this.txn.rollback();
	}
	
	protected final void commit() throws Exception {
		this.txn.commit();
	}

	protected final void end() {
		this.txn.end();
		Log.info("%s. procedure:%s end.", Thread.currentThread(), this);
	}
	
	protected Transaction txn;
	protected Logger log;
	protected Dispatcher net;
	public final void run() {
		try {
			this.prepare();
			this.log = txn.getLogger();
			this.net = txn.getDispatcher();
			
			for(int i = 0 ; i < this.maxRedoCount ; i++) {
				try {
					execute();
					this.commit();
					return;
				}
				catch(ConflictException ce) {
					this.rollback();
				}
			}
			this.onFail();
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
			this.end();
		}
	}
	
	abstract protected void execute() throws Exception;
	abstract protected void onRetError(int retcode, Object content);
	
	protected void onException(Exception e) {
		Log.err("%s.onException. exception:%s", this, e);
		e.printStackTrace();
	}
	protected void onFail() {
		Log.alert("%s.onFail.", this);
	}
}
