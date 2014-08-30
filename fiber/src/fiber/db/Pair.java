package fiber.db;

import fiber.io.Octets;

public final class Pair {
	private final Octets key, value;
	public Pair(Octets key, Octets value) {
		this.key = key;
		this.value = value;
	}
	public final Octets getKey() {
		return key;
	}
	public final Octets getValue() {
		return value;
	}
}
