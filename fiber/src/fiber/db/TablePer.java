package fiber.db;

import java.util.Map;

import fiber.common.Marshaller;
import fiber.io.Const;
import static fiber.io.Log.log;
import fiber.io.Octets;
import fiber.io.OctetsStream;
import fiber.io.Timer;

public class TablePer extends Table {
	public final static class RemoveExpirePolicy implements ShrinkPolicy {
		private final int expireDuration;
		public RemoveExpirePolicy(int expireDuration) {
			this.expireDuration = expireDuration;
		}
		@Override
		public boolean check(Object key, TValue value) {
			return value.getLastAccessTime() + this.expireDuration < Timer.currentTime();
		}		
	}
	
	private final static int DEFAULT_SHRINK_EXPIRE_TIME = Const.getProperty("table_persist_shrink_expire_time", 600, 1, Integer.MAX_VALUE);
	private final static RemoveExpirePolicy DEFAULT_POLICY = new RemoveExpirePolicy(DEFAULT_SHRINK_EXPIRE_TIME);

	public TablePer(int id, int maxsize, Marshaller msKey,
			Marshaller msValue, ShrinkPolicy policy) {
		super(id, true, maxsize, msKey, msValue, policy);
	}
	
	public TablePer(int id, int maxsize, Marshaller msKey,
			Marshaller msValue) {
		super(id, true, maxsize, msKey, msValue, DEFAULT_POLICY);
	}

	@Override
	protected TValue loadValue(Object key) throws Exception {
		TValue tvalue = new TValue();
		if(Transaction.getDirtyData(new WKey(this, key), tvalue)) return tvalue;
		OctetsStream os = OctetsStream.create(8);
		this.marshalKey(os, key);
		Octets ovalue = Storage.getInstance().get(this.getId(), os.toOctets());
		if(ovalue != null) {
			OctetsStream vos = OctetsStream.wrap(ovalue);
			tvalue.setValue(this.unmarshalValue(vos));
		}
		return tvalue;
	}
	
	
	@Override
	public void walk(final Walk w) {
		Storage.getInstance().walk(this.getId(), new Walker() {
			@Override
			public boolean onProcess(Octets key, Octets value) {
				try {
					Object okey = unmarshalKey(OctetsStream.wrap(key));
					Object ovalue = unmarshalValue(OctetsStream.wrap(value));
					return w.onProcess(TablePer.this, okey, new TValue(ovalue));
				} catch(Exception e) {
					log.error("TablePer.walk.", e);
					return true;
				}
			}
		});
		
	}

	@Override
	public void walkCache(Walk w) {
		for(Map.Entry<Object, TValue> e : this.getDataMap().entrySet()) {
			if(!w.onProcess(this, e.getKey(), e.getValue())) return;
		}
	}

}
