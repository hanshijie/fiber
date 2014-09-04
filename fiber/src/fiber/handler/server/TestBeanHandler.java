package fiber.handler.server;
/*
import fiber.app.server.Procedure;
import fiber.bean.UserLogin;
import fiber.common.TaskPool;
*/
import fiber.bean.TestBean;
import fiber.io.*;
import static fiber.io.Log.log;

public class TestBeanHandler extends BeanHandler<TestBean> {
	@Override
	public void onProcess(final IOSession session, final TestBean arg) {
		log.trace("TestBeanHandler.onProcess. sessionid:{}, arg:{}", session.getId(), arg);
		/*
		TaskPool.execute(new Procedure() {
			@Override
			protected void execute() throws Exception {
				log.trace("{}.execute. sessionid:{}, arg:{}", this, session.getId(), arg);
			}
			
			@Override
			protected void onRetError(int retcode, Object content) {
				log.err("{}.onRetError. retcode:{} content:{}", this, retcode, content);
			}

		});
		*/
	}
}
