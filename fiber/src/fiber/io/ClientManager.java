package fiber.io;

import java.io.IOException;
import java.util.Map;

public class ClientManager extends NetManager {
	private boolean reconn;
	private int backoff;
	public ClientManager(IOPoller e, Map<Integer, BeanHandler<?>> handlerStub) {
		super(e, handlerStub);
		this.onlysession = null;
		this.reconn = true;
		this.backoff = Const.reconnect_init_backoff;
	}
	
	@Override
	public void onAddSession(IOSession session) {
		super.onAddSession(session);
		this.onlysession = session;
		this.backoff = Const.reconnect_init_backoff;
	}
	
	@Override 
	public void onDelSession(IOSession session) {
		super.onDelSession(session);
		this.onlysession = null;
		if(this.reconn) {
			reconnect();
		}
	}
	
	@Override
	protected void onAbortSession(Connector connector) {
		if(this.reconn) {
			reconnect();
		}
	}
	
	private final void reconnect() {
		Log.trace("%s reconnect. backoff:%d", this, this.backoff);
		schedule(new Runnable() {
			@Override
			public void run() {
				try {
					ClientManager.this.startClient();
				} catch (IOException e) {
					Log.warn("[%s] reconnect fail. exception:%s", this, e);
				}
			}
			
		}, this.backoff);
		this.backoff <<= 1;
		if(this.backoff > Const.reconnect_max_backoff) {
			this.backoff = Const.reconnect_max_backoff;
		}		
	}
	
	public final <A extends Bean<A>, R extends Bean<R>> boolean sendRpc(final RpcBean<A, R> rpcbean, RpcHandler<A, R> handler) {
		return this.sendRpc(this.onlysession, rpcbean, handler);
	}
	
	public final <A extends Bean<A>, R extends Bean<R>> boolean sendRpc(final RpcBean<A, R> rpcbean) {
		return this.sendRpc(this.onlysession, rpcbean);
	}
	
	public  final void writeMessage(Object msg) {
		if(this.onlysession != null) {
			this.onlysession.send(msg);
		}
	}
	
	public final boolean isConnected() {
		return this.onlysession != null;
	}
	
	private IOSession onlysession;
}
