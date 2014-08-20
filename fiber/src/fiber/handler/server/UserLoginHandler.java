package fiber.handler.server;

import fiber.app.server.Procedure;
import fiber.bean.UserLogin;
import fiber.common.TaskPool;
import fiber.io.*;
import static fiber.app.server.Modules.*;
public class UserLoginHandler extends BeanHandler<UserLogin> {
	@Override
	public void onProcess(final IOSession session, final UserLogin arg) {
		TaskPool.execute(new Procedure() {
			@Override
			protected void execute() throws Exception {
				login.UserLogin(session, arg);
			}

		});
	}
}
