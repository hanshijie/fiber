package fiber.bean;

import java.util.Map;
import java.util.HashMap;
import fiber.io.Bean;

public final class AllBeans
{
	private final static Map<Integer, Bean<?>> allbeans = new HashMap<Integer, Bean<?>>();
	static {	
		allbeans.put(4, Hello.STUB);
		allbeans.put(42, HelloRes.STUB);
		allbeans.put(41, HelloArg.STUB);
		allbeans.put(2, TestBean.STUB);
		allbeans.put(3, TestType.STUB);

	}

	public static Map<Integer, Bean<?>> get()
	{
		return allbeans;
	}
}
