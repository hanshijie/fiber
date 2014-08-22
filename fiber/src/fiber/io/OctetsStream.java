package fiber.io;

import java.nio.ByteBuffer;

public final class OctetsStream {
	private static final byte[] EMPTY = new byte[0];
	private int head;
	private int tail;
	private byte[] data;
	
	public static OctetsStream wrap(byte[] data) {
		return new OctetsStream(data, 0, data.length);
	}
	
	public static OctetsStream wrap(byte[] data, int size) {
		return new OctetsStream(data, 0, size);
	}
	
	public static OctetsStream wrap(byte[] data, int head, int size) {
		return new OctetsStream(data, head, size);
	}
	
	public static OctetsStream wrap(Octets o) {
		return new OctetsStream(o.array(), 0, o.size());
	}
	
	public static OctetsStream create(int initSize) {
		return new OctetsStream(new byte[initSize], 0, 0);
	}
	
	public static OctetsStream create(byte[] data) {
		byte[] newData = new byte[data.length];
		System.arraycopy(data, 0, newData, 0, data.length);
		return wrap(newData);
	}
	
	public static OctetsStream create(byte[] data, int size) {
		byte[] newData = new byte[size];
		System.arraycopy(data, 0, newData, 0, size);
		return wrap(newData);
	}
	
	public static OctetsStream create(byte[] data, int head, int size) {
		byte[] newData = new byte[size];
		System.arraycopy(data, head, newData, 0, size);
		return wrap(newData);
	}
	
	public static OctetsStream create(Octets o) {
		int size = o.size();
		byte[] newData = new byte[size];
		System.arraycopy(o.array(), 0, newData, 0, size);
		return wrap(newData);
	}
	
	private OctetsStream(byte[] data, int head, int size) {
		this.head = head;
		this.tail = head + size;
		this.data = data;
	}
	
	public Octets toOctets() {
		return Octets.create(this.data, this.head, this.tail - this.head);
	}
	
	public Octets toRefOctets() {
		if(this.head == 0) {
			return Octets.wrap(this.data, this.tail);
		} else {
			return Octets.create(this.data, this.head, this.tail - this.head);
		}
	}
	
	public boolean empty() {
		return this.head == this.tail;
	}
	
	public int size() {
		return this.tail - this.head;
	}
	
	public int getHead() {
		return this.head;
	}
	
	public byte[] getData() {
		return this.data;
	}
	
	public void setHead(int head) {
		this.head = head;
	}
	
	public int getTail() {
		return this.tail;
	}
	
	public void setTail(int tail) {
		this.tail = tail;
	}
	
	public OctetsStream getReadOnlySub(int start, int size) {
		return wrap(this.data, start, size);
	}
	
	public final void clear() {
		this.head = 0;
		this.tail = 0;
	}
	
	public void skip(int size) {
		reserve(size);
		this.head += size;
		assert(this.head <= this.tail);
	}
	
	public final void append(Octets o) {
		append(o.array(), o.size());
	}
	
	public final void append(OctetsStream o) {
		append(o.data, o.head, o.size());
	}
	
	public final void append(byte[] d, int size) {
		reserve(size);
		System.arraycopy(d, 0, this.data, this.tail, size);
		this.tail += size;
	}
	
	public final void append(byte[] d, int start, int len) {
		reserve(len);
		System.arraycopy(d, start, this.data, this.tail, len);
		this.tail += len;
	}
	
	public final void append(byte[] d) {
		append(d, d.length);
	}
	
	public final void putTo(ByteBuffer outBuffer) {
		int putSize = outBuffer.remaining();
		if(putSize > this.tail - this.head) {
			putSize = this.tail - this.head;
		}
		outBuffer.put(this.data, this.head, putSize);
		this.head += putSize;
	}
	
	public final void getFrom(ByteBuffer inBuffer) {
		int getSize = inBuffer.remaining();
		reserve(getSize);
		inBuffer.get(this.data, this.tail, getSize);
		this.tail += getSize;
	}
	
	public final void reserve(int size) {
		//Log.debug("reserve:%d head:%d tail:%d length:%d", size, head, tail, data.length);
		if(size + this.tail > this.data.length) {
			doReserve(size);
		}
	}
	
