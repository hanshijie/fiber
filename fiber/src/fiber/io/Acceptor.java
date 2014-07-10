package fiber.io;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


public final class Acceptor extends IOHandler {
	
	public Acceptor(IOManager manager, IOPoller e, ServerSocketChannel channel) {
		super(manager, e, channel);
	}
	
	public IOConfig getConfig() {
		return this.getManager().getConfig();
	}
	
	public void permitAccept() {
		register(SelectionKey.OP_ACCEPT);
	}
	
	public void forbidAccept() {
		register(0);
	}
	
	@Override
	protected void onHandle(SelectionKey key) {
		//Log.trace("Acceptor-%s:", this);
		ServerSocketChannel server = (ServerSocketChannel)getChannel();
		SocketChannel client = null;
		try {
			client = server.accept();
		} catch (IOException e) {
			Log.alert("[%s] accept fail! exception:%s", this, e);
			close();
			return;
		}
		
		try {
			client.configureBlocking(false);
			IOConfig conf = getConfig();
			Socket socket = client.socket();
			socket.setKeepAlive(conf.isKeepAlive());
			socket.setTcpNoDelay(conf.isTpcNoDelay());
			socket.setSendBufferSize(conf.getSendBufferSize());
			socket.setReceiveBufferSize(conf.getRecvBufferSize());
			
			IOTransportor handler = new IOTransportor(getManager(), this.getPoller(), client);
			handler.onOpen();
		} catch (IOException e) {
			Log.alert("[%s] init new connection fail! exception:%s", this, e);
			try {
				client.close();
			} catch (IOException e1) {
				Log.err("Acceptior. close new connection:%s fail! exception:%s", client, e1);
			}
		}
		
	}

	@Override
	protected void onClose() {
		Log.fatal("[Accpetor-%s] close!", this);
	}
	
}
