package fiber.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Log {
	public static final String progName;
	public static final Logger log;
	static {
		String name = System.getProperty("program_name");
		progName = name != null ? name : "fiber";
		log = LoggerFactory.getLogger(progName);
	}
}
