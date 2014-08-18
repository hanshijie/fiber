package fiber.bean;

import fiber.io.*;
import java.util.*;


class _ {
	public static boolean clone_bool(boolean x) { return x; }
	public static int hashcode_bool(boolean x) { return x ? 1 : 0; }
	public static boolean equals_bool(boolean x, boolean y) { return x == y; }
	public static int compareto_bool(boolean x, boolean y) { return x == y ? 0 : (x ? -1 : 1); }
	public static String tostring_bool(boolean x) { return Boolean.toString(x); }
	public static void marshal_bool(OctetsStream os, boolean x) { os.marshal(x); }
	public static boolean unmarshal_bool(OctetsStream os) throws MarshalException { return os.unmarshalBoolean(); }
	public static void marshalscheme_bool(OctetsStream os, boolean x) { os.marshal(x); }
	public static boolean unmarshalscheme_bool(OctetsStream os) throws MarshalException { return os.unmarshalBoolean(); }

	public static String clone_string(String x) { return x; }
	public static int hashcode_string(String x) { return x.hashCode(); }
	public static boolean equals_string(String x, String y) { return x.equals(y); }
	public static int compareto_string(String x, String y) { return x.compareTo(y); }
	public static String tostring_string(String x) { return x; }
	public static void marshal_string(OctetsStream os, String x) { os.marshal(x); }
	public static String unmarshal_string(OctetsStream os) throws MarshalException { return os.unmarshalString(); }
	public static void marshalscheme_string(OctetsStream os, String x) { os.marshal(x); }
	public static String unmarshalscheme_string(OctetsStream os) throws MarshalException { return os.unmarshalString(); }

