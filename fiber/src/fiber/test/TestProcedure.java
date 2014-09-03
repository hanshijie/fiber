package fiber.test;

import fiber.db.Procedure;
import fiber.io.Log;

public class TestProcedure {

	public static void main(String[] args) throws InterruptedException {
		Procedure p = new Procedure() {

			@Override
			protected void execute() throws Exception {
				log.debug("1:%s", "hello");
				log.info("2:%d", 5);
				log.trace("3:%s", "hello");
				log.trace("4:%s", this);
				log.notice("5:%s", this);
				log.warn("6:%s", this);
				log.err("7:%s", this);
				log.alert("8:%s", this);
				log.fatal("9:%s", this);
			}

			@Override
			protected void onRetError(int retcode, Object content) {
				Log.info("onRetError. retcode:%d content:%s", retcode, content);
			}
			
		};
		p.run();
		Thread.sleep(1000);
		
		p = new Procedure() {

			@Override
			protected void execute() throws Exception {
				log.debug("1:%s", "hello");
				log.info("2:%d", 5);
				log.trace("3:%s", "hello");
				log.trace("4:%s", this);
				log.notice("5:%s", this);
				log.warn("6:%s", this);
				log.err("7:%s", this);
				log.alert("8:%s", this);
				log.fatal("9:%s", Log.etos(new Exception("xxxx")));
				trigger(1218);
			}

			@Override
			protected void onRetError(int retcode, Object content) {
				Log.info("onRetError. retcode:%d content:%s", retcode, content);
			}
			
		};
		
		p.run();
		Thread.sleep(1000);
		p = new Procedure() {

			@Override
			protected void execute() throws Exception {
				log.debug("1:%s", "hello");
				log.info("2:%d", 5);
				log.trace("3:%s", "hello");
				log.trace("4:%s", this);
				log.notice("5:%s", this);
				log.warn("6:%s", this);
				log.err("7:%s", this);
				log.alert("8:%s", this);
				log.fatal("9:%s", Log.etos(new Exception("xxxx")));
				throw new Exception("xxxxxx");
			}

			@Override
			protected void onRetError(int retcode, Object content) {
				Log.info("onRetError. retcode:%d content:%s", retcode, content);
			}
			
		};
		p.run();

	}

}
