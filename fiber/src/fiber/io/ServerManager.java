package fiber.io;

import java.io.IOException;
import java.util.Map;

public class ServerManager extends NetManager {

	public ServerManager(IOPoller e, Map<Short, BeanHandler<?>> handlerStub) {
		super(e, handlerStub);
	}
	
	Acceptor acceptor;
	@Override
	public Acceptor startServer() throws IOException {
		acceptor = super.startServer();
		return acceptor;
	}
	
	public final void enableAccept() {
		if(this.acceptor != null) {
			this.acceptor.permitAccept();
		}
	}
	
	public final void forbidAccept() {
		if(this.acceptor != null) {
			this.acceptor.forbidAccept();
		}
	}

}
