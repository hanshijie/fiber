package fiber.io;

public final class Timer {
	public static int currentTime() { return (int)(System.currentTimeMillis() / 1000); }
	public static long currentTimeMillis() { return System.currentTimeMillis(); }
}
