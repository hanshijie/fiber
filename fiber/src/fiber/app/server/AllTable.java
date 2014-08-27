package fiber.app.server;
import java.util.HashMap;
import java.util.Map;

import fiber.bean.SessionInfo;
import fiber.common.Marshaller;
import fiber.common.Wrapper;
import fiber.io.Bean;
import fiber.io.Log;
import fiber.io.MarshalException;
import fiber.io.OctetsStream;
import fiber.mapdb.TValue;
import fiber.mapdb.Table;
import fiber.mapdb.WKey;
import fiber.mapdb.WValue;

import fiber.bean.*;
import static fiber.bean._.*;

public class AllTable {
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
	
	
	/////////////////////////////////////////////////////////
	//  WValue Notifier defines 
	/////////////////////////////////////////////////////////
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
	
	/////////////////////////////////////////////////////////
	//  Wrapper defines 
	/////////////////////////////////////////////////////////
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
		
	}	
	/////////////////////////////////////////////////////////
	//  marshaller defines 
	/////////////////////////////////////////////////////////
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
	
	public static class BeanMarshaller implements Marshaller {
		private final Bean<?> stub;
		public BeanMarshaller(Bean<?> stub) {
			this.stub = stub;
		}
		@Override
		public void marshal(OctetsStream os, Object key) {
			((Bean<?>)key).marshal(os);
		}

		@Override
		public Object unmarshal(OctetsStream os) throws MarshalException {
			Bean<?> obj = this.stub.create();
			obj.unmarshal(os);
			return obj;
		}
	}
	
	public static class BeanSchemeMarshaller implements Marshaller {
		private final Bean<?> stub;
		public BeanSchemeMarshaller(Bean<?> stub) {
			this.stub = stub;
		}
		@Override
		public void marshal(OctetsStream os, Object key) {
			((Bean<?>)key).marshalScheme(os);
		}

		@Override
		public Object unmarshal(OctetsStream os) throws MarshalException {
			Bean<?> obj = this.stub.create();
			obj.unmarshalScheme(os);
			return obj;
		}
	}
	
	/////////////////////////////////////////////////////////
	//  Table Class defines 
	/////////////////////////////////////////////////////////
	public static class IntIntTable extends TableMem {
		public IntIntTable(int id, boolean persist, int maxsize) {
			super(id, persist, maxsize, IntMarshaller, IntMarshaller);
		}
	}
	
	
	
	/////////////////////////////////////////////////////////
	//  db table defines 
	/////////////////////////////////////////////////////////
	public final static Table tUser;
	public final static Table tSession;

	static {
		register(tUser = new IntIntTable(1, false, 1000 * 1000));
		register(tSession = new TableMem(2, false, 1000 * 1000, IntMarshaller, new BeanMarshaller(SessionInfo.STUB)));
	}
	
	/////////////////////////////////////////////////////////
	//  table wrapper getter methods defines 
	/////////////////////////////////////////////////////////

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
	
	public static WrapperSessionInfo getSession(int sid) {
		Transaction txn = Transaction.get();
		WKey key = new WKey(tSession, sid);
		WValue value = txn.getData(key);
		if(value != null) {
			return (_.WrapperSessionInfo)value.getWrapper();
		} else {
			final TValue v = tSession.get(sid);
			value = new WValue(v);
			WrapperSessionInfo wrap = new WrapperSessionInfo((SessionInfo)v.getValue(), new WValueNotifier(value));
			value.setWrapper(wrap);
			txn.putData(key, value);
			return wrap;
		}
	}
	
	public static void test1() {
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
	
	public static void test2() {
		int N = 5;
		for(int i = 1 ; i < N ; i++) {
			WrapperSessionInfo wrap = getSession(i);
			if(wrap.isNULL()) wrap.assign(new SessionInfo());
		}
		
		Transaction txn = Transaction.get();
		txn.dump();
		
		for(int i = 0 ; i < N * 2 ; i++) {
			TValue value = tSession.get(i);
			Log.trace("%s => %s", i, value);
		}
		
		try {
			txn.commit();
			txn.end();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		{
			WrapperSessionInfo w = getSession(1);
			w.setuid(1218);
			try {
				txn.commit();
				txn.end();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.trace("%s", getSession(1));
		}
		{
			WrapperSessionInfo w = getSession(1);

			w.assign(new SessionInfo());

			
			try {
				txn.commit();
				txn.end();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.trace("%s", getSession(1));
		}
	}
	
	public static void main(String[] args) {
		System.setProperty("log_level", Integer.valueOf(Log.LOG_ALL).toString());
		test2();
		
	}
}
