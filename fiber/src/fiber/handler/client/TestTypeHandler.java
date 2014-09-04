package fiber.handler.client;
/*
import fiber.app.server.Procedure;
import fiber.bean.UserLogin;
import fiber.common.TaskPool;
*/
import fiber.bean.TestType;
import fiber.io.*;
import static fiber.io.Log.log;

public class TestTypeHandler extends BeanHandler<TestType> {
	@Override
	public void onProcess(final IOSession session, final TestType arg) {
		log.info("TestTypeHandler.onProcess. sessionid:{}, arg:{}", session.getId(), arg);
		/*
		TaskPool.execute(new Procedure() {
			@Override
			protected void execute() throws Exception {
				log.trace("%s.execute. sessionid:%d, arg:%s", this, session.getId(), arg);
			}
			
			@Override
			protected void onRetError(int retcode, Object content) {
				log.err("%s.onRetError. retcode:%d content:%s", this, retcode, content);
			}

		});
		*/
	}
}
