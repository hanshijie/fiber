package fiber.mapdb;

import fiber.io.Bean;

public final class TValue {
	private Bean<?> value;
	TValue() {
		this.value = null;
	}
	
	TValue(Bean<?> value) {
		this.value = value;
	}
	
	public Bean<?> getValue() {
		return value;
	}
	public void setValue(Bean<?> value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return String.format("TValue{value=%s}", value);
	}
}
