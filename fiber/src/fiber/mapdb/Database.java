package fiber.mapdb;
import java.util.HashMap;
import java.util.Map;

public class Database {
	private final static Map<Integer, Table> tables = new HashMap<Integer, Table>();
	public static Map<Integer, Table> getTables() { return tables; }
	public static Table getTable(int tableid) { return tables.get(tableid); }
	
	public static TValue getData(int tableid, WKey key) {
		return getTable(tableid).get(key);
	}
	
	public static void register(Table table) {
		tables.put(table.getId(), table);
	}
	
	public static void unregister(int tableid) {
		tables.remove(tableid);
	}
	
}
