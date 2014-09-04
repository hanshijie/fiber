package fiber.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import fiber.common.Marshaller;
import fiber.io.Bean;
import fiber.io.Const;
import fiber.io.MarshalException;
import fiber.io.Octets;
import fiber.io.OctetsStream;
import fiber.io.Timer;

public class Enviroment {
	private final static Logger log = LoggerFactory.getLogger(Enviroment.class);
	
	
	private final static int MAX_TABLE_ID = 0x100;
	private final static ArrayList<Table> tables = new ArrayList<Table>(MAX_TABLE_ID);
	private final static Map<Integer, Table> tableMap = new HashMap<Integer, Table>();
	static {
		for(int i = 0 ; i < MAX_TABLE_ID ; i++) {
			tables.add(null);
		}
	}
	
	public static void register(Table table) {
		Integer id = table.getId();
		if(id <= 0 || id >= MAX_TABLE_ID) throw new IllegalArgumentException(String.format("Table id<{}> is invalid!", id));
		if(tables.get(id) != null) throw new IllegalArgumentException(String.format("Table<{}> exists!", id));
		tables.set(id, table);
		tableMap.put(id, table);
	}
	
	private final static Object flushLock = new Object();
	private final static OctetsStream kos = OctetsStream.create(64);
	private final static OctetsStream vos = OctetsStream.create(1024 * 1024);
	private final static Marker FLUSH = MarkerFactory.getMarker("ENV FLUSH");
	
	public static void flush() {
		synchronized(flushLock) {
			final Storage storage = Storage.getInstance();
			if(storage == null) {
				log.warn(FLUSH, "storage hasn't been initiated. omit");
				return;
			}
			log.info(FLUSH, "======> begin");
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
			if(storage.put(tableDatasMap)) {		
				Transaction.doneCommit();
			} else {
				log.error(FLUSH, "storage.put fail. data num:{}", data.size());
			}
			log.info(FLUSH, "======> end");
		}
	}
	
	private static final Marker SHRINK = MarkerFactory.getMarker("SHRINK");
	public static void shrink() {
		long t1 = Timer.currentTimeMillis();
		for(Table table : tableMap.values()) {
			if(table.overmaxsize()) {
				log.info(SHRINK, "table:<{}> size:{} maxsize:{}. begin.", table.getId(), table.size(), table.maxsize());
				table.shrink();
				log.info(SHRINK, "table:<{}> size:{} maxsize:{}. end.", table.getId(), table.size(), table.maxsize());
			}
		}
		long t2 = Timer.currentTimeMillis();
		log.info(SHRINK, "all done! cost time:{}", t2 - t1);
	}
	
	private static final ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor();
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
	//  Wrapper defines for primity types
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
		
		public final void set(Integer value) {
			this.data = value;
			forceModify();
		}
	}
	
	public final static class WrapperLong extends Wrapper<Long> {
		public WrapperLong(Long w, Notifier n) {
			super(w, n);
		}

		@Override
		public Long shallowClone() {
			return this.origin_data;
		}
		
		public final Long get() {
			return this.data;
		}
		
		public final void set(Long value) {
			this.data = value;
			forceModify();
		}
	}
	
	public final static class WrapperOctets extends Wrapper<Octets> {
		public WrapperOctets(Octets w, Notifier n) {
			super(w, n);
		}

		@Override
		public Octets shallowClone() {
			return this.origin_data;
		}
		
		public final Octets get() {
			return this.data;
		}
		
		public final void set(Octets value) {
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
	
	public static Marshaller LongMarshaller = new Marshaller() {
		@Override
		public void marshal(OctetsStream os, Object key) {
			os.marshal((long)(Long)key);
		}

		@Override
		public Long unmarshal(OctetsStream os) throws MarshalException {
			return os.unmarshalLong();
		}
		
	};
	
	public static Marshaller OctetsMarshaller = new Marshaller() {
		@Override
		public void marshal(OctetsStream os, Object key) {
			os.marshal((Octets)key);
		}

		@Override
		public Octets unmarshal(OctetsStream os) throws MarshalException {
			return os.unmarshalOctets();
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
}
