package fiber.common;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fiber.io.Log;

public final class LockPool {
	private static LockPool instance;
	public static void init(int initsize) {
		instance = new LockPool(initsize);
	}
	public static LockPool getInstance() { return instance; }
	
	final private Lock[] locks;
	final int mask;
	public LockPool(int locksize) {
		if(locksize < 32 || locksize > 1024 * 1024) throw new IllegalArgumentException("Illegle locksize:" + locksize);
		locksize = probSize(locksize);
		mask = locksize - 1;
		locks = new Lock[locksize];
		for(int i = 0 ; i < locksize ; i++) {
			locks[i] = new ReentrantLock();
		}
	}
	
	public int lockid(int hash) {
		return hash & mask;
	}
	
	public void lock(int lockid) {
		Log.debug("LockPool.lock:%d", lockid);
		this.locks[lockid].lock();
	}
	
	public void lock(Collection<Integer> c) {
		for(int lockid : c) {
			lock(lockid);
		}
	}
	
	public void unlock(int lockid) {
		Log.debug("LockPool.unlock:%d", lockid);
		this.locks[lockid].unlock();
	}
	
	public void unlock(Collection<Integer> c) {
		for(int lockid : c) {
			unlock(lockid);
		}
	}
	
	private int probSize(int size) {
		int finalSize = 32;
		while(finalSize < size) 
			finalSize <<= 1;
		return finalSize;
	}
}
