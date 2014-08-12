package fiber.app.server;

import fiber.io.Log;


public abstract class Wrapper<W> {
	public static abstract class Notifier {
		public abstract void onChange(Object o);
	}
	
	public static Notifier NONE_NOTIFIER = new Notifier() {
		public void onChange(Object o) { }
	};
	
	protected final W origin_data;
	protected W data;
	protected final Notifier notifier;
	
	public Wrapper(W w, Notifier n) {
		this.origin_data = w != null ? w : getNULL();
		this.data = this.origin_data;
		this.notifier = n;
	}
	
	@Override
	public String toString() {
		return  this.isModify() ? "{" 
				+ " origin_data = " + this.origin_data
				+ ", data = " + this.data + " }"
				: "{ data = " + this.data + " }";
	}
	
	public final boolean isModify() {
		return this.data != this.origin_data;
	}
	
	public final void checkModify() {
		if(!isModify()) {
			this.data = this.shallowClone();
			Log.debug("BeanW.checkModify. origin_data:%s data:%s", this.origin_data, this.data);
			notifier.onChange(this.data);
		}
	}
	
	/**
	 * 请在调用此接口前务必先设置新的data
	 */
	public final void forceModify() {
		//this.data = this.shallowClone();
		Log.debug("BeanW.forceModify. origin_data:%s data:%s", this.origin_data, this.data);
		notifier.onChange(this.data);
	}
	
	public final boolean isNULL() {
		return this.data == this.getNULL();
	}
	
	public abstract W getNULL();
	public abstract W shallowClone();
	public abstract W create();

	public final void assign(W w) {
		this.data = w != null ? w : getNULL();
		Log.debug("BeanW.assign. origin_data:%s data:%s", this.origin_data, this.data);
		notifier.onChange(this.data);
	}
	
	public final void reset() {
		this.data = create();
		Log.debug("BeanW.reset. origin_data:%s data:%s", this.origin_data, this.data);
		notifier.onChange(this.data);
	}
	
	/**
	 * 
	 * @param w
	 * @desc  注意!如果想更新data, 使用assign, 而不是refresh！！！
	 */
	public void refresh(W w) {
		this.data = w != null ? w : getNULL();
	}
	
	protected final W internalGetOriginData() {
		return this.origin_data;
	}
	
	protected final W internalGetData() {
		return this.data;
	}
	
	/** 
	 * 	开放接口给架构用的。普通用户千万不要试图使用此接口访问原始数据
	 */
	public static <V> V getOringinData(Wrapper<V> w) {
		return w.internalGetOriginData();
	}
	
	/** 
	 * 	开放接口给架构用的。普通用户千万不要试图使用此接口访问当前数据
	 */
	public static <V> V getData(Wrapper<V> w) {
		return w.internalGetData();
	}
}
