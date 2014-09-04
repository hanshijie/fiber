package fiber.db;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import fiber.db.Transaction.Dispatcher;
import static fiber.io.Log.log;

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
		this.txn.prepare();
	}
	
	protected final void rollback() {
		this.txn.rollback();
	}
	
	protected final void commit() throws Exception {
		this.txn.commit();
	}

	private final static Marker PROCEDURE = MarkerFactory.getMarker("PROCEDURE"); 
	protected final void end() {
		this.txn.end();
		log.debug(PROCEDURE, "{} end.", this.txn);
	}
	
	protected Transaction txn;
	protected Dispatcher net;
	public final void run() {
		try {
			this.prepare();
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
	
	protected final void trigger(int retcode) throws RetException {
		RetException.trigger(retcode);
	}
	
	protected final void trigger(int retcode, Object content) throws RetException {
		RetException.trigger(retcode, content);
	}
	
	abstract protected void execute() throws Exception;
	abstract protected void onRetError(int retcode, Object content);
	
	protected void onException(Exception e) {
		log.error(PROCEDURE, "{} onException. {}", this.txn, e);
		log.error(PROCEDURE, "", e);
	}
	protected void onFail() {
		log.error(PROCEDURE, "{} {}.onFail.", this.txn, this);
	}
}
