package fiber.bean;

import fiber.io.*;

import java.util.*;


public final class TestType implements Bean<TestType>
{
	public static final int TYPE = 3;
	public static final TestType STUB = new TestType();

	boolean v1;
	byte v2;
	short v3;
	int v4;
	long v5;
	float v6;
	double v7;
	Octets v8;
	String v9;
	ArrayList<Boolean> v10;
	HashSet<Long> v13;
	TreeSet<Float> v14;
	HashMap<Long, String> v16;
	TreeMap<TestBean, Boolean> v17;
	TestBean v19;


	public TestType()
	{
		this.v1 = false;
		this.v2 = 0;
		this.v3 = 0;
		this.v4 = 0;
		this.v5 = 0;
		this.v6 = 0.0f;
		this.v7 = 0.0;
		this.v8 = Octets.EMPTY;
		this.v9 = "";
		this.v10 = new ArrayList<Boolean>();
		this.v13 = new HashSet<Long>();
		this.v14 = new TreeSet<Float>();
		this.v16 = new HashMap<Long, String>();
		this.v17 = new TreeMap<TestBean, Boolean>();
		this.v19 = new TestBean();

	}

	public TestType (boolean v1, byte v2, short v3, int v4, long v5, float v6, double v7, Octets v8, String v9, ArrayList<Boolean> v10, HashSet<Long> v13, TreeSet<Float> v14, HashMap<Long, String> v16, TreeMap<TestBean, Boolean> v17, TestBean v19)
	{
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.v4 = v4;
		this.v5 = v5;
		this.v6 = v6;
		this.v7 = v7;
		this.v8 = v8;
		this.v9 = v9;
		this.v10 = v10;
		this.v13 = v13;
		this.v14 = v14;
		this.v16 = v16;
		this.v17 = v17;
		this.v19 = v19;

	}
	
	@Override public final TestType stub() { return STUB; }
	@Override public final int type() { return 3; }
	@Override public final int maxsize() { return 65536; }
	@Override public final TestType create() { return new TestType(); }
	
 
	public final boolean getv1() { return this.v1; }
	public final void set(boolean v1) { this.v1 = v1; }
 
	public final byte getv2() { return this.v2; }
	public final void set(byte v2) { this.v2 = v2; }
 
	public final short getv3() { return this.v3; }
	public final void set(short v3) { this.v3 = v3; }
 
	public final int getv4() { return this.v4; }
	public final void set(int v4) { this.v4 = v4; }
 
	public final long getv5() { return this.v5; }
	public final void set(long v5) { this.v5 = v5; }
 
	public final float getv6() { return this.v6; }
	public final void set(float v6) { this.v6 = v6; }
 
	public final double getv7() { return this.v7; }
	public final void set(double v7) { this.v7 = v7; }
 
	public final Octets getv8() { return this.v8; }
	public final void set(Octets v8) { this.v8 = v8; }
 
	public final String getv9() { return this.v9; }
	public final void set(String v9) { this.v9 = v9; }
 
	public final ArrayList<Boolean> getv10() { return this.v10; }
	public final void set(ArrayList<Boolean> v10) { this.v10 = v10; }
 
	public final HashSet<Long> getv13() { return this.v13; }
	public final void set(HashSet<Long> v13) { this.v13 = v13; }
 
	public final TreeSet<Float> getv14() { return this.v14; }
	public final void set(TreeSet<Float> v14) { this.v14 = v14; }
 
	public final HashMap<Long, String> getv16() { return this.v16; }
	public final void set(HashMap<Long, String> v16) { this.v16 = v16; }
 
	public final TreeMap<TestBean, Boolean> getv17() { return this.v17; }
	public final void set(TreeMap<TestBean, Boolean> v17) { this.v17 = v17; }
 
	public final TestBean getv19() { return this.v19; }
	public final void set(TestBean v19) { this.v19 = v19; }


	@Override
	public final OctetsStream marshal(OctetsStream os) {
		_.marshal_bool(os, this.v1);
		_.marshal_byte(os, this.v2);
		_.marshal_short(os, this.v3);
		_.marshal_int(os, this.v4);
		_.marshal_long(os, this.v5);
		_.marshal_float(os, this.v6);
		_.marshal_double(os, this.v7);
		_.marshal_binary(os, this.v8);
		_.marshal_string(os, this.v9);
		_.marshal_vector_bool(os, this.v10);
		_.marshal_hashset_long(os, this.v13);
		_.marshal_treeset_float(os, this.v14);
		_.marshal_hashmap_long_string(os, this.v16);
		_.marshal_treemap_TestBean_bool(os, this.v17);
		_.marshal_TestBean(os, this.v19);

		return os;
	}
	
