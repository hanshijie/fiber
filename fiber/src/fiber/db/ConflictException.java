package fiber.db;

public class ConflictException extends Exception {
	private static final long serialVersionUID = -2035543463458170933L;
	public static ConflictException INSTANCE = new ConflictException();
	private ConflictException() {
		super("", null, false, false);
	}
}
