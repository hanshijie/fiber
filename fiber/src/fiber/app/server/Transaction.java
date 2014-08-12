package fiber.app.server;

public class Transaction extends fiber.mapdb.Transaction {
	private final static ThreadLocal<Transaction> contexts = new ThreadLocal<Transaction>() {
		@Override
		public Transaction initialValue() {
			return new Transaction();
		}
	};
	
	public static Transaction get() {
		return contexts.get();
	}
	
	protected void commitModifyData() throws Exception {
		
	}
}
