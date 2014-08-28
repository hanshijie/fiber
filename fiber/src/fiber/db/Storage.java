package fiber.db;

import java.util.ArrayList;
import java.util.Map;

import fiber.io.Octets;
import fiber.mapdb.Pair;

public abstract class Storage {
	public abstract Octets getData(int tableid, Octets key);
	public abstract boolean putData(int tableid, Octets key, Octets value);
	
	public abstract Map<Integer, ArrayList<Octets>> getDatas(Map<Integer, ArrayList<Octets>> tableDatasMap);
	public abstract boolean putDatas(Map<Integer, ArrayList<Pair>> tableDatasMap);
	
	public abstract void walk(int tableid, Octets begin, Walker w);
	public void walk(int tableid, Walker w) { walk(tableid, Octets.EMPTY, w); }
	
	public abstract boolean truncateTable(int tableid);
	
	public abstract void close();
}
