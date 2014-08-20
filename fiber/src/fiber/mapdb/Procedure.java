package fiber.mapdb;

import fiber.common.RetException;
import fiber.io.Log;
import fiber.mapdb.Transaction.Dispatcher;
import fiber.mapdb.Transaction.Logger;

public abstract class Procedure implements Runnable {
	private final int maxRedoCount;
	public Procedure(int maxRedoCount) {
		this.maxRedoCount = maxRedoCount;
	}
	
	public Procedure() {
		this(10);
	}
	
	/**
	 * 设置好正确的txn上下文.
	 */
	protected abstract void prepare();
	
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
			this.onException(e);
		} finally {
			this.end();
		}
	}
	
	abstract protected void execute() throws Exception;
	
	protected void onRetError(int retcode, Object content) {
		Log.err("%s.onRetError. retcode:%d content:%s", this, retcode, content);
	}
	protected void onException(Exception e) {
		Log.err("%s.onException. exception:%s", this, e);
	}
	protected void onFail() {
		Log.err("%s.onFail.", this);
	}
}
