package fiber.app.server;

public abstract class Procedure extends fiber.mapdb.AbstractProcedure {
	@Override
	protected void prepare() {
		this.txn = Transaction.get();
		this.txn.prepare();
	}

}
