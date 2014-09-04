package fiber.test;

import fiber.db.Procedure;
import static fiber.io.Log.log;

public class TestProcedure {

	public static void main(String[] args) throws InterruptedException {
		//System.setProperty("log4j.configurationFile", "log4j.configuration");
		
		Procedure p = new Procedure() {

			@Override
			protected void execute() throws Exception {
				log.debug("1:{}", "hello");
				log.info("2:{}", 5);
				log.trace("3:{}", "hello");
				log.trace("4:{}", this);
				log.warn("6:{}", this);
				log.error("7:{}", this);
			}

			@Override
			protected void onRetError(int retcode, Object content) {
				log.info("onRetError. retcode:{} content:{}", retcode, content);
			}
			
		};
		p.run();
		Thread.sleep(1000);
		
		p = new Procedure() {

			@Override
			protected void execute() throws Exception {
				log.debug("1:{}", "hello");
				log.info("2:{}", 5);
				log.trace("3:{}", "hello");
				log.trace("4:{}", this);
				log.warn("6:{}", this);
				log.error("7:{}", this);
				log.error("9:", new Exception("xxxx"));
				trigger(1218);
			}

			@Override
			protected void onRetError(int retcode, Object content) {
				log.info("onRetError. retcode:{} content:{}", retcode, content);
			}
			
		};
		
		p.run();
		Thread.sleep(1000);
		p = new Procedure() {

			@Override
			protected void execute() throws Exception {
				log.debug("1:{}", "hello");
				log.info("2:{}", 5);
				log.trace("3:{}", "hello");
				log.trace("4:{}", this);
				log.warn("6:{}", this);
				log.error("7:{}", this);
				throw new Exception("xxxxxx");
			}

			@Override
			protected void onRetError(int retcode, Object content) {
				log.info("onRetError. retcode:{} content:{}", retcode, content);
			}
			
		};
		p.run();

	}

}
