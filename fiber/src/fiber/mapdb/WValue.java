package fiber.mapdb;

import fiber.io.Bean;

public final class WValue {
	private final TValue tvalue;
	private final Bean<?> originValue;
	private Bean<?> curValue;
	public WValue(TValue tv, Bean<?> ov, Bean<?> cv) {
		this.tvalue = tv;
		this.originValue = ov;
		this.curValue = cv;
	}
	
	public final Bean<?> getCurValue() {
		return curValue;
	}
	
	public final void setCurValue(Bean<?> curValue) {
		this.curValue = curValue;
	}
	
	public final TValue getTvalue() {
		return tvalue;
	}
	
	public final Bean<?> getOriginValue() {
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
