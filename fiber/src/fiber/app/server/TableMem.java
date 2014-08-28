package fiber.app.server;

import java.util.Map;

import fiber.common.LockPool;
import fiber.common.Marshaller;
import fiber.mapdb.TValue;
import fiber.mapdb.Table;
import fiber.mapdb.WKey;

public class TableMem extends Table {
	
	public final static class RemoveNullPolicy implements ShrinkPolicy {
		@Override
		public boolean check(Object key, TValue value) {
			return value.getValue() == null;
		}
	}
	
	private final static RemoveNullPolicy DEFAULT_POLICY = new RemoveNullPolicy();

	public TableMem(int id, int maxsize, Marshaller msKey,
			Marshaller msValue, ShrinkPolicy policy) {
		super(id, false, maxsize, msKey, msValue, policy);
	}
	
	public TableMem(int id, int maxsize, Marshaller msKey,
			Marshaller msValue) {
		super(id, false, maxsize, msKey, msValue, DEFAULT_POLICY);
	}

	@Override
	public void shrink() {
		LockPool pool = LockPool.getInstance();
		ShrinkPolicy policy = this.getPolicy();
		for(Map.Entry<Object, TValue> e : this.getDataMap().entrySet()) {
			Object key = e.getKey();
			TValue value = e.getValue();
			if(policy.check(key, value)) {
				int lockid = pool.lockid(WKey.keyHashCode(this.getId(), key));
				pool.lock(lockid);
				try {
					// double check.
					if(policy.check(key, value)) {
						remove(key);
					}	
				} finally {
					pool.unlock(lockid);
				}
			}
		}
		
	}

	@Override
	public void walk(Walk w) {
		for(Map.Entry<Object, TValue> e : this.getDataMap().entrySet()) {
			if(!w.onProcess(this, e.getKey(), e.getValue())) break;
		}
	}

	@Override
	public void walkCache(Walk w) {
		walk(w);
	}

}
