package fiber.common;

import org.luaj.vm2.Globals;

public class ScriptEngine {
	private static volatile int version = 0;

	public static interface Factory {
		Globals create();
	}
	
	private static Factory factory = new Factory() {
		@Override
		public Globals create() {
			return LuaState.create();
		}
		
	};
	
	public static void setEngineFactory(Factory fac) {
		factory = fac;
	}
	
	public static void refresh() {
		version++;
	}
	
	static class VersionEngine {
		private int version;
		private Globals g;
		public VersionEngine() {
			this.version = ScriptEngine.version;
			this.g = factory.create();
		}
		
		public int getVersion() {
			return this.version;
		}
		
		public void setVersion(int version) {
			this.version = version;
		}
		
		public Globals checkAndGet(int newVersion) {
			if(this.version < newVersion) {
				this.version = newVersion;
				this.g = factory.create();
			}
			return this.g;
		}
	}
	
	private static ThreadLocal<VersionEngine> engines = new ThreadLocal<VersionEngine>() {
		@Override
		public VersionEngine initialValue() {
			return new VersionEngine();
		}
	};
	
	public static Globals get() {
		return engines.get().checkAndGet(version);
	}
	
	public static void main(String[] args) {
		setEngineFactory(new Factory() {
			@Override
			public Globals create() {
				return LuaState.create("e:/t.lua", "e:/");
			}
			
		});
		
		get();
	}

}