	@Override
	public String toString() {
		return "[OctetsStream. head:" + this.head + ", tail:" + this.tail + ", data.length:" + data.length + "]";
	}
	
	private final void doReserve(int size) {
		int newSize = size + this.tail - this.head;
		if(newSize < this.data.length) {
			System.arraycopy(this.data, this.head, this.data, 0, this.tail -= this.head);
			this.head = 0;
			Log.debug("after shift. head:%d tail:%d length:%d", head, tail, data.length);
		} else {
			int probSize = 16;
			while(probSize < newSize)
				probSize <<= 1;
			byte[] newData = new byte[probSize];
			System.arraycopy(this.data, this.head, newData, 0, this.tail -= this.head);
			this.data = newData;
			this.head = 0;
			Log.debug("after resize. head:%d tail:%d length:%d", head, tail, data.length);
		}
	}
	
	
	public OctetsStream marshal1(byte x)
	{
		reserve(1);
		this.data[this.tail] = x;
		++this.tail;
		return this;
	}

	public OctetsStream marshal2(int x)
	{
		reserve(2);
		this.data[this.tail    ] = (byte)(x >> 8);
		this.data[this.tail + 1] = (byte)x;
		this.tail += 2;
		return this;
	}

	public OctetsStream marshal3(int x)
	{
		reserve(3);
		this.data[this.tail    ] = (byte)(x >> 16);
		this.data[this.tail + 1] = (byte)(x >> 8);
		this.data[this.tail + 2] = (byte)x;
		this.tail += 3;
		return this;
	}

	public OctetsStream marshal4(int x)
	{
		reserve(4);
		this.data[this.tail    ] = (byte)(x >> 24);
		this.data[this.tail + 1] = (byte)(x >> 16);
		this.data[this.tail + 2] = (byte)(x >> 8);
		this.data[this.tail + 3] = (byte)x;
		this.tail += 4;
		return this;
	}

	public OctetsStream marshal5(byte b, int x)
	{
		reserve(5);
		this.data[this.tail    ] = b;
		this.data[this.tail + 1] = (byte)(x >> 24);
		this.data[this.tail + 2] = (byte)(x >> 16);
		this.data[this.tail + 3] = (byte)(x >> 8);
		this.data[this.tail + 4] = (byte)x;
		this.tail += 5;
		return this;
	}

	public OctetsStream marshal5(long x)
	{
		reserve(5);
		this.data[this.tail    ] = (byte)(x >> 32);
		this.data[this.tail + 1] = (byte)(x >> 24);
		this.data[this.tail + 2] = (byte)(x >> 16);
		this.data[this.tail + 3] = (byte)(x >> 8);
		this.data[this.tail + 4] = (byte)x;
		this.tail += 5;
		return this;
	}

	public OctetsStream marshal6(long x)
	{
		reserve(6);
		this.data[this.tail    ] = (byte)(x >> 40);
		this.data[this.tail + 1] = (byte)(x >> 32);
		this.data[this.tail + 2] = (byte)(x >> 24);
		this.data[this.tail + 3] = (byte)(x >> 16);
		this.data[this.tail + 4] = (byte)(x >> 8);
		this.data[this.tail + 5] = (byte)x;
		this.tail += 6;
		return this;
	}

	public OctetsStream marshal7(long x)
	{
		reserve(7);
		this.data[this.tail    ] = (byte)(x >> 48);
		this.data[this.tail + 1] = (byte)(x >> 40);
		this.data[this.tail + 2] = (byte)(x >> 32);
		this.data[this.tail + 3] = (byte)(x >> 24);
		this.data[this.tail + 4] = (byte)(x >> 16);
		this.data[this.tail + 5] = (byte)(x >> 8);
		this.data[this.tail + 6] = (byte)x;
		this.tail += 7;
		return this;
	}

	public OctetsStream marshal8(long x)
	{
		reserve(8);
		this.data[this.tail    ] = (byte)(x >> 56);
		this.data[this.tail + 1] = (byte)(x >> 48);
		this.data[this.tail + 2] = (byte)(x >> 40);
		this.data[this.tail + 3] = (byte)(x >> 32);
		this.data[this.tail + 4] = (byte)(x >> 24);
		this.data[this.tail + 5] = (byte)(x >> 16);
		this.data[this.tail + 6] = (byte)(x >> 8);
		this.data[this.tail + 7] = (byte)x;
		this.tail += 8;
		return this;
	}

