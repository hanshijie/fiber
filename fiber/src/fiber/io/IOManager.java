package fiber.io;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class IOManager {
	private final IOPoller poller;
	private final IOConfig config;
	private final Map<Integer, IOSession> sessionMap;
	private final ProtocolCodec.Factory factory;
	
	public IOManager(IOPoller e, ProtocolCodec.Factory factory) {
		this.poller = e;
		this.config = new IOConfig();
		this.sessionMap = new ConcurrentHashMap<Integer, IOSession>();
		this.factory = factory;
	}
	
	public final IOPoller getPoller() {
		return this.poller;
	}
	
	public final IOConfig getConfig() {
		return this.config;
	}

	public final ProtocolCodec.Factory getFactory() {
		return factory;
	}

	protected void onAddSessionIntern(IOSession s) {
		Log.trace("onAddSession:%d", s.getId());
		this.sessionMap.put(s.getId(), s);
		onAddSession(s);
	}
	
	protected void onDelSessionIntern(IOSession s) {
		Log.trace("onDelSession:%d", s.getId());
		this.sessionMap.remove(s.getId());
		onDelSession(s);
	}
	
	public abstract void onReceiveMessage(IOSession session, Object message);
	
	protected void onIdle(IOSession session) {
		session.close();
	}
	
	protected void checkIdle(final int interval) {
		int minLastActiveTime = Timer.currentTime() - interval;
		for(IOSession session : this.sessionMap.values()) {
			if(session.getLastActiveTime() < minLastActiveTime) {
				this.onIdle(session);
			}
		}
	}
	
	protected void onAddSession(IOSession s) {	}
	
	protected void onDelSession(IOSession s) {	}
	
	protected void onAbortSession(Connector connector) { }
	
	public final Map<Integer, IOSession> getSessions() {
		return this.sessionMap;
	}
	
	public final void broadcast(Octets data) {
		Log.debug("broadcast");
		for(IOSession s : this.sessionMap.values()) {
			s.write(data);
		}	
	}
	
	public final void closeAll() {
		Log.debug("closeAll");
		for(IOSession s : this.sessionMap.values()) {
			s.close();
		}
	}
		
	public void startClient() throws IOException {
		IOConfig conf = getConfig();
		SocketChannel sc = SocketChannel.open();
		sc.configureBlocking(false);
		
		Socket socket = sc.socket();
		socket.setKeepAlive(conf.isKeepAlive());
		socket.setTcpNoDelay(conf.isTpcNoDelay());
		socket.setSendBufferSize(conf.getSendBufferSize());
		socket.setReceiveBufferSize(conf.getRecvBufferSize());
		
		if(sc.connect(conf.getAddr())) {
			/*
			IOTransportor handler = new IOTransportor(this, getPoller(), sc);
			handler.onOpen();
			*/
			Log.trace("directly conn succ. addr:%s", conf.getAddr());
		}
		Connector conn = new Connector(this, getPoller(), sc);
		conn.permitConnect();
	}
	
	public Acceptor startServer() throws IOException {
		IOConfig conf = getConfig();
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		
		ServerSocket socket = ssc.socket();
		socket.setReuseAddress(conf.isReuseAddress());
		
		ssc.bind(conf.getAddr(), conf.getBacklog());
		Acceptor acceptor = new Acceptor(this, getPoller(), ssc);
		acceptor.permitAccept();
		return acceptor;
	}

}
