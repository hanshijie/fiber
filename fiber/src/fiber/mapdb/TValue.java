package fiber.mapdb;

import fiber.io.Bean;

public final class TValue {
	private boolean loaded;
	private boolean modify;
	private Bean<?> value;
	TValue() {
		this.loaded = false;
		this.modify = false;
		this.value = null;
	}
	public boolean isModify() {
		return modify;
	}
	public void setModify(boolean modify) {
		this.modify = modify;
	}
	
	public final boolean isLoaded() {
		return loaded;
	}
	public final void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	public Bean<?> getValue() {
		return value;
	}
	public void setValue(Bean<?> value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return String.format("TValue{modify=%s, value=%s}", modify, value);
	}
}
