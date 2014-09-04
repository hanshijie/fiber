package fiber.test;

import static fiber.io.Log.log;
import fiber.prop.AllDatas;

public final class TestProp {

	public static void main(String[] args) {
		AllDatas data = new AllDatas();
		data.setBasePath("e:/");
		try {
			data.unmarshal();
			log.trace("{}", data);
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
