package fiber.test;

import fiber.common.LuaState;
import fiber.io.Log;

public class TestMultiLua {

	public static void main(String[] args) {
		Log.notice("init...");

		final String file = "src/mirror/test/hello.lua";
		LuaState.setBaseDir("src/mirror/test");
		LuaState.setInitLuaFile(file);
		LuaState.get();
		Log.notice("exit..");
		/*
		final Globals g = JsePlatform.standardGlobals();
		LuaJC.install(g);
		int N = 1000;
		for(int j = 0 ; j < N ;j++) {
			new TaskQueue().add(new Task() {

				@Override
				public void process() throws Exception {
					Globals g = LuaState.create(file);
					g.get("count").invoke(LuaValue.valueOf(1218));
				}

				@Override
				public void onError(int retcode, Object content) {
					
				}
				
			});
		}
		*/
	}

}
