package fiber.app.server;

import fiber.bean.SessionInfo;
import fiber.bean.UserLogin;
import fiber.bean.UserLoginRe;
import fiber.bean._.WrapperSessionInfo;
import fiber.common.RetException;
import fiber.io.IOSession;
import fiber.io.Timer;
import fiber.mapdb.Transaction.Dispatcher;
import fiber.mapdb.Transaction.Logger;

public class Login {

	public void UserLogin(IOSession session, UserLogin arg) throws Exception {
		Transaction txn = Transaction.get();
		Dispatcher net = txn.getDispatcher();
		Logger log = txn.getLogger();
		
		int sid = session.getId();
		log.trace("UserLogin uid:%d sid:%d", arg.getuid(), sid);
		WrapperSessionInfo w = Database.getSession(sid);
		UserLoginRe re = new UserLoginRe();
		if(!w.isNULL() || sid % 2 == 0) RetException.trigger(sid);
		w.assign(new SessionInfo());
		w.setuid(arg.getuid());
		int logintime = Timer.currentTime();
		w.setlogintime(logintime);
		re.settime(logintime);
		net.send(session, re);
		log.trace("UserLogin. succ");
	}

}
