package fiber.handler.client;
/*
import fiber.app.server.Procedure;
import fiber.bean.UserLogin;
import fiber.common.TaskPool;
*/
import fiber.bean.TestType;
import fiber.io.*;

public class TestTypeHandler extends BeanHandler<TestType> {
	@Override
	public void onProcess(final IOSession session, final TestType arg) {
		Log.trace("TestTypeHandler.onProcess. sessionid:%d, arg:%s", session.getId(), arg);
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
