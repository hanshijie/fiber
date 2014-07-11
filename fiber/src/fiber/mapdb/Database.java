package fiber.mapdb;
import java.util.HashMap;
import java.util.Map;

import fiber.mapdb.Procedure.RedoException;

public class Database {
	public static Database instance = new Database();
	public void update(HashMap<TKey, TValue> dataMap) throws RedoException {

	}

	private final static Map<Integer, Table> tables = new HashMap<Integer, Table>();
	public static Map<Integer, Table> getTables() { return tables; }
	public static Table getTable(int tableid) { return tables.get(tableid); }
	
	/**
	 * 
	 * @param tableid
	 * @param key
	 * @return TValue
	 * @description 直接从表中取得数据,慎用.
	 */
	public static TValue getDataRaw(int tableid, TKey key) {
		return getTable(tableid).get(key);
	}
	
	/**
	 * 
	 * @param tableid
	 * @param key
	 * @return TValue
	 * @description 配合Transation系统,取得数据. 取数据的标准用法.
	 */
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
}
