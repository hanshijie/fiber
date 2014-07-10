package fiber.io;

public final class MTimer {
	public static int currentTime() { return (int)(System.currentTimeMillis() / 1000); }
	public static long currentTimeMillis() { return System.currentTimeMillis(); }
}