	public OctetsStream marshal9(byte b, long x)
	{
		reserve(9);
		this.data[this.tail    ] = b;
		this.data[this.tail + 1] = (byte)(x >> 56);
		this.data[this.tail + 2] = (byte)(x >> 48);
		this.data[this.tail + 3] = (byte)(x >> 40);
		this.data[this.tail + 4] = (byte)(x >> 32);
		this.data[this.tail + 5] = (byte)(x >> 24);
		this.data[this.tail + 6] = (byte)(x >> 16);
		this.data[this.tail + 7] = (byte)(x >> 8);
		this.data[this.tail + 8] = (byte)x;
		this.tail += 9;
		return this;
	}

	public OctetsStream marshal(boolean b)
	{
		reserve(1);
		this.data[this.tail] = (byte)(b ? 1 : 0);
		++this.tail;
		return this;
	}

	public OctetsStream marshal(byte x)
	{
		return marshal((int)x);
	}

	public OctetsStream marshal(short x)
	{
		return marshal((int)x);
	}

	public OctetsStream marshal(char x)
	{
		return marshal((int)x);
	}

	public OctetsStream marshal(int x)
	{
		if(x >= 0)
		{
		    if(x < 0x40)      return marshal1((byte)x);        // 00xx xxxx
		    if(x < 0x2000)    return marshal2(x + 0x4000);     // 010x xxxx +1B
		    if(x < 0x100000)  return marshal3(x + 0x600000);   // 0110 xxxx +2B
		    if(x < 0x8000000) return marshal4(x + 0x70000000); // 0111 0xxx +3B
		                      return marshal5((byte)0x78, x);  // 0111 1000 +4B
		}
		if(x >= -0x40)        return marshal1((byte)x);        // 11xx xxxx
		if(x >= -0x2000)      return marshal2(x - 0x4000);     // 101x xxxx +1B
		if(x >= -0x100000)    return marshal3(x - 0x600000);   // 1001 xxxx +2B
		if(x >= -0x8000000)   return marshal4(x - 0x70000000); // 1000 1xxx +3B
		                      return marshal5((byte)0x87, x);  // 1000 0111 +4B
	}

	public static int marshalLen(int x)
	{
		if(x >= 0)
		{
		    if(x < 0x40)      return 1;
		    if(x < 0x2000)    return 2;
		    if(x < 0x100000)  return 3;
		    if(x < 0x8000000) return 4;
		                      return 5;
		}
		if(x >= -0x40)        return 1;
		if(x >= -0x2000)      return 2;
		if(x >= -0x100000)    return 3;
		if(x >= -0x8000000)   return 4;
		                      return 5;
	}

	public OctetsStream marshal(long x)
	{
		if(x >= 0)
		{
			if(x < 0x8000000)         return marshal((int)x);
		    if(x < 0x400000000L)      return marshal5(x + 0x7800000000L);       // 0111 10xx +4B
		    if(x < 0x20000000000L)    return marshal6(x + 0x7c0000000000L);     // 0111 110x +5B
		    if(x < 0x1000000000000L)  return marshal7(x + 0x7e000000000000L);   // 0111 1110 +6B
		    if(x < 0x80000000000000L) return marshal8(x + 0x7f00000000000000L); // 0111 1111 0+7B
		                  return marshal9((byte)0x7f, x + 0x8000000000000000L); // 0111 1111 1+8B
		}
		if(x >= -0x8000000)           return marshal((int)x);
		if(x >= -0x400000000L)        return marshal5(x - 0x7800000000L);       // 1000 01xx +4B
		if(x >= -0x20000000000L)      return marshal6(x - 0x7c0000000000L);     // 1000 001x +5B
		if(x >= -0x1000000000000L)    return marshal7(x - 0x7e000000000000L);   // 1000 0001 +6B
		if(x >= -0x80000000000000L)   return marshal8(x - 0x7f00000000000000L); // 1000 0000 1+7B
		                  return marshal9((byte)0x80, x - 0x8000000000000000L); // 1000 0000 0+8B
	}

