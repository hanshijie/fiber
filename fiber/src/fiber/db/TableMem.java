package fiber.db;

import java.util.Map;

import fiber.common.Marshaller;

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