	@Override
	public final OctetsStream unmarshal(OctetsStream os) throws MarshalException {
		this.v1 = _.unmarshal_bool(os);
		this.v2 = _.unmarshal_byte(os);
		this.v3 = _.unmarshal_short(os);
		this.v4 = _.unmarshal_int(os);
		this.v5 = _.unmarshal_long(os);
		this.v6 = _.unmarshal_float(os);
		this.v7 = _.unmarshal_double(os);
		this.v8 = _.unmarshal_binary(os);
		this.v9 = _.unmarshal_string(os);
		this.v10 = _.unmarshal_vector_bool(os);
		this.v13 = _.unmarshal_hashset_long(os);
		this.v14 = _.unmarshal_treeset_float(os);
		this.v16 = _.unmarshal_hashmap_long_string(os);
		this.v17 = _.unmarshal_treemap_TestBean_bool(os);
		this.v19 = _.unmarshal_TestBean(os);

		return os;
	}
		
	@Override
	public final OctetsStream marshalScheme(OctetsStream os) {
		os.marshalUInt(15);
		_.marshalscheme_TestBean(os, this.v19);
		_.marshalscheme_treemap_TestBean_bool(os, this.v17);
		_.marshalscheme_hashmap_long_string(os, this.v16);
		_.marshalscheme_treeset_float(os, this.v14);
		_.marshalscheme_hashset_long(os, this.v13);
		_.marshalscheme_vector_bool(os, this.v10);
		_.marshalscheme_string(os, this.v9);
		_.marshalscheme_binary(os, this.v8);
		_.marshalscheme_double(os, this.v7);
		_.marshalscheme_float(os, this.v6);
		_.marshalscheme_long(os, this.v5);
		_.marshalscheme_int(os, this.v4);
		_.marshalscheme_short(os, this.v3);
		_.marshalscheme_byte(os, this.v2);
		_.marshalscheme_bool(os, this.v1);

		return os;
	}

	@Override
	public final OctetsStream unmarshalScheme(OctetsStream os) throws MarshalException {
		switch(os.unmarshalUInt()) {
			case 15 : _.marshalscheme_TestBean(os, this.v19);
			case 14 : _.marshalscheme_treemap_TestBean_bool(os, this.v17);
			case 13 : _.marshalscheme_hashmap_long_string(os, this.v16);
			case 12 : _.marshalscheme_treeset_float(os, this.v14);
			case 11 : _.marshalscheme_hashset_long(os, this.v13);
			case 10 : _.marshalscheme_vector_bool(os, this.v10);
			case 9 : _.marshalscheme_string(os, this.v9);
			case 8 : _.marshalscheme_binary(os, this.v8);
			case 7 : _.marshalscheme_double(os, this.v7);
			case 6 : _.marshalscheme_float(os, this.v6);
			case 5 : _.marshalscheme_long(os, this.v5);
			case 4 : _.marshalscheme_int(os, this.v4);
			case 3 : _.marshalscheme_short(os, this.v3);
			case 2 : _.marshalscheme_byte(os, this.v2);
			case 1 : _.marshalscheme_bool(os, this.v1);
			case 0 : break;
			default: throw MarshalException.createEOF(false); 
		}

		return os;
	}

	@Override
	public TestType clone() {
		final TestType o = new TestType();
		o.v1 = _.clone_bool(this.v1);
		o.v2 = _.clone_byte(this.v2);
		o.v3 = _.clone_short(this.v3);
		o.v4 = _.clone_int(this.v4);
		o.v5 = _.clone_long(this.v5);
		o.v6 = _.clone_float(this.v6);
		o.v7 = _.clone_double(this.v7);
		o.v8 = _.clone_binary(this.v8);
		o.v9 = _.clone_string(this.v9);
		o.v10 = _.clone_vector_bool(this.v10);
		o.v13 = _.clone_hashset_long(this.v13);
		o.v14 = _.clone_treeset_float(this.v14);
		o.v16 = _.clone_hashmap_long_string(this.v16);
		o.v17 = _.clone_treemap_TestBean_bool(this.v17);
		o.v19 = _.clone_TestBean(this.v19);

		return o;
	}

	@Override
	public int hashCode() {
		int h = 3 * 0x9e3779b1;
		h = h * 31 + _.hashcode_bool(this.v1);
		h = h * 31 + _.hashcode_byte(this.v2);
		h = h * 31 + _.hashcode_short(this.v3);
		h = h * 31 + _.hashcode_int(this.v4);
		h = h * 31 + _.hashcode_long(this.v5);
		h = h * 31 + _.hashcode_float(this.v6);
		h = h * 31 + _.hashcode_double(this.v7);
		h = h * 31 + _.hashcode_binary(this.v8);
		h = h * 31 + _.hashcode_string(this.v9);
		h = h * 31 + _.hashcode_vector_bool(this.v10);
		h = h * 31 + _.hashcode_hashset_long(this.v13);
		h = h * 31 + _.hashcode_treeset_float(this.v14);
		h = h * 31 + _.hashcode_hashmap_long_string(this.v16);
		h = h * 31 + _.hashcode_treemap_TestBean_bool(this.v17);
		h = h * 31 + _.hashcode_TestBean(this.v19);

		return h;
	}

