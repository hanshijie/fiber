package fiber.test;

import static fiber.io.Log.log;

public class TestBean {

	public static void main(String[] args) throws Exception {
		log.info("{}", null instanceof Integer);
		long N = 10000000;
		long M = 10;
		for(int j = 0 ; j < M ; j++) {
			long t1 = System.currentTimeMillis();
			for(int i = 0 ; i < N ; i++)
				System.currentTimeMillis();
			long t2 = System.currentTimeMillis();
			log.info("average:{}", N * 1000 / (t2 - t1));
		}
	}

}
