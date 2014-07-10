package fiber.io;

import java.net.InetSocketAddress;


public final class IOConfig {

	public IOConfig() {	}
	public InetSocketAddress setAddr(String ip, short port) {
		this.addr = new  InetSocketAddress(ip, port);
		return this.addr;
	}
	
	public InetSocketAddress getAddr() {
		return this.addr;
	}
	
	public int getInBufferSize() {
		return this.inBufferSize;
	}
	
	public void setInBufferSize(int size) {
		this.inBufferSize = size;
	}
	
	public int getOutBufferSize() {
		return this.outBufferSize;
	}
	
	public void setOutBufferSize(int size) {
		this.outBufferSize = size;
	}
	
	public final int getSendBufferSize() {
		return sendBufferSize;
	}
	
	public final void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}
	
	public final int getRecvBufferSize() {
		return recvBufferSize;
	}
	
	public final void setRecvBufferSize(int recvBufferSize) {
		this.recvBufferSize = recvBufferSize;
	}
	
	public final boolean isTpcNoDelay() {
		return tpcNoDelay;
	}
	
	public final void setTpcNoDelay(boolean tpcNoDelay) {
		this.tpcNoDelay = tpcNoDelay;
	}
	
	public final boolean isKeepAlive() {
		return keepAlive;
	}
	
	public final void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}
	
	public final boolean isReuseAddress() {
		return reuseAddress;
	}
	
	public final void setReuseAddress(boolean reuseAddress) {
		this.reuseAddress = reuseAddress;
	}
	
	public final int getBacklog() {
		return backlog;
	}
	
	public final void setBacklog(int backlog) {
		this.backlog = backlog;
	}
	
	public int getMaxInBufferSize() {
		return maxInBufferSize;
	}

	public void setMaxInBufferSize(int maxInBufferSize) {
		this.maxInBufferSize = maxInBufferSize;
	}

	public int getMaxOutBufferSize() {
		return maxOutBufferSize;
	}

	public void setMaxOutBufferSize(int maxOutBufferSize) {
		this.maxOutBufferSize = maxOutBufferSize;
	}


	// socket options
	
	// for all
	private InetSocketAddress addr;
	private int sendBufferSize = 4 * 1024; 
	private int recvBufferSize = 4 * 1024;
	private boolean tpcNoDelay = false;
	private boolean keepAlive = false;

	
	// for serversocket
	private boolean reuseAddress = true;
	private int backlog = 16;
	
	
	// for transportor. 
	private int inBufferSize = 4 * 1024;
	private int outBufferSize = 4 * 1024;

	
	private int maxInBufferSize = 1024 * 1024;
	private int maxOutBufferSize = 1024 * 1024;
}
