package fiber.bean;

import fiber.io.*;



public class BFloat implements Bean<BFloat>, Comparable<BFloat> {
	public final static BFloat NULL = new BFloat() {
		public final void setValue(float value) { assert(false); }
	};
	public final static short TYPE = 7;
	
	private float value;
	public BFloat() {
		this.value = 0.0f;
	}
	
	public BFloat(float value) {
		this.value = value;
	}

	@Override
	public final BFloat clone() {
		try {
			return (BFloat)super.clone();
		} catch (CloneNotSupportedException e) {
			// impossible!
		}
		return null;
	}
	

	@Override
	public final BFloat create() {
		return new BFloat();
	}

	@Override
	public final short getType() {
		return TYPE;
	}

	@Override
	public final int getMaxSize() {
		return 1218;
	}

	@Override
	public final int getInitSize() {
		return 12;
	}

	@Override
	public final OctetsStream marshal(OctetsStream os) {
		os.marshal(this.value);
		return os;
	}

	@Override
	public final OctetsStream unmarshal(OctetsStream os) throws MarshalException {
		this.value = os.unmarshalFloat();
		return os;
	}

	@Override
	public final OctetsStream marshalScheme(OctetsStream os) {
		os.marshalUInt(1);
		os.marshal(this.value);
		return os;
	}

	@Override
	public final OctetsStream unmarshalScheme(OctetsStream os) throws MarshalException {
		switch(os.unmarshalUInt()) {
			default : throw MarshalException.createEOF(Const.unmarshal_exception_verbose);
			case 1: this.value = os.unmarshalFloat();
			case 0: ;
		}
		return os;
	}
	
	@Override
	public final int hashCode() {
		int h = 7 * 0x9e3779b1;
		h = 31 * h + 1 + (int)this.value;
		return h;
	}
	
	@Override
	public final boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof BFloat)) return false;
		BFloat _o = (BFloat)o;
		if(this.value != _o.value) return false;
		return _o == o;	
	}
	
		@Override
	public int compareTo(BFloat _o)
	{
		if(_o == this) return 0;
		if(_o == null) return 1;
		int c = 0;
		if(this.value != _o.value) return this.value < _o.value ? -1 : 1;
		return c;
	}

	@Override
	public final String toString() {
		StringBuilder _re = new StringBuilder();
		_re.append("BFloat{");
		_re.append("value = " + value + ", ");
		_re.append("}");
		return _re.toString();
	}

	public final float getValue() { return this.value; }
	public void setValue(float value) { this.value = value; }
}

	
	
