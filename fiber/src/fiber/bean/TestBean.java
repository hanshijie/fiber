package fiber.bean;

import fiber.io.*;


public final class TestBean implements Bean<TestBean>
{
	public static final int TYPE = 2;
	public static final TestBean STUB = new TestBean();

	boolean v1;


	public TestBean()
	{
		this.v1 = false;

	}

	public TestBean (boolean v1)
	{
		this.v1 = v1;

	}
	
	@Override public final TestBean stub() { return STUB; }
	@Override public final int type() { return 2; }
	@Override public final int maxsize() { return 100; }
	@Override public final TestBean create() { return new TestBean(); }
	
 
	public final boolean getv1() { return this.v1; }
	public final void set(boolean v1) { this.v1 = v1; }


	@Override
	public final OctetsStream marshal(OctetsStream os) {
		_.marshal_bool(os, this.v1);

		return os;
	}
	
	@Override
	public final OctetsStream unmarshal(OctetsStream os) throws MarshalException {
		this.v1 = _.unmarshal_bool(os);

		return os;
	}
		
	@Override
	public final OctetsStream marshalScheme(OctetsStream os) {
		os.marshalUInt(1);
		_.marshalscheme_bool(os, this.v1);

		return os;
	}

	@Override
	public final OctetsStream unmarshalScheme(OctetsStream os) throws MarshalException {
		switch(os.unmarshalUInt()) {
			case 1 : _.marshalscheme_bool(os, this.v1);
			case 0 : break;
			default: throw MarshalException.createEOF(false); 
		}

		return os;
	}

	@Override
	public TestBean clone() {
		final TestBean o = new TestBean();
		o.v1 = _.clone_bool(this.v1);

		return o;
	}

	@Override
	public int hashCode() {
		int h = 2 * 0x9e3779b1;
		h = h * 31 + _.hashcode_bool(this.v1);

		return h;
	}

	@Override
	public boolean equals(Object _b)	{
		if(_b == this) return true;
		if(!(_b instanceof TestBean)) return false;
		final TestBean _o = (TestBean)_b;
		if(!_.equals_bool(this.v1, _o.v1)) return false;

		return _b == _o;
	}

	@Override
	public int compareTo(TestBean _o) {
		if(_o == this) return 0;
		if(_o == null) return 1;
		int c;
		if((c = _.compareto_bool(this.v1, _o.v1)) != 0) return c;

		return 0;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("TestBean{");
		s.append("v1 = "); s.append(_.tostring_bool(this.v1)); s.append(", ");

		return s.append('}').toString();
	}
}
