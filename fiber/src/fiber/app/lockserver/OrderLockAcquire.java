package fiber.app.lockserver;

import java.util.ArrayList;
import java.util.TreeSet;

import fiber.io.Timer;
import fiber.io.Octets;

public final class OrderLockAcquire {
	private final int gsid;
	private final ArrayList<Octets> orderkeys;
	private final ArrayList<LockManager.Value> orderLocks;
	private int nextKeyIndex;
	public OrderLockAcquire(int gsid, ArrayList<Octets> unorderLocks) {
		this.gsid = gsid;
		TreeSet<Octets> lockset = new TreeSet<Octets>(unorderLocks);
		this.orderkeys = new ArrayList<Octets>(lockset);
		this.orderLocks = new ArrayList<LockManager.Value>(this.orderkeys.size());
		this.nextKeyIndex = 0;
	}
	
	public Octets getNextKey() {
		if(this.nextKeyIndex < this.orderkeys.size()) {
			return this.orderkeys.get(this.nextKeyIndex++);
		} else {
			return null;
		}
	}
	
	public void setCurLock(LockManager.Value lock) {
		this.orderLocks.add(lock);
	}
	
	public void doneLock() {
		assert(this.orderkeys.size() == this.orderLocks.size());
		long now = Timer.currentTimeMillis();
		for(LockManager.Value value : this.orderLocks) {
			value.doneLock(now);
		}
	}
	
	public final int getGsid() {
		return gsid;
	}

	public final ArrayList<Octets> getOrderLocks() {
		return orderkeys;
	}

	public static void main(String[] args) {

	}

}
