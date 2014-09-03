package fiber.common;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import fiber.io.Log;

public class CallbackFuture<V> implements Future<V> {
	private volatile boolean done;
	private volatile V result;
	public CallbackFuture() {
		this.done = false;
	}

	/**
	 * 不支持取消!
	 */
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public V get() throws InterruptedException {
		while(true) {
			synchronized(this) {
				if(this.done) {
					return this.result;
				} else {
					this.wait();
				}
			}
		}
	}

	/**
	 * 不支持带超时
	 */
	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		throw new TimeoutException();
	}

	@Override
	public boolean isCancelled() {
		return done;
	}

	@Override
	public boolean isDone() {
		return done;
	}
	
	public void set(V value) {
		synchronized(this) {
			this.result = value;
			this.done = true;
			this.notifyAll();
		}
	}

	public static void main(String[] args) {
		final CallbackFuture<String> f = new CallbackFuture<String>();
		for(int i = 0 ; i < 5; i++) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					Log.info("try get");
					try {
						String result = f.get();
						Log.info("result:%s", result);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
				
			});
			t.start();
		}
		
		try {
			Thread.sleep(2000);
			f.set("hell");
			Thread.sleep(2000);
			Log.info("exit..");
		} catch (InterruptedException e) {

		}
		

	}

}
