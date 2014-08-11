package fiber.mapdb;

import fiber.common.RetException;
import fiber.io.Log;

public abstract class Procedure {
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
	/*
		Log.info("%s. prepare.", Thread.currentThread());
		this.txn = Transaction.get();
		this.txn.prepare();	
	 */
	
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
	public final void process() throws Exception {
		try {
			this.prepare();
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
		} finally {
			this.end();
		}
	}
	
	abstract protected void execute() throws Exception;
	
	protected void onRetError(int retcode, Object content) {
		Log.err("%s. onRetError. retcode:%d content:%s", Thread.currentThread(), retcode, content);
	}
	protected void onFail() {
		Log.err("%s. procedure:%s fail!", Thread.currentThread(), this);
	}
}
