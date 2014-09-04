package fiber.app.lockserver;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static fiber.io.Log.log;
import fiber.io.Timer;
import fiber.io.Octets;
import fiber.io.OctetsStream;

public final class LockManager {
	private static enum Status {
		IDLE,
		ACQUIRING,
		ACQUIRED,
		LOCKED,
	}
	private final static long MIN_LOCK_DURATION = 10; // 10 ms
	public static class Value {
		private int gsid;
		private long accessTimestamp;
		private long permitAcquireTimestamp;
		private long version;
		private Deque<OrderLockAcquire> waitgs;
		Status status;
		Value(int g) {
			this.gsid = g;
			this.accessTimestamp = 0;
			this.permitAcquireTimestamp = 0;
			this.version = 0;
			this.waitgs = null;
			this.status = Status.ACQUIRED;
		}
		
		public final int getGsid() {
			return gsid;
		}

		public final void setGsid(int gsid) {
			this.gsid = gsid;
		}

		public final long getAccessTimestamp() {
			return accessTimestamp;
		}
		public final void setAccessTimestamp(long accessTimestamp) {
			this.accessTimestamp = accessTimestamp;
		}
		public final long getPermitAcquireTimestamp() {
			return permitAcquireTimestamp;
		}
		public final void setPermitAcquireTimestamp(long retainTimestamp) {
			this.permitAcquireTimestamp = retainTimestamp;
		}
		public final long getVersion() {
			return version;
		}
		public final void setVersion(long version) {
			this.version = version;
		}
		public final Deque<OrderLockAcquire> getWaitgs() {
			return waitgs;
		}
		public final void setWaitgs(Deque<OrderLockAcquire> waitgs) {
			this.waitgs = waitgs;
		}
		
		public final boolean isLocked() {
			return this.status == Status.LOCKED;
		}
		
		public final void clearLock() {
			assert(this.status == Status.LOCKED);
			this.gsid = 0;
			this.status = Status.IDLE;
		}
		
		public final boolean acquire(OrderLockAcquire req) {
			if(this.status == Status.IDLE || (this.gsid == req.getGsid() && this.status == Status.LOCKED)) {
				this.status = Status.ACQUIRED;
				return true;
			} else {
				if(this.waitgs == null) {
					this.waitgs = new LinkedList<OrderLockAcquire>();
				} else if(this.waitgs.contains(req)) {
					return false;
				}
				this.waitgs.offer(req);
				
				if(this.status == Status.LOCKED) {
					this.status = Status.ACQUIRING;
					long now = Timer.currentTimeMillis();
					if(now >= this.permitAcquireTimestamp) {
						LockManager.addInstantAcquire(this);
					} else {
						LockManager.addDeferAcquire(this);
					}
				}
				return false;
			}
		}
		
		public void doneAcquire(long version) {
			OrderLockAcquire req = this.waitgs.poll();
			this.gsid = req.getGsid();
			this.version = version;
			this.status = Status.ACQUIRED;
			
			req.setCurLock(this);
			lockall(req);
		}
		
		public void doneLock(long now) {
			this.accessTimestamp = now;
			this.permitAcquireTimestamp = now + MIN_LOCK_DURATION;
			if(this.waitgs == null || this.waitgs.isEmpty()) {
				this.status = Status.LOCKED;
			} else {
				LockManager.addDeferAcquire(this);
			}
		}
	}
	
	private static Deque<Value> instantAcquires = new LinkedList<Value>();
	private static ArrayList<Value> deferAcquires = new ArrayList<Value>();
	
	private final static Map<Octets, Value> lockMap = new HashMap<Octets, Value>();
	
	public static void addInstantAcquire(Value value) {
		instantAcquires.add(value);
		log.debug("addInstantAcquire. value:{} size:{}", value, instantAcquires.size());
	}
	
	public static void addDeferAcquire(Value value) {
		deferAcquires.add(value);
		log.debug("addDeferAcquire. value:{} size:{}", value, deferAcquires.size());
	}
	
	public static void processAcquires() {
		// TODO just for test.
		log.debug("processAllAcquires======================");
		long now = Timer.currentTimeMillis();
		
		{
			log.debug("processInstantAcquire==============");
			Deque<Value> acquires = instantAcquires;
			instantAcquires = new LinkedList<Value>();
			for(Value value : acquires) {
				value.doneAcquire(now);
			}
		}
		{
			log.debug("ProcessDeferAcquire================");
			ArrayList<Value> acquires = deferAcquires;
			deferAcquires = new ArrayList<Value>();
			for(Value value : acquires) {
				if(value.getPermitAcquireTimestamp() < now) {
					value.doneAcquire(now);
				} else {
					deferAcquires.add(value);
				}
			}
		}
	}
	
	public static void depriveGSLocks(int gsid) {
		assert(gsid > 0);
		for(Value value : lockMap.values()) {
			// 只清除处理锁定状态的locks. 其他的
			if(value.getGsid() == gsid && value.isLocked()) {
				value.clearLock();
			}
		}
	}
	
	public static void lockall(OrderLockAcquire req) {
		for(Octets key = req.getNextKey() ; key != null ; key = req.getNextKey()) {
			log.debug("lock:{}", key.dump());
			Value lock = lockMap.get(key);
			if(lock != null) {
				if(lock.acquire(req)) {
					log.debug("lock:{}. acquire succ.", key.dump());
					req.setCurLock(lock);
				} else {
					log.debug("lock:{}. acquiring.", key.dump());
					return;
				}
			} else {
				lock = new Value(req.getGsid());
				lockMap.put(key, lock);
				req.setCurLock(lock);
				log.debug("lock:{}. create and acquire succ.", key.dump());
			}
		}
		req.doneLock();
		// TODO
		// notify req.gs  has got all locks;
		log.info("gs:{} get all locks.", req.getGsid());
	}
	
	static Octets makeKey(long key) {
		return OctetsStream.create(10).marshal(key).toRefOctets();
	}
	
	static ArrayList<Octets> makeKeys(long begin, long end) {
		ArrayList<Octets> keys = new ArrayList<Octets>();
		for(long k = begin ; k <= end ; k++) {
			keys.add(makeKey(k));
		}
		return keys;
	}
	
	static void lockrange(int gs, long begin, long end) {
		ArrayList<Octets> keys1 = makeKeys(begin, end);
		OrderLockAcquire ola1 = new OrderLockAcquire(gs, keys1);
		lockall(ola1);	
	}
	
	public static void main(String[] args) throws Exception {
		lockrange(1, 5, 10);
		lockrange(2, 2, 7);
		lockrange(3, 7, 11);
		
		for(long ver = 1 ; ver < 10 ; ver++) {
			processAcquires();
			Thread.sleep(100);
		}
		
	}

}
