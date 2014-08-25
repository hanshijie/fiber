package fiber.io;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetManager extends IOManager {

	private final Map<Integer, BeanHandler<?>> handlerStub;

	private static class BeanCodecFactory extends ProtocolCodec.Factory {
		final private Map<Integer, BeanHandler<?>> handlerStub;
		public BeanCodecFactory(final Map<Integer, BeanHandler<?>> handlerStub) {
			this.handlerStub = handlerStub;
		}
		@Override
		public ProtocolCodec createCodec() {
			return new BeanCodec(handlerStub);
		}
		
	}
	
	public NetManager(IOPoller e, final Map<Integer, BeanHandler<?>> handlerStub) {
		super(e,  new BeanCodecFactory(handlerStub));
		this.handlerStub = handlerStub;
	}

	synchronized public void broadcastMessage(Object msg) {
		Log.debug("broadcast");
		for(IOSession s : this.getSessions().values()) {
			s.send(msg);
		}	
	}

	private static final ConcurrentHashMap<Integer, RpcBean<?, ?>> _rpcs = new ConcurrentHashMap<Integer, RpcBean<?, ?>>(); // 当前管理器等待回复的RPC
	private final static  ScheduledExecutorService rpcExecutor = Executors.newSingleThreadScheduledExecutor();
	static {
		rpcExecutor.scheduleAtFixedRate(
			new Runnable() {
				@Override
				public void run() {
					int now = Timer.currentTime();
					for (Map.Entry<Integer, RpcBean<?, ?>> e : _rpcs.entrySet()) {
						RpcBean<?, ?> rpcbean = e.getValue();
						if (now - rpcbean.getReqTime() > rpcbean.getTimeout() && _rpcs.remove(e.getKey()) != null) {
							RpcHandler<?, ?> onclient = rpcbean.getOnClient();
							IOSession session = rpcbean.getSession();
							handleTimeout(onclient, session, rpcbean.getArg());
						}
					}
				}
		}, Const.rpc_check_timeout_interval, Const.rpc_check_timeout_interval, TimeUnit.SECONDS);
	}
	
	public static  RpcBean<?, ?> removeRpc(int rpcid) {
		return _rpcs.remove(rpcid);
	}
	
	protected static void schedule(Runnable task, long delay) {
		rpcExecutor.schedule(task, delay, TimeUnit.SECONDS);
	}
	
	protected static void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
		rpcExecutor.scheduleAtFixedRate(task, period, period, TimeUnit.SECONDS);
	}

	/**
	 * 向某个连接发送RPC
	 * <p>
	 * 此操作是异步的
	 * @param handler 可设置一个回调对象,用于在RPC回复和超时时回调. null表示使用注册的处理器处理回复和超时(RpcHandler.onClient/onTimeout)
	 * @return 如果连接已经失效则返回false, 否则返回true。
	 * 	原先设计是如果连接失效,不执行onTimeout,虽然从逻辑上合理，但导致同时在发送失败与超时两处写逻辑，
	 *  给使用者带来不便，所以修改了做法。为什么不在失效时立即调用onTimeout呢？因为很多情形下,onTimeout里
	 *  会试图重试，如果立即调用，会导致疯狂递归onTimeout而崩溃。
	 */
	@SuppressWarnings("unchecked")
	public <A extends Bean<A>, R extends Bean<R>> boolean sendRpc(final IOSession session, final RpcBean<A, R> rpcbean, RpcHandler<A, R> handler)
	{
		assert(rpcbean.getArg() != null);
		rpcbean.setRequest();
		final RpcHandler<A, R> fhandler = handler != null ? handler : (RpcHandler<A, R>)this.handlerStub.get(rpcbean.type());		
		rpcbean.setOnClient(fhandler);
		if(session == null) {
			NetManager.schedule(new Runnable() {
				@Override
				public void run() {
					handleTimeout(fhandler, session, rpcbean.getArg());
				}			
			}, rpcbean.getTimeout());
			Log.notice("sendRpc timeout");
			return false;
		}
		rpcbean.setReqTime((int)(System.currentTimeMillis() / 1000));
		rpcbean.setSession(session);
		session.send(rpcbean); 
		_rpcs.put(rpcbean.getRpcId(), rpcbean);
		return true;
	}
	
	private static <A extends Bean<A>, R extends Bean<R>> 
	void handleTimeout(final RpcHandler<A, R> handler, final IOSession session, final Object arg) {
		try {
			handler.timeout(session, arg);
		} catch (Exception e) {
			Log.alert("handlerTimeout. RpcHandler:%s arg:%s exception:%s", handler, arg, e);
		}	
	}
	
	public <A extends Bean<A>, R extends Bean<R>> boolean sendRpc(final IOSession session, final RpcBean<A, R> rpcbean)	{
		return sendRpc(session, rpcbean, null);
	}

	@Override
	public void onReceiveMessage(final IOSession session, Object message) {
		final Bean<?> bean = (Bean<?>)message;
		final BeanHandler<?> handler = this.handlerStub.get(bean.type());
		try {
			handler.process(session, bean);
		} catch (Exception e) {
			Log.alert("onReceiveMessage. BeanHandler:%s bean:%s exception:%s", handler, bean, e);
			e.printStackTrace();
		}
	}

}
