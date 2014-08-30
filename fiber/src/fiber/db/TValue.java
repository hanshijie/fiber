package fiber.db;

import fiber.io.Timer;

public final class TValue {
	private boolean shrink = false;
	private int lastAccessTime = Timer.currentTime();
	private Object value;
	
	public TValue() {
		this.value = null;
	}
	
	public TValue(Object value) {
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public final int getLastAccessTime() {
		return lastAccessTime;
	}

	public final void setLastAccessTime(int lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public final boolean isShrink() {
		return shrink;
	}

	public final void setShrink(boolean shrink) {
		this.shrink = shrink;
	}

	@Override
	public String toString() {
		return String.format("TValue{shrink=%s, lastAccessTime:%s, value=%s}", shrink, lastAccessTime, value);
	}
}
