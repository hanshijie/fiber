package fiber.mapdb;

import fiber.io.Bean;


public final class TKey {
	private final int tableid;
	private final Bean<?> key;
	TKey(int tableid, Bean<?> key) {
		this.tableid = tableid;
		this.key = key;
	}
	
	public final int getTableid() {
		return tableid;
	}

	public final Bean<?> getKey() {
		return key;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof TKey) {
			TKey k = (TKey)o;
			return this.tableid == k.tableid && this.key.equals(k.key);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.tableid * 31 + key.hashCode();
	}
	
	@Override
	public String toString() {
		return String.format("TKey{tableid=%d, key=%s}", this.tableid, this.key);
	}
}
