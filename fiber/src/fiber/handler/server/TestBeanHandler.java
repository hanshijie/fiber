package fiber.handler.server;

import fiber.bean.TestBean;
import fiber.io.*;

public class TestBeanHandler extends BeanHandler<TestBean> {
	@Override
	public void onProcess(final IOSession session, final TestBean arg) {
	}
}
