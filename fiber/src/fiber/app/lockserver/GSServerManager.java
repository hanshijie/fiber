package fiber.app.lockserver;

import java.util.HashMap;
import java.util.Map;
import fiber.io.BeanHandler;
import fiber.io.IOPoller;
import fiber.io.IOSession;
import fiber.io.ServerManager;
import static fiber.io.Log.log;

public class GSServerManager extends ServerManager {

	public GSServerManager(IOPoller e, Map<Integer, BeanHandler<?>> handlerStub) {
		super(e, handlerStub);
	}
	
	private final HashMap<Integer, IOSession> gs2SessionMap = new HashMap<Integer, IOSession>();
	private final HashMap<IOSession, Integer> session2gsMap = new HashMap<IOSession, Integer>();
	@Override
	protected void onAddSession(IOSession session) {
		
	}
	
	public boolean bindGSSession(Integer gsid, IOSession session) {
		if(this.gs2SessionMap.containsKey(gsid)) {
			log.error("dumplicate gsid:{}", gsid);
			return false;
		}
		log.trace("bindGSSession. gsid:{} sessionid:{}", gsid, session.getId());
		this.gs2SessionMap.put(gsid, session);
		this.session2gsMap.put(session, gsid);
		return true;
	}
	
	@Override
	protected void onDelSession(IOSession session) {
		Integer gsid = this.session2gsMap.get(session);
		if(gsid != null) {
			log.trace("onDelSession. gsid:{}", gsid);
			this.session2gsMap.remove(session);
			this.gs2SessionMap.remove(gsid);
		}
	}

}