	public static HashMap<Long, String> clone_hashmap_long_string(HashMap<Long, String> x) { HashMap<Long, String> y = new HashMap<Long, String>(); for(Map.Entry<Long, String> e : x.entrySet()) { y.put(clone_long(e.getKey()), clone_string(e.getValue())); } return y; }
	public static int hashcode_hashmap_long_string(HashMap<Long, String> x) { int h = 0x9e3779b1 * x.size(); for(Map.Entry<Long, String> e : x.entrySet()) h = h * 31 + hashcode_long(e.getKey()) + hashcode_string(e.getValue()); return h; }
	public static boolean equals_hashmap_long_string(HashMap<Long, String> x, HashMap<Long, String> y) { return x.equals(y); }
	public static int compareto_hashmap_long_string(HashMap<Long, String> x, HashMap<Long, String> y) { return 0; }
	public static String tostring_hashmap_long_string(HashMap<Long, String> x) { return x.toString(); }
	public static void marshal_hashmap_long_string(OctetsStream os, HashMap<Long, String> x) { os.marshalUInt(x.size()); for(Map.Entry<Long, String> e : x.entrySet()) { marshal_long(os, e.getKey()); marshal_string(os, e.getValue()); } }
	public static HashMap<Long, String> unmarshal_hashmap_long_string(OctetsStream os) throws MarshalException { HashMap<Long, String> y = new HashMap<Long, String>(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.put(unmarshal_long(os), unmarshal_string(os)); return y; }
	public static void marshalscheme_hashmap_long_string(OctetsStream os, HashMap<Long, String> x) { os.marshalUInt(x.size()); for(Map.Entry<Long, String> e : x.entrySet()) { marshal_long(os, e.getKey()); marshalscheme_string(os, e.getValue()); } }
	public static HashMap<Long, String> unmarshalscheme_hashmap_long_string(OctetsStream os) throws MarshalException { HashMap<Long, String> y = new HashMap<Long, String>(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.put(unmarshal_long(os), unmarshalscheme_string(os)); return y; }

	public static short clone_short(short x) { return x; }
	public static int hashcode_short(short x) { return x; }
	public static boolean equals_short(short x, short y) { return x == y; }
	public static int compareto_short(short x, short y) { return x - y; }
	public static String tostring_short(short x) { return Short.toString(x); }
	public static void marshal_short(OctetsStream os, short x) { os.marshal(x); }
	public static short unmarshal_short(OctetsStream os) throws MarshalException { return os.unmarshalShort(); }
	public static void marshalscheme_short(OctetsStream os, short x) { os.marshal(x); }
	public static short unmarshalscheme_short(OctetsStream os) throws MarshalException { return os.unmarshalShort(); }

	public static Octets clone_binary(Octets x) { return x.clone(); }
	public static int hashcode_binary(Octets x) { return x.hashCode(); }
	public static boolean equals_binary(Octets x, Octets y) { return x.equals(y); }
	public static int compareto_binary(Octets x, Octets y) { return x.compareTo(y); }
	public static String tostring_binary(Octets x) { return x.toString(); }
	public static void marshal_binary(OctetsStream os, Octets x) { os.marshal(x); }
	public static Octets unmarshal_binary(OctetsStream os) throws MarshalException { return os.unmarshalOctets(); }
	public static void marshalscheme_binary(OctetsStream os, Octets x) { os.marshal(x); }
	public static Octets unmarshalscheme_binary(OctetsStream os) throws MarshalException { return os.unmarshalOctets(); }

	public static float clone_float(float x) { return x; }
	public static int hashcode_float(float x) { return ((Float)x).hashCode(); }
	public static boolean equals_float(float x, float y) { return x == y; }
	public static int compareto_float(float x, float y) { return Float.compare(x, y); }
	public static String tostring_float(float x) { return Float.toString(x); }
	public static void marshal_float(OctetsStream os, float x) { os.marshal(x); }
	public static float unmarshal_float(OctetsStream os) throws MarshalException { return os.unmarshalFloat(); }
	public static void marshalscheme_float(OctetsStream os, float x) { os.marshal(x); }
	public static float unmarshalscheme_float(OctetsStream os) throws MarshalException { return os.unmarshalFloat(); }

	public static TreeMap<TestBean, Boolean> clone_treemap_TestBean_bool(TreeMap<TestBean, Boolean> x) { TreeMap<TestBean, Boolean> y = new TreeMap<TestBean, Boolean>(); for(Map.Entry<TestBean, Boolean> e : x.entrySet()) { y.put(clone_TestBean(e.getKey()), clone_bool(e.getValue())); } return y; }
	public static int hashcode_treemap_TestBean_bool(TreeMap<TestBean, Boolean> x) { int h = 0x9e3779b1 * x.size(); for(Map.Entry<TestBean, Boolean> e : x.entrySet()) h = h * 31 + hashcode_TestBean(e.getKey()) + hashcode_bool(e.getValue()); return h; }
	public static boolean equals_treemap_TestBean_bool(TreeMap<TestBean, Boolean> x, TreeMap<TestBean, Boolean> y) { return x.equals(y); }
	public static int compareto_treemap_TestBean_bool(TreeMap<TestBean, Boolean> x, TreeMap<TestBean, Boolean> y) { return 0; }
	public static String tostring_treemap_TestBean_bool(TreeMap<TestBean, Boolean> x) { return x.toString(); }
	public static void marshal_treemap_TestBean_bool(OctetsStream os, TreeMap<TestBean, Boolean> x) { os.marshalUInt(x.size()); for(Map.Entry<TestBean, Boolean> e : x.entrySet()) { marshal_TestBean(os, e.getKey()); marshal_bool(os, e.getValue()); } }
	public static TreeMap<TestBean, Boolean> unmarshal_treemap_TestBean_bool(OctetsStream os) throws MarshalException { TreeMap<TestBean, Boolean> y = new TreeMap<TestBean, Boolean>(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.put(unmarshal_TestBean(os), unmarshal_bool(os)); return y; }
	public static void marshalscheme_treemap_TestBean_bool(OctetsStream os, TreeMap<TestBean, Boolean> x) { os.marshalUInt(x.size()); for(Map.Entry<TestBean, Boolean> e : x.entrySet()) { marshal_TestBean(os, e.getKey()); marshalscheme_bool(os, e.getValue()); } }
	public static TreeMap<TestBean, Boolean> unmarshalscheme_treemap_TestBean_bool(OctetsStream os) throws MarshalException { TreeMap<TestBean, Boolean> y = new TreeMap<TestBean, Boolean>(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.put(unmarshal_TestBean(os), unmarshalscheme_bool(os)); return y; }

	public static long clone_long(long x) { return x; }
	public static int hashcode_long(long x) { return ((Long)x).hashCode(); }
	public static boolean equals_long(long x, long y) { return x == y; }
	public static int compareto_long(long x, long y) { return Long.signum(x - y); }
	public static String tostring_long(long x) { return Long.toString(x); }
	public static void marshal_long(OctetsStream os, long x) { os.marshal(x); }
	public static long unmarshal_long(OctetsStream os) throws MarshalException { return os.unmarshalLong(); }
	public static void marshalscheme_long(OctetsStream os, long x) { os.marshal(x); }
	public static long unmarshalscheme_long(OctetsStream os) throws MarshalException { return os.unmarshalLong(); }

	public static HashSet<Long> clone_hashset_long(HashSet<Long> x) { HashSet<Long> y = new HashSet<Long>(); for(Long e : x) { y.add(clone_long(e)); } return y; }
	public static int hashcode_hashset_long(HashSet<Long> x) { int h = 0x9e3779b1 * x.size(); for(Long e : x) h = h * 31 + hashcode_long(e); return h; }
	public static boolean equals_hashset_long(HashSet<Long> x, HashSet<Long> y) { return x.equals(y); }
	public static int compareto_hashset_long(HashSet<Long> x, HashSet<Long> y) { return 0; }
	public static String tostring_hashset_long(HashSet<Long> x) { return x.toString(); }
	public static void marshal_hashset_long(OctetsStream os, HashSet<Long> x) { os.marshalUInt(x.size()); for(Long e : x) marshal_long(os, e); }
	public static HashSet<Long> unmarshal_hashset_long(OctetsStream os) throws MarshalException { HashSet<Long> y = new HashSet<Long>(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.add(unmarshal_long(os)); return y; }
	public static void marshalscheme_hashset_long(OctetsStream os, HashSet<Long> x) { os.marshalUInt(x.size()); for(Long e : x) marshalscheme_long(os, e); }
	public static HashSet<Long> unmarshalscheme_hashset_long(OctetsStream os) throws MarshalException { HashSet<Long> y = new HashSet<Long>(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.add(unmarshalscheme_long(os)); return y; }

	public static int clone_int(int x) { return x; }
	public static int hashcode_int(int x) { return x; }
	public static boolean equals_int(int x, int y) { return x == y; }
	public static int compareto_int(int x, int y) { return x - y; }
	public static String tostring_int(int x) { return Integer.toString(x); }
	public static void marshal_int(OctetsStream os, int x) { os.marshal(x); }
	public static int unmarshal_int(OctetsStream os) throws MarshalException { return os.unmarshalInteger(); }
	public static void marshalscheme_int(OctetsStream os, int x) { os.marshal(x); }
	public static int unmarshalscheme_int(OctetsStream os) throws MarshalException { return os.unmarshalInteger(); }

	public static ArrayList<Boolean> clone_vector_bool(ArrayList<Boolean> x) { ArrayList<Boolean> y = new ArrayList<Boolean>(); for(Boolean e : x) { y.add(clone_bool(e)); } return y; }
	public static int hashcode_vector_bool(ArrayList<Boolean> x) { int h = 0x9e3779b1 * x.size(); for(Boolean e : x) h = h * 31 + hashcode_bool(e); return h; }
	public static boolean equals_vector_bool(ArrayList<Boolean> x, ArrayList<Boolean> y) { return x.equals(y); }
	public static int compareto_vector_bool(ArrayList<Boolean> x, ArrayList<Boolean> y) { return 0; }
	public static String tostring_vector_bool(ArrayList<Boolean> x) { return x.toString(); }
	public static void marshal_vector_bool(OctetsStream os, ArrayList<Boolean> x) { os.marshalUInt(x.size()); for(Boolean e : x) marshal_bool(os, e); }
	public static ArrayList<Boolean> unmarshal_vector_bool(OctetsStream os) throws MarshalException { ArrayList<Boolean> y = new ArrayList<Boolean>(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.add(unmarshal_bool(os)); return y; }
	public static void marshalscheme_vector_bool(OctetsStream os, ArrayList<Boolean> x) { os.marshalUInt(x.size()); for(Boolean e : x) marshalscheme_bool(os, e); }
	public static ArrayList<Boolean> unmarshalscheme_vector_bool(OctetsStream os) throws MarshalException { ArrayList<Boolean> y = new ArrayList<Boolean>(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.add(unmarshalscheme_bool(os)); return y; }

	public static byte clone_byte(byte x) { return x; }
	public static int hashcode_byte(byte x) { return x; }
	public static boolean equals_byte(byte x, byte y) { return x == y; }
	public static int compareto_byte(byte x, byte y) { return x - y; }
	public static String tostring_byte(byte x) { return Byte.toString(x); }
	public static void marshal_byte(OctetsStream os, byte x) { os.marshal(x); }
	public static byte unmarshal_byte(OctetsStream os) throws MarshalException { return os.unmarshalByte(); }
	public static void marshalscheme_byte(OctetsStream os, byte x) { os.marshal(x); }
	public static byte unmarshalscheme_byte(OctetsStream os) throws MarshalException { return os.unmarshalByte(); }

	public static TreeSet<Float> clone_treeset_float(TreeSet<Float> x) { TreeSet<Float> y = new TreeSet<Float>(); for(Float e : x) { y.add(clone_float(e)); } return y; }
	public static int hashcode_treeset_float(TreeSet<Float> x) { int h = 0x9e3779b1 * x.size(); for(Float e : x) h = h * 31 + hashcode_float(e); return h; }
	public static boolean equals_treeset_float(TreeSet<Float> x, TreeSet<Float> y) { return x.equals(y); }
	public static int compareto_treeset_float(TreeSet<Float> x, TreeSet<Float> y) { return 0; }
	public static String tostring_treeset_float(TreeSet<Float> x) { return x.toString(); }
	public static void marshal_treeset_float(OctetsStream os, TreeSet<Float> x) { os.marshalUInt(x.size()); for(Float e : x) marshal_float(os, e); }
	public static TreeSet<Float> unmarshal_treeset_float(OctetsStream os) throws MarshalException { TreeSet<Float> y = new TreeSet<Float>(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.add(unmarshal_float(os)); return y; }
	public static void marshalscheme_treeset_float(OctetsStream os, TreeSet<Float> x) { os.marshalUInt(x.size()); for(Float e : x) marshalscheme_float(os, e); }
	public static TreeSet<Float> unmarshalscheme_treeset_float(OctetsStream os) throws MarshalException { TreeSet<Float> y = new TreeSet<Float>(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.add(unmarshalscheme_float(os)); return y; }

	public static TestBean clone_TestBean(TestBean x) { return x.clone(); }
	public static int hashcode_TestBean(TestBean x) { return x.hashCode(); }
	public static boolean equals_TestBean(TestBean x, TestBean y) { return x.equals(y); }
	public static int compareto_TestBean(TestBean x, TestBean y) { return x.compareTo(y); }
	public static String tostring_TestBean(TestBean x) { return x.toString(); }
	public static void marshal_TestBean(OctetsStream os, TestBean x) { os.marshal(x); }
	public static TestBean unmarshal_TestBean(OctetsStream os) throws MarshalException { TestBean o = new TestBean(); o.unmarshal(os); return o; }
	public static void marshalscheme_TestBean(OctetsStream os, TestBean x) { os.marshal(x); }
	public static TestBean unmarshalscheme_TestBean(OctetsStream os) throws MarshalException { TestBean o = new TestBean(); o.unmarshalScheme(os); return o; }

	public static double clone_double(double x) { return x; }
	public static int hashcode_double(double x) { return ((Double)x).hashCode(); }
	public static boolean equals_double(double x, double y) { return x == y; }
	public static int compareto_double(double x, double y) { return Double.compare(x, y); }
	public static String tostring_double(double x) { return Double.toString(x); }
	public static void marshal_double(OctetsStream os, double x) { os.marshal(x); }
	public static double unmarshal_double(OctetsStream os) throws MarshalException { return os.unmarshalDouble(); }
	public static void marshalscheme_double(OctetsStream os, double x) { os.marshal(x); }
	public static double unmarshalscheme_double(OctetsStream os) throws MarshalException { return os.unmarshalDouble(); }

}