	public static int marshalLen(long x)
	{
		if(x >= 0)
		{
			if(x < 0x8000000)         return marshalLen((int)x);
		    if(x < 0x400000000L)      return 5;
		    if(x < 0x20000000000L)    return 6;
		    if(x < 0x1000000000000L)  return 7;
		    if(x < 0x80000000000000L) return 8;
		                              return 9;
		}
		if(x >= -0x8000000)           return marshalLen((int)x);
		if(x >= -0x400000000L)        return 5;
		if(x >= -0x20000000000L)      return 6;
		if(x >= -0x1000000000000L)    return 7;
		if(x >= -0x80000000000000L)   return 8;
		                              return 9;
	}

	public OctetsStream marshalUInt(int x)
	{
		if(x < 0x80)      return marshal1((byte)(x > 0 ? x : 0)); // 0xxx xxxx
		if(x < 0x4000)    return marshal2(x + 0x8000);            // 10xx xxxx +1B
		if(x < 0x200000)  return marshal3(x + 0xc00000);          // 110x xxxx +2B
		if(x < 0x10000000) return marshal4(x + 0xe0000000);        // 1110 xxxx +3B
		                  return marshal5((byte)0xf0, x);         // 1111 0000 +4B
	}

	public int marshalUIntBack(int p, int x)
	{
		int t = this.tail;
		if(x < 0x80)      { this.tail = p - 1; marshal1((byte)(x > 0 ? x : 0)); this.tail = t; return 1; }
		if(x < 0x4000)    { this.tail = p - 2; marshal2(x + 0x8000);            this.tail = t; return 2; }
		if(x < 0x200000)  { this.tail = p - 3; marshal3(x + 0xc00000);          this.tail = t; return 3; }
		if(x < 0x10000000) { this.tail = p - 4; marshal4(x + 0xe0000000);        this.tail = t; return 4; }
		                  { this.tail = p - 5; marshal5((byte)0xf0, x);         this.tail = t; return 5; }
	}

	public static int marshalUIntLen(int x)
	{
		if(x < 0x80)      return 1;
		if(x < 0x4000)    return 2;
		if(x < 0x200000)  return 3;
		if(x < 0x10000000) return 4;
		                  return 5;
	}

	public OctetsStream marshalUTF8(char x)
	{
		if(x < 0x80)  return marshal1((byte)x);                                              // 0xxx xxxx
		if(x < 0x800) return marshal2(((x << 2) & 0x1f00) + (x & 0x3f) + 0xc8);              // 110x xxxx  10xx xxxx
		return marshal3(((x << 4) & 0xf0000) + ((x << 2) & 0x3f00) + (x & 0x3f) + 0xe08080); // 1110 xxxx  10xx xxxx  10xx xxxx
	}

	public OctetsStream marshal(float x)
	{
		return marshal4(Float.floatToRawIntBits(x));
	}

	public OctetsStream marshal(double x)
	{
		return marshal8(Double.doubleToRawLongBits(x));
	}

	public OctetsStream marshal(byte[] bytes)
	{
		marshalUInt(bytes.length);
		append(bytes, 0, bytes.length);
		return this;
	}

	public OctetsStream marshal(Octets o)
	{
		marshalUInt(o.size());
		append(o);
		return this;
	}

	public OctetsStream marshal(String str)
	{
		int cn = str.length();
		int bn = 0;
		for(int i = 0; i < cn; ++i)
		{
			int c = str.charAt(i);
			if(c < 0x80) ++bn;
			else bn += (c < 0x800 ? 2 : 3);
		}
		marshalUInt(bn);
		reserve(this.tail + bn);
		if(bn == cn)
		{
			for(int i = 0; i < cn; ++i)
				marshal1((byte)str.charAt(i));
		}
		else
		{
			for(int i = 0; i < cn; ++i)
				marshalUTF8(str.charAt(i));
		}
		return this;
	}


	public OctetsStream marshal(Bean<?> b)
	{
		return b.marshal(this);
	}
	
