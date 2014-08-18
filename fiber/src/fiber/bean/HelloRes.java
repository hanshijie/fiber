package fiber.bean;

import fiber.io.*;


public final class HelloRes implements Bean<HelloRes>
{
	public static final int TYPE = 42;
	public static final HelloRes STUB = new HelloRes();

	boolean v1;
	short v3;


	public HelloRes()
	{
		this.v1 = false;
		this.v3 = 0;

	}

	public HelloRes (boolean v1, short v3)
	{
		this.v1 = v1;
		this.v3 = v3;

	}
	
	@Override public final HelloRes stub() { return STUB; }
	@Override public final int type() { return 42; }
	@Override public final int maxsize() { return 100; }
	@Override public final HelloRes create() { return new HelloRes(); }
	
 
	public final boolean getv1() { return this.v1; }
	public final void set(boolean v1) { this.v1 = v1; }
 
	public final short getv3() { return this.v3; }
	public final void set(short v3) { this.v3 = v3; }


	@Override
	public final OctetsStream marshal(OctetsStream os) {
		_.marshal_bool(os, this.v1);
		_.marshal_short(os, this.v3);

		return os;
	}
	
	@Override
	public final OctetsStream unmarshal(OctetsStream os) throws MarshalException {
		this.v1 = _.unmarshal_bool(os);
		this.v3 = _.unmarshal_short(os);

		return os;
	}
		
	@Override
	public final OctetsStream marshalScheme(OctetsStream os) {
		os.marshalUInt(2);
		_.marshalscheme_short(os, this.v3);
		_.marshalscheme_bool(os, this.v1);

		return os;
	}

	@Override
	public final OctetsStream unmarshalScheme(OctetsStream os) throws MarshalException {
		switch(os.unmarshalUInt()) {
			case 2 : _.marshalscheme_short(os, this.v3);
			case 1 : _.marshalscheme_bool(os, this.v1);
			case 0 : break;
			default: throw MarshalException.createEOF(false); 
		}

		return os;
	}

	@Override
	public HelloRes clone() {
		final HelloRes o = new HelloRes();
		o.v1 = _.clone_bool(this.v1);
		o.v3 = _.clone_short(this.v3);

		return o;
	}

	@Override
	public int hashCode() {
		int h = 42 * 0x9e3779b1;
		h = h * 31 + _.hashcode_bool(this.v1);
		h = h * 31 + _.hashcode_short(this.v3);

		return h;
	}

	@Override
	public boolean equals(Object _b)	{
		if(_b == this) return true;
		if(!(_b instanceof HelloRes)) return false;
		final HelloRes _o = (HelloRes)_b;
		if(!_.equals_bool(this.v1, _o.v1)) return false;
		if(!_.equals_short(this.v3, _o.v3)) return false;

		return _b == _o;
	}

	@Override
	public int compareTo(HelloRes _o) {
		if(_o == this) return 0;
		if(_o == null) return 1;
		int c;
		if((c = _.compareto_bool(this.v1, _o.v1)) != 0) return c;
		if((c = _.compareto_short(this.v3, _o.v3)) != 0) return c;

		return 0;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("HelloRes{");
		s.append("v1 = "); s.append(_.tostring_bool(this.v1)); s.append(", ");
		s.append("v3 = "); s.append(_.tostring_short(this.v3)); s.append(", ");

		return s.append('}').toString();
	}
}
