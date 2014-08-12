package fiber.mapdb;


public final class WKey {
	private final Table table;
	private final Object key;
	
	public WKey(Table table, Object key) {
		this.table = table;
		this.key = key;
	}
	
	public final int getTableid() {
		return table.getId();
	}
	
	public final Table getTable() {
		return table;
	}

	public final Object getKey() {
		return key;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof WKey) {
			WKey k = (WKey)o;
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
		return String.format("WKey{tableid=%d, key=%s}", this.table.getId(), this.key);
	}
}
