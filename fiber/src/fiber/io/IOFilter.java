package fiber.io;

public abstract class IOFilter {
	public abstract OctetsStream update(OctetsStream in);
	public OctetsStream update(Octets in) {	return update(OctetsStream.wrap(in)); }
}