	public boolean unmarshalBoolean() throws MarshalException
	{
		if(this.head >= this.tail) throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
		return this.data[this.head++] != 0;
	}

	public byte unmarshalByte() throws MarshalException {
		return (byte)unmarshalInt();
	}
	
	private byte _unmarshalByte() throws MarshalException {
		if(this.head >= this.tail) throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
		return this.data[this.head++];
	}

	public short unmarshalShort() throws MarshalException {
		return (short)unmarshalInt();
	}
	
	private short _unmarshalShort() throws MarshalException
	{
		int pos_new = this.head + 2;
		if(pos_new > this.tail) throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
		byte b0 = this.data[this.head    ];
		byte b1 = this.data[this.head + 1];
		this.head = pos_new;
		return (short)((b0 << 8) + (b1 & 0xff));
	}

	public char _unmarshalChar() throws MarshalException
	{
		int pos_new = this.head + 2;
		if(pos_new > this.tail) throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
		byte b0 = this.data[this.head    ];
		byte b1 = this.data[this.head + 1];
		this.head = pos_new;
		return (char)((b0 << 8) + (b1 & 0xff));
	}

	public int _unmarshalInt3() throws MarshalException
	{
		int pos_new = this.head + 3;
		if(pos_new > this.tail) throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
		byte b0 = this.data[this.head    ];
		byte b1 = this.data[this.head + 1];
		byte b2 = this.data[this.head + 2];
		this.head = pos_new;
		return ((b0 & 0xff) << 16) +
		       ((b1 & 0xff) <<  8) +
		        (b2 & 0xff);
	}

	public int _unmarshalInt4() throws MarshalException
	{
		int pos_new = this.head + 4;
		if(pos_new > this.tail) throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
		byte b0 = this.data[this.head    ];
		byte b1 = this.data[this.head + 1];
		byte b2 = this.data[this.head + 2];
		byte b3 = this.data[this.head + 3];
		this.head = pos_new;
		return ( b0         << 24) +
		       ((b1 & 0xff) << 16) +
		       ((b2 & 0xff) <<  8) +
		        (b3 & 0xff);
	}

	public long _unmarshalLong5() throws MarshalException
	{
		int pos_new = this.head + 5;
		if(pos_new > this.tail) throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
		byte b0 = this.data[this.head    ];
		byte b1 = this.data[this.head + 1];
		byte b2 = this.data[this.head + 2];
		byte b3 = this.data[this.head + 3];
		byte b4 = this.data[this.head + 4];
		this.head = pos_new;
		return ((b0 & 0xffL) << 32) +
		       ((b1 & 0xffL) << 24) +
		       ((b2 & 0xff ) << 16) +
		       ((b3 & 0xff ) <<  8) +
		        (b4 & 0xff );
	}

	public long _unmarshalLong6() throws MarshalException
	{
		int pos_new = this.head + 6;
		if(pos_new > this.tail) throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
		byte b0 = this.data[this.head    ];
		byte b1 = this.data[this.head + 1];
		byte b2 = this.data[this.head + 2];
		byte b3 = this.data[this.head + 3];
		byte b4 = this.data[this.head + 4];
		byte b5 = this.data[this.head + 5];
		this.head = pos_new;
		return ((b0 & 0xffL) << 40) +
		       ((b1 & 0xffL) << 32) +
		       ((b2 & 0xffL) << 24) +
		       ((b3 & 0xff ) << 16) +
		       ((b4 & 0xff ) <<  8) +
		        (b5 & 0xff );
	}

	public long _unmarshalLong7() throws MarshalException
	{
		int pos_new = this.head + 7;
		if(pos_new > this.tail) throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
		byte b0 = this.data[this.head    ];
		byte b1 = this.data[this.head + 1];
		byte b2 = this.data[this.head + 2];
		byte b3 = this.data[this.head + 3];
		byte b4 = this.data[this.head + 4];
		byte b5 = this.data[this.head + 5];
		byte b6 = this.data[this.head + 6];
		this.head = pos_new;
		return ((b0 & 0xffL) << 48) +
		       ((b1 & 0xffL) << 40) +
		       ((b2 & 0xffL) << 32) +
		       ((b3 & 0xffL) << 24) +
		       ((b4 & 0xff ) << 16) +
		       ((b5 & 0xff ) <<  8) +
		        (b6 & 0xff );
	}

