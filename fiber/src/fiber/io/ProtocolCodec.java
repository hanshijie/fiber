package fiber.io;

public abstract class ProtocolCodec {
	public abstract void decode(IOSession session, OctetsStream is);
	public abstract Octets encode(IOSession session, Object obj);
	public static abstract class Factory {
		public abstract ProtocolCodec createCodec();
	}
}
