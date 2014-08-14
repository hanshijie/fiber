package fiber.app.server;
import java.util.HashMap;
import java.util.Map;

import fiber.common.LockPool;
import fiber.common.Marshaller;
import fiber.io.Log;
import fiber.io.MarshalException;
import fiber.io.OctetsStream;
import fiber.io.SystemConfig;
import fiber.mapdb.TValue;
import fiber.mapdb.Table;
import fiber.mapdb.WKey;
import fiber.mapdb.WValue;

public class Database {
	private final static Map<Integer, Table> tables = new HashMap<Integer, Table>();
	public static Map<Integer, Table> getTables() { return tables; }
	public static Table getTable(int tableid) { return tables.get(tableid); }
	
	public static TValue getData(int tableid, WKey key) {
		return getTable(tableid).get(key);
	}
	
	public static void register(Table table) {
		Integer id = table.getId();
		assert(!tables.containsKey(id));
		tables.put(id, table);
	}
	
	public static void unregister(int tableid) {
		tables.remove(tableid);
	}
	
	public static Marshaller IntMarshaller = new Marshaller() {
		@Override
		public void marshal(OctetsStream os, Object key) {
			os.marshal((int)(Integer)key);
		}

		@Override
		public Integer unmarshal(OctetsStream os) throws MarshalException {
			return os.unmarshalInt();
		}
		
	};
	
	public static class WValueNotifier extends Wrapper.Notifier {
		private final WValue value;
		public WValueNotifier(WValue v) {
			this.value = v;
		}
		@Override
		public void onChange(Object o) {
			this.value.setCurValue(o);
		}
	}
	
	public static class IntIntTable extends Table {
		public IntIntTable(int id, boolean persist) {
			super(id, persist, IntMarshaller, IntMarshaller);
		}
	}
	
	public final static Table tUser = new IntIntTable(1, true);
	public final static class WrapperInt extends Wrapper<Integer> {
		public WrapperInt(Integer w, Notifier n) {
			super(w, n);
		}

		@Override
		public Integer shallowClone() {
			return this.origin_data;
		}
		
		public final Integer get() {
			return this.data;
		}
		
		public final void set(int value) {
			this.data = value;
			forceModify();
		}

		@Override
		public void commit() { }
		
	}
	
	public static WrapperInt getUser(int uid) {
		Transaction txn = Transaction.get();
		WKey key = new WKey(tUser, uid);
		WValue value = txn.getData(key);
		if(value != null) {
			Log.trace("getUser from txn. key:%s", key);
			return (WrapperInt)value.getWrapper();
		} else {
			Log.trace("getUser from table. key:%s", key);
			final TValue v = tUser.get(uid);
			value = new WValue(v);
			WrapperInt wrap = new WrapperInt((Integer)v.getValue(), new WValueNotifier(value));
			value.setWrapper(wrap);
			txn.putData(key, value);
			return wrap;
		}
	}
	
	static {
		register(tUser);
	}
	
	
	public static void main(String[] args) {
		SystemConfig.getInstance().setLogLevel(0);
		LockPool.init(32);
		int N = 5;
		for(int i = 1 ; i < N ; i++) {
			WrapperInt wrap = getUser(i);
			wrap.set(i * i);
		}
		
		Transaction txn = Transaction.get();
		txn.dump();
		
		for(int i = 0 ; i < N * 2 ; i++) {
			TValue value = tUser.get(i);
			Log.trace("%s => %s", i, value);
		}
		
		try {
			//tUser.get(1).setShrink(true);
			//tUser.get(1).setValue(1218);
			txn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for(int i = 0 ; i < N * 2 ; i++) {
			TValue value = tUser.get(i);
			Log.trace("%s => %s", i, value);
		}
		
	}
}
