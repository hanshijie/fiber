package fiber.db;

import fiber.io.Octets;

public interface Walker {
	boolean onProcess(Octets key, Octets value);
}
