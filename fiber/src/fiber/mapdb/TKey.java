package fiber.mapdb;

import fiber.io.Bean;


public final class TKey {
	private final Table table;
	private final Bean<?> key;
	
	TKey(Table table, Bean<?> key) {
		this.table = table;
		this.key = key;
	}
	
	public final int getTableid() {
		return table.getId();
	}
	
	public final Table getTable() {
		return table;
	}

	public final Bean<?> getKey() {
		return key;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof TKey) {
			TKey k = (TKey)o;
			return this.table == k.table && this.key.equals(k.key);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.getTableid() * 31 + key.hashCode();
	}
	
	@Override
	public String toString() {
		return String.format("TKey{tableid=%d, key=%s}", this.table.getId(), this.key);
	}
}
