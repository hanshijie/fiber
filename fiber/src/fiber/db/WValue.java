package fiber.db;

import fiber.io.Timer;

public final class WValue {
	private final TValue tvalue;
	private final Object originValue;
	private Object curValue;
	private Object wrapper;
	public WValue(TValue tv) {
		this.tvalue = tv;
		this.curValue = this.originValue = tv.getValue();
	}
	
	public final Object getCurValue() {
		return curValue;
	}
	
	public final void setCurValue(Object curValue) {
		this.curValue = curValue;
	}
	
	public final TValue getTvalue() {
		return tvalue;
	}
	
	public final Object getOriginValue() {
		return originValue;
	}
	
	public final Object getWrapper() {
		return wrapper;
	}
	
	public final void setWrapper(Object wrapper) {
		this.wrapper = wrapper;
	}

	public boolean isConflict() { 
		return tvalue.getValue() != this.originValue;
	}
	
	public boolean isModify() {
		return originValue != curValue;
	}
	
	public void commit() {
		if(isModify()) {
			tvalue.setValue(curValue);
		}
		tvalue.setLastAccessTime(Timer.currentTime());
	}
	
	@Override
	public String toString() {
		return isModify() ? 
			String.format("WValue{tvalue=%s, wrapper=%s, origin=%s, cur=%s}", this.tvalue, this.wrapper, this.originValue, this.curValue) :
			String.format("WValue{tvalue=%s, wrapper=%s, origin=%s}", this.tvalue, this.wrapper, this.originValue);
	}
}
