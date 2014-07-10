package fiber.io;

/**
 * RPC处理器的基类(抽象类)
 * <p>
 * 此类是BeanHandler的子类
 * 如果是rpc的server端,应该重载 onServer
 * 如果是rpc的client端,应该重载onClient和onTimeout.
 */
public abstract class RpcHandler<A extends Bean<A>, R extends Bean<R>> extends BeanHandler<RpcBean<A, R>>
{
	public boolean onServer(IOSession session, A arg, R res) throws Exception {
		Log.alert("bean<%s>.onServer unprocess!", this.getClass().getSimpleName());
		return true;
	}

	public void onClient(IOSession session, A arg, R res) throws Exception {
		Log.alert("bean<%s>.onClient unprocess!", this.getClass().getSimpleName());
	}

	public void onTimeout(IOSession session, A arg) throws Exception {
		Log.warn("bean<%s>.onTimeout unprocess!", this.getClass().getSimpleName());
	}

	
	
	
	
	
	@SuppressWarnings("unchecked")
	final void timeout(IOSession session, Object arg) throws Exception {
		onTimeout(session, (A)arg);
	}

	@Override
	public void onProcess(IOSession session, RpcBean<A, R> rpcbean)	throws Exception {
		if (rpcbean.isRequest()) {
			rpcbean.setRes(rpcbean.createRes());
			if (onServer(session, rpcbean.getArg(), rpcbean.getRes())) {
				rpcbean.setArg(null);
				rpcbean.setResponse();
				session.send(rpcbean);
			}
		} else {
			@SuppressWarnings("unchecked")
			RpcBean<A, R> rpcbean_old = (RpcBean<A, R>) NetManager.removeRpc(rpcbean.getRpcId());
			if (rpcbean_old != null) {
				RpcHandler<A, R> onClient = rpcbean_old.getOnClient();
				onClient.onClient(session, rpcbean_old.getArg(), rpcbean.getRes());
			}
		}
	}
	
}
