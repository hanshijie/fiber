package fiber.app.server;

public abstract class Procedure extends fiber.mapdb.Procedure {
	@Override
	protected void prepare() {
		this.txn = Transaction.get();
		this.txn.prepare();
	}

}
