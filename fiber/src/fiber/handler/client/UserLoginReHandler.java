package fiber.handler.client;

import fiber.bean.UserLoginRe;
import fiber.io.*;
import static fiber.io.Log.log;

public class UserLoginReHandler extends BeanHandler<UserLoginRe> {
	@Override
	public void onProcess(final IOSession session, final UserLoginRe arg) {
		log.info("UserLoginRe. retcode:{} logintime:{}", arg.getretcode(), arg.gettime());
	}
}
