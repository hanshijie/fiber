package fiber.test;

import fiber.io.Log;
import fiber.prop.AllDatas;

public final class TestProp {

	public static void main(String[] args) {
		AllDatas data = new AllDatas();
		data.setBasePath("e:/");
		try {
			data.unmarshal();
			Log.trace("%s", data);
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