	@Override
	public boolean equals(Object _b)	{
		if(_b == this) return true;
		if(!(_b instanceof TestType)) return false;
		final TestType _o = (TestType)_b;
		if(!_.equals_bool(this.v1, _o.v1)) return false;
		if(!_.equals_byte(this.v2, _o.v2)) return false;
		if(!_.equals_short(this.v3, _o.v3)) return false;
		if(!_.equals_int(this.v4, _o.v4)) return false;
		if(!_.equals_long(this.v5, _o.v5)) return false;
		if(!_.equals_float(this.v6, _o.v6)) return false;
		if(!_.equals_double(this.v7, _o.v7)) return false;
		if(!_.equals_binary(this.v8, _o.v8)) return false;
		if(!_.equals_string(this.v9, _o.v9)) return false;
		if(!_.equals_vector_bool(this.v10, _o.v10)) return false;
		if(!_.equals_hashset_long(this.v13, _o.v13)) return false;
		if(!_.equals_treeset_float(this.v14, _o.v14)) return false;
		if(!_.equals_hashmap_long_string(this.v16, _o.v16)) return false;
		if(!_.equals_treemap_TestBean_bool(this.v17, _o.v17)) return false;
		if(!_.equals_TestBean(this.v19, _o.v19)) return false;

		return _b == _o;
	}

	@Override
	public int compareTo(TestType _o) {
		if(_o == this) return 0;
		if(_o == null) return 1;
		int c;
		if((c = _.compareto_bool(this.v1, _o.v1)) != 0) return c;
		if((c = _.compareto_byte(this.v2, _o.v2)) != 0) return c;
		if((c = _.compareto_short(this.v3, _o.v3)) != 0) return c;
		if((c = _.compareto_int(this.v4, _o.v4)) != 0) return c;
		if((c = _.compareto_long(this.v5, _o.v5)) != 0) return c;
		if((c = _.compareto_float(this.v6, _o.v6)) != 0) return c;
		if((c = _.compareto_double(this.v7, _o.v7)) != 0) return c;
		if((c = _.compareto_binary(this.v8, _o.v8)) != 0) return c;
		if((c = _.compareto_string(this.v9, _o.v9)) != 0) return c;
		if((c = _.compareto_vector_bool(this.v10, _o.v10)) != 0) return c;
		if((c = _.compareto_hashset_long(this.v13, _o.v13)) != 0) return c;
		if((c = _.compareto_treeset_float(this.v14, _o.v14)) != 0) return c;
		if((c = _.compareto_hashmap_long_string(this.v16, _o.v16)) != 0) return c;
		if((c = _.compareto_treemap_TestBean_bool(this.v17, _o.v17)) != 0) return c;
		if((c = _.compareto_TestBean(this.v19, _o.v19)) != 0) return c;

		return 0;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("TestType{");
		s.append("v1 = "); s.append(_.tostring_bool(this.v1)); s.append(", ");
		s.append("v2 = "); s.append(_.tostring_byte(this.v2)); s.append(", ");
		s.append("v3 = "); s.append(_.tostring_short(this.v3)); s.append(", ");
		s.append("v4 = "); s.append(_.tostring_int(this.v4)); s.append(", ");
		s.append("v5 = "); s.append(_.tostring_long(this.v5)); s.append(", ");
		s.append("v6 = "); s.append(_.tostring_float(this.v6)); s.append(", ");
		s.append("v7 = "); s.append(_.tostring_double(this.v7)); s.append(", ");
		s.append("v8 = "); s.append(_.tostring_binary(this.v8)); s.append(", ");
		s.append("v9 = "); s.append(_.tostring_string(this.v9)); s.append(", ");
		s.append("v10 = "); s.append(_.tostring_vector_bool(this.v10)); s.append(", ");
		s.append("v13 = "); s.append(_.tostring_hashset_long(this.v13)); s.append(", ");
		s.append("v14 = "); s.append(_.tostring_treeset_float(this.v14)); s.append(", ");
		s.append("v16 = "); s.append(_.tostring_hashmap_long_string(this.v16)); s.append(", ");
		s.append("v17 = "); s.append(_.tostring_treemap_TestBean_bool(this.v17)); s.append(", ");
		s.append("v19 = "); s.append(_.tostring_TestBean(this.v19)); s.append(", ");

		return s.append('}').toString();
	}
}
