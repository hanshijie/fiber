package fiber.io;

public final class Timer {
	/*
	private static volatile int now = (int)(System.currentTimeMillis() / 1000);
	static {
		NetManager.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				now = (int)System.currentTimeMillis() / 1000;
			}
			
		}, 1, 1, TimeUnit.SECONDS);
	}
	public static int currentTime() { return now; }
	*/
	public static int currentTime() { return (int)(System.currentTimeMillis() / 1000); }
	public static long currentTimeMillis() { return System.currentTimeMillis(); }
}