	public long _unmarshalLong8() throws MarshalException
	{
		int pos_new = this.head + 8;
		if(pos_new > this.tail) throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
		byte b0 = this.data[this.head    ];
		byte b1 = this.data[this.head + 1];
		byte b2 = this.data[this.head + 2];
		byte b3 = this.data[this.head + 3];
		byte b4 = this.data[this.head + 4];
		byte b5 = this.data[this.head + 5];
		byte b6 = this.data[this.head + 6];
		byte b7 = this.data[this.head + 7];
		this.head = pos_new;
		return ((long)b0     << 56) +
		       ((b1 & 0xffL) << 48) +
		       ((b2 & 0xffL) << 40) +
		       ((b3 & 0xffL) << 32) +
		       ((b4 & 0xffL) << 24) +
		       ((b5 & 0xff ) << 16) +
		       ((b6 & 0xff ) <<  8) +
		        (b7 & 0xff );
	}

	public float unmarshalFloat() throws MarshalException
	{
		return Float.intBitsToFloat(_unmarshalInt4());
	}

	public double unmarshalDouble() throws MarshalException
	{
		return Double.longBitsToDouble(_unmarshalLong8());
	}

	public OctetsStream unmarshalSkip(int n) throws MarshalException
	{
		if(n < 0) throw MarshalException.create(Const.unmarshal_exception_verbose);
		int pos_new = this.head + n;
		if(pos_new > this.tail) throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
		this.head = pos_new;
		return this;
	}

	public int unmarshalInt() throws MarshalException
	{
		int b = _unmarshalByte();
		switch((b >> 3) & 0x1f)
		{
		case 0x00: case 0x01: case 0x02: case 0x03: case 0x04: case 0x05: case 0x06: case 0x07:
		case 0x18: case 0x19: case 0x1a: case 0x1b: case 0x1c: case 0x1d: case 0x1e: case 0x1f: return b;
		case 0x08: case 0x09: case 0x0a: case 0x0b: return ((b - 0x40) <<  8) + (_unmarshalByte()  & 0xff);
		case 0x14: case 0x15: case 0x16: case 0x17: return ((b + 0x40) <<  8) + (_unmarshalByte()  & 0xff);
		case 0x0c: case 0x0d:                       return ((b - 0x60) << 16) + (_unmarshalShort() & 0xffff);
		case 0x12: case 0x13:                       return ((b + 0x60) << 16) + (_unmarshalShort() & 0xffff);
		case 0x0e:                                  return ((b - 0x70) << 24) +  _unmarshalInt3();
		case 0x11:                                  return ((b + 0x70) << 24) +  _unmarshalInt3();
		case 0x0f:
			switch(b & 7)
			{
			case 0: case 1: case 2: case 3: return _unmarshalInt4();
			case 4: case 5:                 return unmarshalSkip(1)._unmarshalInt4();
			case 6:                         return unmarshalSkip(2)._unmarshalInt4();
			default: return unmarshalSkip(2 - (_unmarshalByte() >> 7))._unmarshalInt4();
			}
		default: // 0x10
			switch(b & 7)
			{
			case 4: case 5: case 6: case 7: return _unmarshalInt4();
			case 2: case 3:                 return unmarshalSkip(1)._unmarshalInt4();
			case 1:                         return unmarshalSkip(2)._unmarshalInt4();
			default: return unmarshalSkip(3 + (_unmarshalByte() >> 7))._unmarshalInt4();
			}
		}
	}
	
	public int unmarshalInteger() throws MarshalException
	{
		return unmarshalInt();
	}

