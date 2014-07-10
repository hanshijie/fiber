package fiber.common;
import java.util.HashMap;

import fiber.common.Procedure.RedoException;
import fiber.io.Bean;
import fiber.mapdb.TKey;

public class DB {
	public static DB instance = new DB();
	public void update(HashMap<TKey, Bean<?>> dataMap) throws RedoException {

	}
}
