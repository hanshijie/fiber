package fiber.mapdb;

import fiber.common.RetException;
import fiber.io.Log;

public abstract class Procedure {
	public static final class  RedoException extends Exception {
		public static final RedoException instance = new RedoException();
		private static final long serialVersionUID = 7272522830324985095L;
		private RedoException() {
			super("", null, false, false);
		}
	}

	private final int maxRedoCount;
	public Procedure(int maxRedoCount) {
		this.maxRedoCount = maxRedoCount;
	}
	
	public Procedure() {
		this(10);
	}
	
	protected final void prepare() {
		Log.info("%d. procedure:%s start.", Thread.currentThread().getId(), this);
		this.txn = Transaction.get();
		this.txn.prepare();	
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
	public final void process() throws Exception {
		try {
			this.prepare();
			for(int i = 0 ; i < this.maxRedoCount ; i++) {
				try {
					execute();
					this.commit();
					return;
				}
				catch(RedoException ce) {
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
		Log.err("[thread-%d]. onRetError. retcode:%d content:%s", Thread.currentThread().getId(), retcode, content);
	}
	protected void onFail() {
		Log.err("%d. procedure:%s fail!", Thread.currentThread().getId(), this);
	}
}