	public long unmarshalLong() throws MarshalException
	{
		int b = _unmarshalByte();
		switch((b >> 3) & 0x1f)
		{
		case 0x00: case 0x01: case 0x02: case 0x03: case 0x04: case 0x05: case 0x06: case 0x07:
		case 0x18: case 0x19: case 0x1a: case 0x1b: case 0x1c: case 0x1d: case 0x1e: case 0x1f: return b;
		case 0x08: case 0x09: case 0x0a: case 0x0b: return ((b - 0x40) <<  8) + (_unmarshalByte()  & 0xff);
		case 0x14: case 0x15: case 0x16: case 0x17: return ((b + 0x40) <<  8) + (_unmarshalByte()  & 0xff);
		case 0x0c: case 0x0d:                       return ((b - 0x60) << 16) + (_unmarshalShort() & 0xffff);
		case 0x12: case 0x13:                       return ((b + 0x60) << 16) + (_unmarshalShort() & 0xffff);
		case 0x0e:                                  return ((b - 0x70) << 24) +  _unmarshalInt3();
		case 0x11:                                  return ((b + 0x70) << 24) +  _unmarshalInt3();
		case 0x0f:
			switch(b & 7)
			{
			case 0: case 1: case 2: case 3: return ((long)(b - 0x78) << 32) + (_unmarshalInt4() & 0xffffffffL);
			case 4: case 5:                 return ((long)(b - 0x7c) << 40) + _unmarshalLong5();
			case 6:                         return _unmarshalLong6();
			default: long r = _unmarshalLong7(); return r < 0x80000000000000L ?
					r : ((r - 0x80000000000000L) << 8) + (_unmarshalByte() & 0xff);
			}
		default: // 0x10
			switch(b & 7)
			{
			case 4: case 5: case 6: case 7: return ((long)(b + 0x78) << 32) + (_unmarshalInt4() & 0xffffffffL);
			case 2: case 3:                 return ((long)(b + 0x7c) << 40) + _unmarshalLong5();
			case 1:                         return 0xffff000000000000L + _unmarshalLong6();
			default: long r = _unmarshalLong7(); return r >= 0x80000000000000L ?
					0xff00000000000000L + r : ((r + 0x80000000000000L) << 8) + (_unmarshalByte() & 0xff);
			}
		}
	}

	public int unmarshalUInt() throws MarshalException
	{
		int b = _unmarshalByte() & 0xff;
		switch(b >> 4)
		{
		case  0: case  1: case  2: case  3: case 4: case 5: case 6: case 7: return b;
		case  8: case  9: case 10: case 11: return ((b & 0x3f) <<  8) + (_unmarshalByte() & 0xff);
		case 12: case 13:                   return ((b & 0x1f) << 16) + (_unmarshalShort() & 0xffff);
		case 14:                            return ((b & 0x0f) << 24) +  _unmarshalInt3();
		default: int r = _unmarshalInt4(); if(r < 0) throw MarshalException.create(Const.unmarshal_exception_verbose); return r;
		}
	}

	public char unmarshalUTF8() throws MarshalException
	{
		int b = _unmarshalByte();
		if(b >= 0) return (char)b;
		if(b < -0x20) return (char)(((b & 0x1f) << 6) + (_unmarshalByte() & 0x3f));
		int c = _unmarshalByte();
		return (char)(((b & 0xf) << 12) + ((c & 0x3f) << 6) + (_unmarshalByte() & 0x3f));
	}

	public byte[] unmarshalBytes() throws MarshalException
	{
		int size = unmarshalUInt();
		if(size <= 0) return EMPTY;
		int pos_new = this.head + size;
		if(pos_new > this.tail) throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
		if(pos_new < this.head) throw MarshalException.create(Const.unmarshal_exception_verbose);
		byte[] r = new byte[size];
		System.arraycopy(this.data, this.head, r, 0, size);
		this.head = pos_new;
		return r;
	}

	public Octets unmarshalOctets() throws MarshalException
	{
		return Octets.wrap(unmarshalBytes());
	}

	public OctetsStream unmarshal(Bean<?> b) throws MarshalException
	{
		return b.unmarshal(this);
	}

	public String unmarshalString() throws MarshalException
	{
		int size = unmarshalUInt();
		if(size <= 0) return "";
		int pos_new = this.head + size;
		if(pos_new > this.tail) throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
		if(pos_new < this.head) throw MarshalException.create(Const.unmarshal_exception_verbose);
		char[] tmp = new char[size];
		int n = 0;
		while(this.head < pos_new)
			tmp[n++] = unmarshalUTF8();
		this.head = pos_new;
		return new String(tmp, 0, n);
	}

}
