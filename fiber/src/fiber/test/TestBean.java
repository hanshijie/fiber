package fiber.test;

import fiber.io.Log;

public class TestBean {

	public static void main(String[] args) throws Exception {
		Log.trace("%s", null instanceof Integer);
		long N = 10000000;
		long M = 10;
		for(int j = 0 ; j < M ; j++) {
			long t1 = System.currentTimeMillis();
			for(int i = 0 ; i < N ; i++)
				System.currentTimeMillis();
			long t2 = System.currentTimeMillis();
			Log.trace("average:%d", N * 1000 / (t2 - t1));
		}
	}

}
