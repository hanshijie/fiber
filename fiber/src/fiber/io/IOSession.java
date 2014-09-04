package fiber.io;

import java.nio.ByteBuffer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static fiber.io.Log.log;

public final class IOSession {
	public IOSession(IOTransportor handler) {
		this.handler = handler;
		this.id = idGen.incrementAndGet();
		IOConfig config = handler.getManager().getConfig();
		this.inputBuffer = OctetsStream.create(config.getInBufferSize());
		this.outputBuffer = OctetsStream.create(config.getOutBufferSize());
		this.datas = new LinkedList<Octets>();
		this.coder = handler.getManager().getFactory().createCodec();
		this.lastActiveTime = Timer.currentTime();
	}
	
	public int getId() {
		return this.id;
	}
	
	public void close() {
		this.handler.close();
		log.info("[session-{}] close", this.getId());
	}
	
	public void onClose() {
		
	}
	
	public IOTransportor getHandler() {
		return handler;
	}
	
	public void onRead(ByteBuffer inBuffer) {
		inBuffer.flip();

		OctetsStream is = OctetsStream.wrap(inBuffer.array(), inBuffer.arrayOffset() + inBuffer.position(), inBuffer.remaining());
		if(this.inFilter != null) {
			is = this.inFilter.update(is);
		}
		this.inputBuffer.append(is);
		inBuffer.clear();
		this.coder.decode(this, this.inputBuffer);
	}
	
	public void onWrite(ByteBuffer outBuffer) {
		outBuffer.compact();
		synchronized(this.datas) { 
			if(!this.datas.isEmpty()) {
				Octets data;
				if(this.outFilter != null) {
					while((data = this.datas.poll()) != null) {
						this.outputBuffer.append(this.outFilter.update(data));
					}
				} else {
					while((data = this.datas.poll()) != null) {
						this.outputBuffer.append(data);
					}				
				}
			}
			this.outputBuffer.putTo(outBuffer);
		}
		outBuffer.flip();
	}
	
	// 在IOTransportor.write完成后调用.
	protected void checkForbidWrite() {
		synchronized(this.datas) {
			if(!this.remainOutputData()) {
				this.handler.forbidWrite();
			}
		}
	}
	
	public void forbidRead() {
		this.handler.forbidRead();
	}
	
	public void permitRead() {
		this.handler.permitRead();
	}
	
	public void write(Octets o) {
		synchronized(this.datas) {
			if(!this.remainOutputData()) {
				this.handler.permitWrite();
			}
			this.datas.add(o);
		}
	}
	
	/**
	 * 注意!!!!
	 * 只发送普通的非rpc协议.
	 * 如果要发送rpc,使用manager的sendRpc接口
	 */
	public void send(Object msg) {
		write(this.coder.encode(this, msg));
	}
	
	protected boolean remainOutputData() {
		return !this.datas.isEmpty() || !this.outputBuffer.empty();
	}
	
	public final void setInFilter(IOFilter inFilter) {
		this.inFilter = inFilter;
	}

	public final void setOutFilter(IOFilter outFilter) {
		this.outFilter = outFilter;
	}

	public final int getLastActiveTime() {
		return lastActiveTime;
	}

	public final void setLastActiveTime(int lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	private final int id;
	private final IOTransportor handler;
	private final OctetsStream inputBuffer;
	private final OctetsStream outputBuffer;
	private IOFilter inFilter;
	private IOFilter outFilter;
	private final ProtocolCodec coder;
	
	private final Queue<Octets> datas;
	private int lastActiveTime;
	private static final AtomicInteger idGen = new AtomicInteger(0);
}
