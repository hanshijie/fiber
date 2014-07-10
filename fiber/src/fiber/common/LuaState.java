package fiber.common;

import java.util.HashMap;

import org.luaj.vm2.Globals;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;

public class LuaState {
	public static Globals create() {
		Globals g = JsePlatform.standardGlobals();
		LuaJC.install(g);
		g.set("basedir", baseDir);
		return g;
	}
	
	public static Globals create(String luaFile) {
		Globals g = create();
		if(!luaFile.isEmpty())
			g.loadfile(luaFile).call();
		return g;
	}
	
	private static final HashMap<Long, Globals> Ls = new HashMap<Long, Globals>();
	private static final Object luaLock = new Object();
	public static Globals get() {
		synchronized(luaLock) {
			Long tid = Thread.currentThread().getId();
			Globals g = Ls.get(tid);
			if(g == null) {
				g = create(initLuaFile);
				Ls.put(tid, g);
			}
			return g;
		}
	} 
	
	private static String initLuaFile = "";
	public static void setInitLuaFile(String file) {
		initLuaFile = file;
		updateAll();
	}
	
	private static String baseDir = ".";
	public static void setBaseDir(String baseLuaDir) {
		baseDir = baseLuaDir;
	}
	
	public static void updateAll() {
		synchronized(luaLock) {
			for(Long tid : Ls.keySet()) {
				Ls.put(tid, create(initLuaFile));
			}
		}
	}
	
	public static void invoke(String method, Varargs varargs) {
		get().get(method).invoke(varargs);
	}

}
