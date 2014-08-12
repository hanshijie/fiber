package fiber.mapdb;

public final class TValue {
	private boolean shrink;
	private Object value;
	TValue() {
		this.value = null;
		this.shrink = false;
	}
	
	TValue(Object value) {
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	public final boolean isShrink() {
		return shrink;
	}

	public final void setShrink(boolean shrink) {
		this.shrink = shrink;
	}

	@Override
	public String toString() {
		return String.format("TValue{shrink=%s, value=%s}", shrink, value);
	}
}
