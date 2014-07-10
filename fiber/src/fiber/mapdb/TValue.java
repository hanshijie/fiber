package fiber.mapdb;

import fiber.io.Bean;

public final class TValue {
	private boolean modify;
	private Bean<?> value;
	TValue(Bean<?> value) {
		this.modify = false;
		this.value = value;
	}
	public boolean isModify() {
		return modify;
	}
	public void setModify(boolean modify) {
		this.modify = modify;
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
