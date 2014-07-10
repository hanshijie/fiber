package fiber.bean;

import fiber.io.*;



public class BString implements Bean<BString>, Comparable<BString> {
	public final static BString NULL = new BString() {
		public final void setValue(String value) { assert(false); }
	};
	public final static short TYPE = 3;
	
	private String value;
	public BString() {
		this.value = "";
	}
	
	public BString(String value) {
		this.value = value;
	}

	@Override
	public final BString clone() {
		try {
			return (BString)super.clone();
		} catch (CloneNotSupportedException e) {
			// impossible!
		}
		return null;
	}
	

	@Override
	public final BString create() {
		return new BString();
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
		this.value = os.unmarshalString();
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
			case 1: this.value = os.unmarshalString();
			case 0: ;
		}
		return os;
	}
	
	@Override
	public final int hashCode() {
		int h = 3 * 0x9e3779b1;
		h = 31 * h + 1 + this.value.hashCode();
		return h;
	}
	
	@Override
	public final boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof BString)) return false;
		BString _o = (BString)o;
		if(!this.value.equals(_o.value)) return false;;
		return _o == o;	
	}
	
		@Override
	public int compareTo(BString _o)
	{
		if(_o == this) return 0;
		if(_o == null) return 1;
		int c = 0;
		c = this.value.compareTo(_o.value); if(c != 0) return c;
		return c;
	}

	@Override
	public final String toString() {
		StringBuilder _re = new StringBuilder();
		_re.append("BString{");
		_re.append("value = " + value + ", ");
		_re.append("}");
		return _re.toString();
	}
	
	public final String getValue() { return this.value; }
	public void setValue(String value) { this.value = value; }
}

	
	
