package fiber.io;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static fiber.io.Log.log;

public final class IOTransportor extends IOHandler {
	private final Object operLock;
	private int opers;
	private final ByteBuffer inputBuffer;
	private final ByteBuffer outputBuffer;
	private IOSession session;
	
	public IOTransportor(IOManager manager, IOPoller thread,
			SelectableChannel channel) {
		super(manager, thread, channel);
		this.operLock = new Object();
		this.opers = 0;
		
		IOConfig config = manager.getConfig();
		this.inputBuffer = ByteBuffer.allocate(config.getInBufferSize());
		this.outputBuffer = ByteBuffer.allocate(config.getOutBufferSize());
		this.outputBuffer.flip();
	}
	
	public void permitRead() {
		synchronized(this.operLock) {
			if((this.opers & SelectionKey.OP_READ) == 0) {
				this.opers |= SelectionKey.OP_READ;
				register(this.opers);
				log.debug("[session-{}] permitRead", this.session.getId());
			}
		}
	}
	
	public void forbidRead() {
		synchronized(this.operLock) {
			if((this.opers & SelectionKey.OP_READ) != 0) {
				this.opers &= ~SelectionKey.OP_READ;
				register(this.opers);
				log.debug("[session-{}] forbidRead", this.session.getId());
			}
		}
	}
	
	public void permitWrite() {
		synchronized(this.operLock) {
			if((this.opers & SelectionKey.OP_WRITE) == 0) {
				this.opers |= SelectionKey.OP_WRITE;
				register(this.opers);
				log.debug("[session-{}] permitWrite", this.session.getId());
			}
		}
	}

	public void forbidWrite() {
		synchronized(this.operLock) {
			if((this.opers & SelectionKey.OP_WRITE) != 0) {
				this.opers &= ~SelectionKey.OP_WRITE;
				register(this.opers);
				log.debug("[session-{}] forbidWrite", this.session.getId());
			}
		}
	}
	
	/*
	 * mina 里为了优化read&write性能,只要缓冲区没满或空,会多次尝试read 与 write
	 * 我就暂时不优化了.感觉必要性不大.因为io性能已经足够用了.目前瓶颈都在逻辑处理上.
	 *	private final static int WRITE_SPIN_COUNT = 3;
	 *	private final static int READ_SPIN_COUNT = 3;
	*/
	@Override
	protected void onHandle(SelectionKey key) {
		SocketChannel channel = (SocketChannel)getChannel();
		try {
			if(key.isReadable()) {		
				int readByte = channel.read(this.inputBuffer);
				if(readByte < 0) {
					close();
					return;
				}
				log.debug("[session-{}] read data byte:{}", this.session.getId(), readByte);
				this.session.onRead(this.inputBuffer);
			}
			if(key.isWritable()) {
				this.session.onWrite(this.outputBuffer);
				int writeByte = channel.write(this.outputBuffer);
				if(writeByte < 0) {
					close();
					return;
				}	
				log.debug("[session-{}] write data byte:{}", this.session.getId(), writeByte);
				if(this.outputBuffer.remaining() == 0) {
					this.session.checkForbidWrite();
				}
			}
			this.session.setLastActiveTime(Timer.currentTime());
		} catch(IOException e) {
			close();
		}
	}

	protected void onOpen() {
		this.session = new IOSession(this);
		this.getManager().onAddSessionIntern(this.session);
		this.permitRead();
	}
	
	@Override
	protected void onClose() {
		log.debug("[IOTransportor-{}] onclose", this.session.getId());
		this.getManager().onDelSessionIntern(this.session);
		this.session.onClose();
	}

}
