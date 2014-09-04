package fiber.db;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import fiber.io.Const;
import static fiber.io.Log.log;

public final class LockPool {
	private final static Marker LOCK = MarkerFactory.getMarker("LOCK");
	private static final LockPool instance = new LockPool(Const.getProperty("lock_pool_size", 1024 * 8));
	public static LockPool getInstance() { return instance; }
	
	final private Lock[] locks;
	final int mask;
	LockPool(int locksize) {
		if(locksize < 32 || locksize > 1024 * 1024) throw new IllegalArgumentException("Illegle locksize:" + locksize);
		locksize = probSize(locksize);
		mask = locksize - 1;
		locks = new Lock[locksize];
		for(int i = 0 ; i < locksize ; i++) {
			locks[i] = new ReentrantLock();
		}
		log.info(LOCK, "init. locksize:{} mask:{}", locksize, mask);
	}
	
	public int lockid(int hash) {
		return hash & mask;
	}
	
	
	public void lock(int lockid) {
		log.debug(LOCK, "lock:{}", lockid);
		this.locks[lockid].lock();
	}
	
	public void lock(Collection<Integer> c) {
		for(int lockid : c) {
			lock(lockid);
		}
	}
	
	public void unlock(int lockid) {
		log.debug(LOCK, "unlock:{}", lockid);
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
