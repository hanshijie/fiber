package fiber.io;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public final class Connector extends IOHandler {
	public Connector(IOManager manager, IOPoller e, SelectableChannel channel) throws IOException {
		super(manager, e, channel);
	}
	
	protected void permitConnect() {
		register(SelectionKey.OP_CONNECT);
	}
	
	@Override
	protected void onHandle(SelectionKey key) {
		Log.debug("Connector-%s:", this);
		SocketChannel channel = (SocketChannel)getChannel();
		try {
			if(channel.finishConnect()) {
				IOTransportor handler = new IOTransportor(getManager(), getPoller(), channel);
				handler.onOpen();
			} else {
				close();
			}
		} catch (IOException e) {
			close();
		}
	}

	@Override
	protected void onClose() {
		Log.err("[Connector-%s] fail", this);
		this.getManager().onAbortSession(this);
	}
}
