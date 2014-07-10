package fiber.test;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import fiber.io.Log;

public class JavascriptBenchmark {

	public static void main(String[] args) {
		ScriptEngineManager sem = new ScriptEngineManager();

		try {
			Log.notice("init...");

			int K = 1; // Integer.parseInt(args[0]);
			String cmd = "function count(sum) { for(var i = 0 ; i < 100000 ; i++) sum += i; return sum; }";
			for (int k = 0; k < K; k++) {
				ScriptEngine e = sem.getEngineByName("javascript");
				e.eval(cmd);
				final Invocable f = (Invocable) e;

				int N = 100;
				for (int j = 0; j < 100; j++) {
					long t1 = System.currentTimeMillis();
					for (int i = 0; i < N; i++) {
						Object re = f.invokeFunction("count", 0);
						Log.trace("[%d] %s", i, re);
					}
					long t2 = System.currentTimeMillis();
					Log.trace("average:%d", N * 1000 / (t2 - t1));
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
