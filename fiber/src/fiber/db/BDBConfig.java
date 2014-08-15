package fiber.db;

import java.util.Map;
import java.util.TreeMap;

import com.sleepycat.je.Durability;

public class BDBConfig {
	private String envRoot;
	private Map<Integer, String> databases;
	private long cacheSize;
	private Durability durability;
	
	public BDBConfig() {
		this.envRoot = "";
		this.databases = new TreeMap<Integer, String>();
		this.cacheSize = 0;
		this.durability = Durability.COMMIT_WRITE_NO_SYNC;
	}

	public final String getEnvRoot() {
		return envRoot;
	}

	public final void setEnvRoot(String envRoot) {
		this.envRoot = envRoot;
	}
	
	public final Map<Integer, String> getDatabases() {
		return databases;
	}

	public final void setDatabases(Map<Integer, String> databases) {
		this.databases = databases;
	}
	
	public final void AddDatabse(int dbId, String dbName) {
		this.databases.put(dbId, dbName);
	}

	public final long getCacheSize() {
		return cacheSize;
	}

	public final void setCacheSize(long cacheSize) {
		this.cacheSize = cacheSize;
	}

	public final Durability getDurability() {
		return durability;
	}

	public final void setDurability(Durability durability) {
		this.durability = durability;
	}
	
	

	
}
