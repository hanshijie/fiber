package fiber.handler.server;


import fiber.bean.UserLogin;
import fiber.common.TaskPool;
import fiber.db.Procedure;
import fiber.io.*;
import static fiber.app.server.G.*;
public class UserLoginHandler extends BeanHandler<UserLogin> {
	@Override
	public void onProcess(final IOSession session, final UserLogin arg) {
		TaskPool.execute(new Procedure() {
			@Override
			protected void execute() throws Exception {
				login.UserLogin(session, arg);
			}

			@Override
			protected void onRetError(int retcode, Object content) {
				
			}

		});
	}
}
