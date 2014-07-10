package fiber.io;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * RPC类型的bean(抽象类)
 * <p>
 * 包含请求和回复的两个bean
 */
public abstract class RpcBean<A extends Bean<A>, R extends Bean<R>> implements Bean<RpcBean<A, R>> {
	private static final AtomicInteger RPCID = new AtomicInteger(); 	// RPC的ID分配器
	private int _rpcid = RPCID.getAndIncrement() & 0x7fffffff;			// RPC的ID. 用于匹配请求和回复的RPC
	private int _reqtime; 												// 发送请求的时间戳(秒)
	private IOSession _session; 										// 请求时绑定的session
	private RpcHandler<A, R> _onclient; 								// 回复的回调
	protected A arg; 													// 请求bean
	protected R res; 													// 回复bean

	public final int getReqTime() {
		return _reqtime;
	}

	protected final void setReqTime(int time) {
		_reqtime = time;
	}

	public final IOSession getSession() {
		return _session;
	}

	protected final void setSession(IOSession session) {
		_session = session;
	}

	public final RpcHandler<A, R> getOnClient() {
		return _onclient;
	}

	protected final void setOnClient(RpcHandler<A, R> handler) {
		_onclient = handler;
	}

	/**
	 * 当前RPC的ID
	 * <p>
	 * 用于标识当前唯一的RPC,并和RPC的回复相配
	 */
	public final int getRpcId() {
		return _rpcid & 0x7fffffff;
	}

	/**
	 * 判断当前的RPC是否是请求RPC
	 * <p>
	 * RPC的ID最高位如果是0则表示请求RPC,否则是回复RPC
	 */
	public final boolean isRequest() {
		return _rpcid >= 0;
	}

	/**
	 * 设置当前的RPC为请求RPC
	 */
	public final void setRequest() {
		_rpcid &= 0x7fffffff;
	}

	/**
	 * 设置当前的RPC为回复RPC
	 */
	public final void setResponse() {
		_rpcid |= 0x80000000;
	}

	public final A getArg() {
		return arg;
	}

	public final void setArg(A a) {
		arg = a;
	}

	public final R getRes() {
		return res;
	}

	public final void setRes(R r) {
		res = r;
	}

	/**
	 * 获取超时时间(秒)
	 * <p>
	 * 当RPC的请求发出之后,如果一段时间没有收到此RPC的回复,就会出现超时<br>
	 * 子类应该实现这个方法返回合适的值,默认是10秒
	 */
	@SuppressWarnings("static-method")
	public int getTimeout() {
		return 10;
	}

	/**
	 * 创建一个新的请求bean实例
	 * <p>
	 * 子类的实现一般是new A(),返回对象的所有字段只有初始的默认值
	 */
	public abstract A createArg();

	/**
	 * 创建一个新的回复bean实例
	 * <p>
	 * 子类的实现一般是new R(),返回对象的所有字段只有初始的默认值
	 */
	public abstract R createRes();

	@Override
	public final OctetsStream marshal(OctetsStream os) {
		os.marshal(_rpcid);
		return _rpcid >= 0 ? arg.marshal(os) : res.marshal(os);
	}

	@Override
	public final OctetsStream unmarshal(OctetsStream os) throws MarshalException {
		_rpcid = os.unmarshalInt();
		if (_rpcid >= 0) {
			arg = createArg();
			arg.unmarshal(os);
		} else {
			res = createRes();
			res.unmarshal(os);
		}
		return os;
	}

	@Override
	public int hashCode() {
		int h = 0;
		if (arg != null)
			h += arg.hashCode();
		if (res != null)
			h += res.hashCode();
		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof RpcBean))
			return false;
		RpcBean<?, ?> b = (RpcBean<?, ?>) o;
		return (arg == b.arg || arg != null && arg.equals(b.arg))
				&& (res == b.res || res != null && res.equals(b.res))
				&& getClass() == o.getClass();
	}

	@Override
	public String toString() {
		return "{arg=" + arg + ",res=" + res + "}";
	}

}
