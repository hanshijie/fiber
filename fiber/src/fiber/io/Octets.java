package fiber.io;

import java.io.UnsupportedEncodingException;

/**
 * 
 * @author HuangQiang
 * @desription 不可变的Octet类. 管理多字节序列.线程安全.
 *
 */
public final class Octets implements Cloneable, Comparable<Octets> {
	public static final Octets EMPTY = new Octets();
	//protected static final byte[] ARRAY_EMPTY = new byte[0];
	protected static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	
	protected final byte[]  buffer;
	protected final int count;

	public static Octets create(byte[] data, int size) {
		if(size <= 0) return EMPTY;
		byte[] newData = new byte[size];
		System.arraycopy(data, 0, newData, 0, size);
		return wrap(newData);
	}
	
	public static Octets create(byte[] data, int head, int size) {
		if(size <= 0) return EMPTY;
		byte[] newData = new byte[size];
		System.arraycopy(data, head, newData, 0, size);
		return wrap(newData);
	}
	
	public static Octets wrap(byte[] data, int size) {
		return new Octets(data, size);
	}

	public static Octets wrap(byte[] data) {
		return new Octets(data);
	}

	public static Octets createSpace(int size)	{
		return size > 0 ? new Octets(new byte[size]) : EMPTY;
	}

	public Octets()	{
		this.buffer = new byte[0];
		this.count = 0;
	}
	
	public Octets(byte[] data, int size) {
		this.buffer = data;
		this.count = size;
	}

	public Octets(byte[] data)	{
		this.buffer = data;
		this.count = data.length;
	}

	public byte[] array()	{
		return buffer;
	}

	public boolean empty() {
		return count <= 0;
	}

	public int size() {
		return count;
	}

	public int capacity() {
		return buffer.length;
	}

	public byte getByte(int p)
	{
		return buffer[p];
	}

	public byte[] getBytes()
	{
		byte[] buf = new byte[count];
		System.arraycopy(buffer, 0, buf, 0, count);
		return buf;
	}

	public Octets append(byte[] data, int pos, int size) {
		if(size <= 0) return this;
		int len = data.length;
		if(pos < 0) pos = 0;
		if(pos >= len) return this;
		len -= pos;
		if(size > len) size = len;
		byte[] newData = new byte[this.count + size];
		System.arraycopy(this.buffer, 0, newData, 0, this.count);
		System.arraycopy(data, pos, newData, count, size);
		return wrap(newData);
	}

	public Octets append(byte[] data) {
		return append(data, 0, data.length);
	}

	public Octets append(Octets o) {
		return append(o.buffer, 0, o.count);
	}

	public int find(int pos, int end, byte b) {
		if(pos < 0) pos = 0;
		if(end > count) end = count;
		byte[] buf = buffer;
		for(; pos < end; ++pos)
			if(buf[pos] == b) return pos;
		return -1;
	}

	public int find(int pos, byte b) {
		return find(pos, count, b);
	}

	public int find(byte b)	{
		return find(0, count, b);
	}

	@Override
	public Octets clone() {
		return this;
	}

	@Override
	public int hashCode() {
		int result = count;
		if(count <= 32)	{
			for(int i = 0; i < count; ++i)
				result = 31 * result + buffer[i];
		} else {
			for(int i = 0; i < 16; ++i)
				result = 31 * result + buffer[i];
			for(int i = count - 16; i < count; ++i)
				result = 31 * result + buffer[i];
		}
		return result;
	}

	@Override
	public int compareTo(Octets o) {
		if(o == null) return 1;
		int n = (count <= o.count ? count : o.count);
		byte[] buf = buffer;
		byte[] data = o.buffer;
		for(int i = 0; i < n; ++i) {
			int v = buf[i] - data[i];
			if(v != 0) return v;
		}
		return count - o.count;
	}

	@Override
	public boolean equals(Object o)	{
		if(this == o) return true;
		if(!(o instanceof Octets)) return false;
		Octets oct = (Octets)o;
		if(count != oct.count) return false;
		byte[] buf = buffer;
		byte[] data = oct.buffer;
		for(int i = 0, n = count; i < n; ++i)
			if(buf[i] != data[i]) return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + count + '/' + buffer.length + ']';
	}
	
	public String toPrintString() throws UnsupportedEncodingException {
		return new String(array(), "UTF-8");
	}

	public StringBuilder dump(StringBuilder s) {
		if(s == null) s = new StringBuilder(count * 3 + 4);
		s.append('[');
		if(count <= 0) return s.append(']');
		for(int i = 0;;) {
			int b = buffer[i];
			s.append(HEX[(b >> 4) & 15]);
			s.append(HEX[b & 15]);
			if(++i >= count) return s.append(']');
			s.append(' ');
		}
	}

	public StringBuilder dump() {
		return dump(null);
	}

	public StringBuilder dumpJStr(StringBuilder s) {
		if(s == null) s = new StringBuilder(count * 4 + 4);
		s.append('"');
		if(count <= 0) return s.append('"');
		for(int i = 0;;) {
			int b = buffer[i];
			s.append('\\').append('x');
			s.append(HEX[(b >> 4) & 15]);
			s.append(HEX[b & 15]);
			if(++i >= count) return s.append('"');
		}
	}

	public StringBuilder dumpJStr()	{
		return dumpJStr(null);
	}
	
}

