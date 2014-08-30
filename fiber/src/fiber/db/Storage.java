package fiber.db;

import java.util.ArrayList;
import java.util.Map;

import fiber.io.Octets;

public abstract class Storage {
	private static Storage instance;
	public static void setInstance(Storage ins) { instance = ins; }
	public static Storage getInstance() { return instance; }
	
	public abstract Octets get(int tableid, Octets key);
	public abstract boolean put(int tableid, Octets key, Octets value);
	
	public abstract Map<Integer, ArrayList<Octets>> get(Map<Integer, ArrayList<Octets>> tableDatasMap);
	public abstract boolean put(Map<Integer, ArrayList<Pair>> tableDatasMap);
	
	public abstract boolean del(int tableid, Octets key);
	
	public abstract void walk(int tableid, Octets begin, Walker w);
	public void walk(int tableid, Walker w) { walk(tableid, Octets.EMPTY, w); }
	
	public abstract boolean truncateTable(int tableid);
	
	public abstract void checkpoint() throws Exception;
	
	public abstract void close();
}
