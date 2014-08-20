package fiber.handler.client;

import fiber.bean.UserLoginRe;
import fiber.io.*;

public class UserLoginReHandler extends BeanHandler<UserLoginRe> {
	@Override
	public void onProcess(final IOSession session, final UserLoginRe arg) {
		Log.notice("UserLoginRe. retcode:%d logintime:%d", arg.getretcode(), arg.gettime());
	}
}
