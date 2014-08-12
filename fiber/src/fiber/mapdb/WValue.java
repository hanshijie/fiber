package fiber.mapdb;

public final class WValue {
	private final TValue tvalue;
	private final Object originValue;
	private Object curValue;
	public WValue(TValue tv, Object ov, Object cv) {
		this.tvalue = tv;
		this.originValue = ov;
		this.curValue = cv;
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
	
	public boolean isConflict() { 
		return tvalue.getValue() != this.originValue;
	}
	
	public boolean isModify() {
		return originValue != curValue;
	}
	
	public void commit() {
		tvalue.setValue(curValue);
	}
	
}
