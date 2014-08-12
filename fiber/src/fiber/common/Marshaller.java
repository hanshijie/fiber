package fiber.common;

import fiber.io.MarshalException;
import fiber.io.OctetsStream;

public interface Marshaller {
	void marshal(OctetsStream os, Object key);
	Object unmarshal(OctetsStream os) throws MarshalException;	
}
