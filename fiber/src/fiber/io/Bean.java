package fiber.io;

import fiber.io.OctetsStream;

public interface Bean<B extends Bean<B>> extends Cloneable, Comparable<B> {
	
	public B stub();
	public B create();
	public int type();
	public int maxsize();
	
	// 普通无scheme的序列化
	public OctetsStream marshal(OctetsStream os);
	public OctetsStream unmarshal(OctetsStream os) throws MarshalException;
	
	// 支持scheme的序列化, 比如新版本增加字段，能正常unmarshal老数据
	public OctetsStream marshalScheme(OctetsStream os);
	public OctetsStream unmarshalScheme(OctetsStream os) throws MarshalException;
}