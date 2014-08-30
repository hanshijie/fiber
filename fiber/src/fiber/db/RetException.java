package fiber.db;

public class RetException extends Exception {
	private static final long serialVersionUID = 5479983805739536726L;
	
	private final int retcode;
	private final Object content;
	public RetException(int retcode, Object content) {
		super("", null, false, false);
		this.retcode = retcode;
		this.content = content;
	}
	public int getRetcode() {
		return retcode;
	}
	
	public final Object getContent() {
		return content;
	}
	
	public static void trigger(int retcode) throws RetException {
		trigger(retcode, null);
	}
	
	public static void trigger(int retcode, Object content) throws RetException {
		throw new RetException(retcode, content);
	}
	
	@Override
	public final String getMessage() {
		return "error:" + this.retcode;
	}
}
