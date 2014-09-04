package fiber.io;

import java.io.IOException;

import java.nio.channels.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static fiber.io.Log.log;

public final class IOPoller {
	private final int id;
	private final Selector selector;
	private final Object eventLock;
	
	private static class Event {
		public final int opers;
		public final IOHandler handler;
		public Event(int opers, IOHandler handler) {
			this.opers = opers;
			this.handler = handler;
		}
	}
	private Map<SelectableChannel, Event> changedChannelMap;
	private Set<SelectableChannel> removeChannelSet;
	
	public IOPoller(int id) throws IOException {
		this.id = id;
		this.selector = Selector.open();
		this.eventLock = new Object();
		this.changedChannelMap = new HashMap<SelectableChannel, Event>();
		this.removeChannelSet = new HashSet<SelectableChannel>();
	}
	
	@Override
	public String toString() {
		return "IOPoller-" + id;
	}
	
	public void register(SelectableChannel channel, int opers, IOHandler handler) {
		synchronized(this.eventLock) {
			if(this.changedChannelMap.isEmpty() && this.removeChannelSet.isEmpty()) {
				this.selector.wakeup();
			}
			if(!this.removeChannelSet.contains(channel)) {
				this.changedChannelMap.put(channel, new Event(opers, handler));
			}
		}	
	}
	
	public void close(SelectableChannel channel) {
		synchronized(this.eventLock) {
			if(this.changedChannelMap.isEmpty() && this.removeChannelSet.isEmpty()) {
				this.selector.wakeup();
			}
			this.removeChannelSet.add(channel);
			this.changedChannelMap.remove(channel);
		}	
	}
	
	public void poll(long timeout) throws IOException {
		final Map<SelectableChannel, Event> new_event_map = this.changedChannelMap;
		final Set<SelectableChannel> remove_channel_set = this.removeChannelSet;
		synchronized (this.eventLock) {
			this.changedChannelMap = new HashMap<SelectableChannel, Event>();
			this.removeChannelSet = new HashSet<SelectableChannel>();
		}

		for (SelectableChannel channel : remove_channel_set) {
			SelectionKey key = channel.keyFor(this.selector);
			if (key != null) {
				IOHandler handler = (IOHandler) key.attachment();
				if (!handler.isClosing()) {
					handler.setClosing(true);
					handler.onClose();
					channel.close();
				}
			}
		}

		for (Map.Entry<SelectableChannel, Event> entry : new_event_map.entrySet()) {
			Event e = entry.getValue();
			if (e.handler.isClosing()) continue;
			SelectableChannel channel = entry.getKey();
			channel.register(this.selector, e.opers, e.handler);
		}

		int ready_channel_num = this.selector.select(timeout);
		log.debug("{} ready channel num:{}", this, ready_channel_num);
		if(ready_channel_num == 0) return;
		final Set<SelectionKey> keys = this.selector.selectedKeys();
		for (SelectionKey key : keys) {
			IOHandler handler = (IOHandler) key.attachment();
			handler.onHandle(key);
		}
		keys.clear();
	}
	
	public void runBackground() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						poll(0);
					}
				} catch(Exception e) {
					log.error("IOPoller.runBackground. exception.", e);
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}

}
