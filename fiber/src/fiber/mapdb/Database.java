package fiber.mapdb;
import java.util.HashMap;
import java.util.Map;

public class Database {
	/*
	public void update(Transaction txn, HashMap<TKey, TValue> dataMap) throws RedoException {

	}
	
	private final static ConcurrentHashMap<TKey, Bean<?>> modifyDataMap = new ConcurrentHashMap<TKey, Bean<?>>();
	*/
	private final static Map<Integer, Table> tables = new HashMap<Integer, Table>();
	public static Map<Integer, Table> getTables() { return tables; }
	public static Table getTable(int tableid) { return tables.get(tableid); }
	
	/**
	 * @param tableid
	 * @param key
	 * @return TValue
	 * @description 直接从表中取得数据,慎用.
	 */
	public static TValue getData(int tableid, TKey key) {
		return getTable(tableid).get(key);
	}
	
	/**
	 * 
	 * @param tableid
	 * @param key
	 * @return TValue
	 * @description 配合Transation系统,取得数据. 取数据的标准用法.
	 
	public static TValue getData(int tableid, TKey key) {
		Transaction txn = Transaction.get();
		TValue value = txn.getData(key);
		if(value != null) {
			return value;
		} else {
			value = getTable(tableid).get(key);
			txn.putData(key, value);
			return value;
		}
	}
	*/
}
