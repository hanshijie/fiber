package fiber.io;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

abstract public class IOHandler {
	private final IOPoller poller;
	private final IOManager manager;
	private final SelectableChannel channel;
	private boolean isClosing;

	public IOHandler(IOManager manager, IOPoller thread, SelectableChannel channel) {
		this.poller = thread;
		this.manager = manager;
		this.channel = channel;
		this.isClosing = false;
	}
	
	protected final IOPoller getPoller() {
		return this.poller;
	}
	
	public final IOManager getManager() {
		return this.manager;
	}
	
	protected final SelectableChannel getChannel() {
		return this.channel;
	}
	
	protected final void register(int opers) {
		this.poller.register(channel, opers, this);
	}
	
	protected final boolean isClosing() {
		return isClosing;
	}

	protected final void setClosing(boolean isClosing) {
		this.isClosing = isClosing;
	}

	protected final void close() {
		this.poller.close(this.channel);
	}
	
	protected abstract void onHandle(SelectionKey key);
	protected abstract void onClose();
}
