package fiber.app.server;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fiber.bean.SessionInfo;
import fiber.bean._.WrapperSessionInfo;
import fiber.common.Marshaller;
import fiber.common.Wrapper;
import fiber.db.BDBConfig;
import fiber.db.BDBStorage;
import fiber.db.Storage;
import fiber.io.Bean;
import fiber.io.Const;
import fiber.io.Log;
import fiber.io.MarshalException;
import fiber.io.OctetsStream;
import fiber.mapdb.Pair;
import fiber.mapdb.TValue;
import fiber.mapdb.Table;
import fiber.mapdb.WKey;
import fiber.mapdb.WValue;
import fiber.bean.*;

public class AllTable {
	private final static Map<Integer, Table> tables = new HashMap<Integer, Table>();
	public static Map<Integer, Table> getTables() { return tables; }
	public static Table getTable(int tableid) { return tables.get(tableid); }
	
	public static TValue getData(int tableid, WKey key) throws Exception {
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
	
	private final static Object flushLock = new Object();
	private final static OctetsStream kos = OctetsStream.create(64);
	private final static OctetsStream vos = OctetsStream.create(1024 * 1024);
	
	public static void flush() {
		synchronized(flushLock) {
			if(G.storage == null) {
				Log.notice("AllTable.flush  storage hasn't been initiated. omit");
				return;
			}
			Log.notice("======> AllTable. flush begin");
			Map<WKey, WValue> data = Transaction.getWaitCommitDataMap();
			TreeMap<Integer, ArrayList<Pair>> tableDatasMap = new TreeMap<Integer, ArrayList<Pair>>();
			for(Map.Entry<WKey, WValue> e : data.entrySet()) {
				WKey wkey = e.getKey();
				Object key = wkey.getKey();
				Object value = e.getValue().getCurValue();
				Table table = wkey.getTable();
				kos.clear();
				table.marshalKey(kos, key);
				vos.clear();
				if(value != null) {
					table.marshalValue(vos, value);
				}
				ArrayList<Pair> tableDatas = tableDatasMap.get(table.getId());
				if(tableDatas == null) {
					tableDatas = new ArrayList<Pair>();
					tableDatasMap.put(table.getId(), tableDatas);
				}
				tableDatas.add(new Pair(kos.toOctets(), vos.toOctets()));
			}
			if(G.storage.put(tableDatasMap)) {		
				Transaction.doneCommit();
			} else {
				Log.alert("AllTable.flush  storage.put fail. data num:%d", data.size());
			}
			Log.notice("======> AllTable.flush end");
		}
	}
	
	private static final ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor();
	public static void shrink() {
		for(Table table : tables.values()) {
			if(table.overmaxsize()) {
				Log.notice("AllTable.shrink. table:<%d> size:%s maxsize:%s. shrink begin.", table.getId(), table.size(), table.maxsize());
				table.shrink();
				Log.notice("AllTable.shrink. table:<%d> size:%s maxsize:%s. shrink end  .", table.getId(), table.size(), table.maxsize());
			}
		}
	}
	
	static {
		final int shrink_check_interval = Const.getProperty("shrink_check_interval", 6, 1, Integer.MAX_VALUE);
		scheduleExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				shrink();
			}
			
		}, shrink_check_interval, shrink_check_interval, TimeUnit.SECONDS);
		
		final int storage_flush_interval = Const.getProperty("storage_flush_interval", 30, 1, 1800);
		scheduleExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				flush();
			}
			
		}, storage_flush_interval, storage_flush_interval, TimeUnit.SECONDS);
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
	//  DTable Class defines 
	/////////////////////////////////////////////////////////
	public static class IntIntTable extends TableMem {
		public IntIntTable(int id, int maxsize) {
			super(id, maxsize, IntMarshaller, IntMarshaller);
		}
	}
	
	
	
	/////////////////////////////////////////////////////////
	//  db table defines 
	/////////////////////////////////////////////////////////
	public final static Table tUser;
	public final static Table tSession;

	static {
		register(tUser = new TableMem(1, 10, IntMarshaller, IntMarshaller));
		register(tSession = new TablePer(2, 10, IntMarshaller, new BeanMarshaller(SessionInfo.STUB)));
	}
	
	/////////////////////////////////////////////////////////
	//  table wrapper getter methods defines 
	/////////////////////////////////////////////////////////

	public static WrapperInt getUser(int uid) throws Exception {
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
	
	public static WrapperSessionInfo getSession(int sid) throws Exception {
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
	
	public static void testtable() throws Exception {
		BDBConfig conf = new BDBConfig();
		conf.setEnvRoot("e:/bdb_root");
		conf.setBackupRoot("e:/bdb_backup_root");
		conf.setIncrementalBackupInterval(0);
		conf.setFullBackupInterval(0);
		conf.setCacheSize(10 * 1024 * 1024);
		
		conf.AddDatabse(1, "user");
		conf.AddDatabse(2, "role");
		
		G.storage = BDBStorage.create(conf);
		
		
		Storage storage = G.storage;
		storage.truncateTable(1);
		storage.truncateTable(2);
		
		Transaction txn = Transaction.get();
		try {
			for(int i = 0 ; i < 20 ; i++) {
				 WrapperInt wi = getUser(i);
				 Log.trace("User[%d] = %s", i, wi);
				 if(wi.isNULL()) wi.set(i * 1000);
				 WrapperSessionInfo ws = getSession(i);
				 Log.trace("SessionInfo[%d] = %s", i, ws);
				 if(ws.isNULL()) ws.assign(new SessionInfo());
			}
			txn.commit();
		} catch(Exception e) {
			txn.rollback();
		} finally {
			txn.end();
		}
		flush();
		
		Log.trace("===============>");
		for(int i = 0 ; i < 20 ; i++) {
			 WrapperInt wi = getUser(i);
			 Log.trace("User[%d] = %s", i, wi);
			 WrapperSessionInfo ws = getSession(i);
			 Log.trace("SessionInfo[%d] = %s", i, ws);
			 ws.setlogintime(i * 1218);
			 ws.setuid(i);
			 ws.getroleids().add(i + 1000);
			 ws.getroleids().add(i + 2000);
		}
		txn.commit();
		txn.end();
		flush();
		
		Log.trace("===============>");
		for(int i = 0 ; i < 20 ; i++) {
			 WrapperInt wi = getUser(i);
			 Log.trace("User[%d] = %s", i, wi);
			 wi.assign(null);
			 WrapperSessionInfo ws = getSession(i);
			 Log.trace("SessionInfo[%d] = %s", i, ws);
			 ws.assign(null);
		}
		txn.commit();
		txn.end();	
		
		flush();
	}
	
	
	public static void main(String[] args) throws Exception {
		System.setProperty("log_level", Integer.valueOf(Log.LOG_ALL).toString());
		testtable();	
	}
}
