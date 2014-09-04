package fiber.io;

import java.io.IOException;
import static fiber.io.Log.log;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public final class Connector extends IOHandler {
	public Connector(IOManager manager, IOPoller e, SelectableChannel channel) throws IOException {
		super(manager, e, channel);
	}
	
	protected void permitConnect() {
		// 有可能之前调用connect时已经成功连接了,
		// 对此不能保证还能收到 op_connect操作,
		// 但肯定有op_write.
		register(SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE | SelectionKey.OP_READ);
	}
	
	@Override
	protected void onHandle(SelectionKey key) {
		log.debug("Connector.onHandler {}:", this);
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
		log.error("Connector.onClose {}", this);
		this.getManager().onAbortSession(this);
	}
}
