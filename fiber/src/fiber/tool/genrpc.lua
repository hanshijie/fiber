-- UTF-8 without BOM
local type = type
local string = string
local error = error
local pairs = pairs
local table = table
local concat = table.concat
local ipairs = ipairs
local print = print
local open = io.open

local root = root or "../.."
local bean_path = bean_path or "fiber/bean"
local handler_path = handler_path or "fiber/handler"
local helperClass = "_"
local namespace = namespace or "fiber"

local allbeans = {}
local allhandlers = {}
local beantypes = {}
local helptypes = {}
local typeclass = {}

local context = {
	bean = nil,  	-- current bean
	var = nil, 		-- current variable
	handler = nil, 	-- current handler
}


local template_bean = [=[
package fiber.bean;

import fiber.io.*;
$(bean_import)

public final class $(bean.name) implements Bean<$(bean.name)> {
	public static final int TYPE = $(bean.type);
	public static final $(bean.name) STUB = new $(bean.name)();

$(bean_define)

	public $(bean.name)() {
$(bean_default_init)
	}

	public $(bean.name) ($(bean_arg)) {
$(bean_init)
	}
	
	@Override public final $(bean.name) stub() { return STUB; }
	@Override public final int type() { return $(bean.type); }
	@Override public final int maxsize() { return $(bean.maxsize); }
	@Override public final $(bean.name) create() { return new $(bean.name)(); }
	public final $(bean.name) shallowClone() { try { return ($(bean.name))super.clone(); } catch (CloneNotSupportedException e) {	} return null; }
	
$(bean_getter_setter)

	@Override
	public final OctetsStream marshal(OctetsStream os) {
$(bean_marshal)
		return os;
	}
	
	@Override
	public final OctetsStream unmarshal(OctetsStream os) throws MarshalException {
$(bean_unmarshal)
		return os;
	}
		
	@Override
	public final OctetsStream marshalScheme(OctetsStream os) {
$(bean_marshalscheme)
		return os;
	}

	@Override
	public final OctetsStream unmarshalScheme(OctetsStream os) throws MarshalException {
$(bean_unmarshalscheme)
		return os;
	}

	@Override
	public $(bean.name) clone() {
		final $(bean.name) o = new $(bean.name)();
$(bean_clone)
		return o;
	}

	@Override
	public int hashCode() {
		int h = $(bean.type) * 0x9e3779b1;
$(bean_hashcode)
		return h;
	}

	@Override
	public boolean equals(Object _b)	{
		if(_b == this) return true;
		if(!(_b instanceof $(bean.name))) return false;
		final $(bean.name) _o = ($(bean.name))_b;
$(bean_equals)
		return _b == _o;
	}

	@Override
	public int compareTo($(bean.name) _o) {
		if(_o == this) return 0;
		if(_o == null) return 1;
		int c;
$(bean_compareto)
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("$(bean.name){");
$(bean_tostring)
		return s.append('}').toString();
	}
}
]=]

local template_rpcbean = [=[
package fiber.bean;

import fiber.io.*;

public final class $(bean.name) extends RpcBean<$(bean.arg), $(bean.res)> {
	public  static final $(bean.name) STUB = new $(bean.name)();
	public $(bean.name)() {}
	public $(bean.name)($(bean.arg) a) { arg = a; }
	@Override public final $(bean.name) stub() { return STUB; }
	@Override public final int maxsize() { return $(bean.maxsize); }
	@Override public final int type() { return $(bean.type); }
	@Override public final int getTimeout() { return $(bean.timeout); }
	@Override public final $(bean.name) create() { return new $(bean.name)(); }
	@Override public final $(bean.arg) createArg() { return new $(bean.arg)(); }
	@Override public final $(bean.res) createRes() { return new $(bean.res)(); }
}
]=]

local template_allbeans = [=[
package fiber.bean;

import java.util.Map;
import java.util.HashMap;
import fiber.io.Bean;

public final class AllBeans {
	private final static Map<Integer, Bean<?>> allbeans = new HashMap<Integer, Bean<?>>();
	static {	
$(beans_stub)
	}

	public static Map<Integer, Bean<?>> get() {
		return allbeans;
	}
}
]=]

local template_allhandlers = [=[
package fiber.handler.$(handler.path);

import java.util.Map;
import java.util.HashMap;
import fiber.io.BeanHandler;

public final class AllHandlers {
	private final static Map<Integer, BeanHandler<?>> allhandlers = new HashMap<Integer, BeanHandler<?>>();
	static {
$(handlers_stub)
	}

	public static Map<Integer, BeanHandler<?>> get() {
		return allhandlers;
	}
}
]=]

local template_bean_handler = [=[
package fiber.handler.$(handler.path);
/*
import fiber.app.server.Procedure;
import fiber.bean.UserLogin;
import fiber.common.TaskPool;
*/
import fiber.bean.$(bean.name);
import fiber.io.*;
import static fiber.io.Log.log;

public class $(bean.name)Handler extends BeanHandler<$(bean.name)> {
	@Override
	public void onProcess(final IOSession session, final $(bean.name) arg) {
		log.trace("$(bean.name)Handler.onProcess. sessionid:{}, arg:{}", session.getId(), arg);
		/*
		TaskPool.execute(new Procedure() {
			@Override
			protected void execute() throws Exception {
				log.trace("{}.execute. sessionid:{}, arg:{}", this, session.getId(), arg);
			}
			
			@Override
			protected void onRetError(int retcode, Object content) {
				log.err("{}.onRetError. retcode:{} content:{}", this, retcode, content);
			}

		});
		*/
	}
}
]=]

local template_rpc_handler = [=[
package fiber.handler.$(handler.path);

import fiber.io.*;
import fiber.bean.$(bean.arg);
import fiber.bean.$(bean.res);

public class $(bean.name)Handler extends RpcHandler<$(bean.arg), $(bean.res)> {
	@Override
	public boolean onServer(IOSession session, $(bean.arg) arg, $(bean.res) res) {
		return true;
	}

	@Override
	public void onClient(IOSession session, $(bean.arg) arg, $(bean.res) res) {
	}

	@Override
	public void onTimeout(IOSession session, $(bean.arg) arg) {
	}
}
]=]

local template_helper = [=[
package fiber.bean;

$(helper_imports)

public class $(helper_class) {
$(helper_methods)

$(helper_wrappers)
}
]=]

local function err(...)
	print("[error]", string.format(...))
	os.exit()
end

-- 变量求值
-- 如果name对应的值是字符串,则直接返回
-- 如果是函数, 则执行
-- name 可以是复合名字如  string.concat

local function eval(name, ctx)
-- 为毛不用loadstring("return " .. name)()
-- 因为这个比较慢.
	local pos = 1
	local m = ctx
	while true do
		local pstart, pend = name:find(".", pos, true)
		if pstart then
			m = m[name:sub(pos, pstart - 1)]
			if not m then return "###" .. name .. "###"	end
		else
			break
		end
		pos = pend + 1
	end
	m = m[name:sub(pos)]
	if type(m) == "string" or type(m) == "number" then
		return m
	elseif type(m) == "function" then
		return m(ctx)
	else 
		return "###" .. name .. "###"
	end
end

local function gen_template(template, ctx)
	return template:gsub("%$%((.-)%)", function (s) 
		return eval(s, ctx)
	end)
end

local function gen_save(template, path, ctx, noreplace)
	local content = gen_template(template, ctx)
	local outf = io.open(path)
	if noreplace and outf then return end
	if not outf or outf:read("*a") ~= content then
		if outf then outf:close() end
		outf = io.open(path, "w")
		if not outf then err("file:%s open fail! please create parent directory first!!!", path) end
		outf:write(content)
		outf:close()
		print("save :", path)
	end
end

local rawtype2objectmap = {
	bool = "Boolean",
	byte = "Byte",
	short = "Short",
	int = "Integer",
	long = "Long",
	float = "Float",
	double = "Double",
	binary = "Octets",
	string = "String",
}

local function get_container_keytype(keytype)
	return rawtype2objectmap[keytype] or keytype
end

local function merge(a, b)
	local c = {}
	for k, v in pairs(a) do
		c[k] = v
	end
	for k, v in pairs(b) do
		c[k] = v
	end
	return c
end

local function get_basetype(type)
	local pos = type:find("<", 1, true)
	return pos and type:sub(1, pos - 1) or (typeclass[type] and type or "bean")
end
	
local function get_sidtype(type)
	return type:gsub(">", ""):gsub("[^%w]", "_")
end

local function get_objtype(rawtype)
	return get_container_keytype(rawtype)
end

local function get_widtype(type)
	return type:gsub("[^%w]", "")
end

local containtypes = { vector = true, hashset = true, treeset = true, hashmap = true, treemap = true}
local function is_container(type)
	return containtypes[type]
end


local tc = typeclass

tc.bool = {
	sid = function (var) return get_sidtype(var.type) end,
	basetype = function (var) return var.type end,
	finaltype = function () return "boolean" end,
	default = function () return "false" end,
	
	clone = function (var, tc) return string.format("\tpublic static %s clone_%s(%s x) { return x; }", var.finaltype, var.sid, var.finaltype)  end,
	hashcode = function (var, tc) return string.format("\tpublic static int hashcode_%s(%s x) { return x ? 1 : 0; }", var.sid, var.finaltype) end,
	equals = function(var, tc) return string.format("\tpublic static boolean equals_%s(%s x, %s y) { return x == y; }", var.sid, var.finaltype, var.finaltype) end,
	compareto = function(var, tc) return string.format("\tpublic static int compareto_%s(%s x, %s y) { return x == y ? 0 : (x ? -1 : 1); }", var.sid, var.finaltype, var.finaltype) end,
	tostring = function(var, tc) return string.format("\tpublic static String tostring_%s(%s x) { return %s.toString(x); }", var.sid, var.finaltype, get_objtype(var.type)) end,
	marshal = function(var, tc) return string.format("\tpublic static void marshal_%s(OctetsStream os, %s x) { os.marshal(x); }", var.sid, var.finaltype) end,
	unmarshal = function(var, tc) return string.format("\tpublic static %s unmarshal_%s(OctetsStream os) throws MarshalException { return os.unmarshal%s(); }", var.finaltype, var.sid, get_objtype(var.type)) end,
	marshalscheme = function(var, tc) return string.format("\tpublic static void marshalscheme_%s(OctetsStream os, %s x) { os.marshal(x); }", var.sid, var.finaltype) end,
	unmarshalscheme = function(var, tc) return string.format("\tpublic static %s unmarshalscheme_%s(OctetsStream os) throws MarshalException { return os.unmarshal%s(); }", var.finaltype, var.sid, get_objtype(var.type)) end,
	
	wrappergettersetter = function(var, tc) return string.format("\t\tpublic %s get%s() { return this.data.get%s(); }\n", var.finaltype, var.name, var.name)
		.. string.format("\t\tpublic void set%s(%s %s) { checkModify(); this.data.set%s(%s); }", var.name, var.finaltype, var.name, var.name, var.name) end,
}

tc.byte = merge(tc.bool, {
	finaltype = function (var) return var.type end,
	default = function(var) return 0 end,
	
	hashcode = function (var, tc) return string.format("\tpublic static int hashcode_%s(%s x) { return x; }", var.sid, var.finaltype) end,
	compareto = function(var, tc) return string.format("\tpublic static int compareto_%s(%s x, %s y) { return x - y; }", var.sid, var.finaltype, var.finaltype) end,
})

tc.short = tc.byte

tc.int = tc.byte

tc.long = merge(tc.byte, {
	hashcode = function (var, tc) return string.format("\tpublic static int hashcode_%s(%s x) { return ((%s)x).hashCode(); }", var.sid, var.finaltype, get_objtype(var.type)) end,
	compareto = function(var, tc) return string.format("\tpublic static int compareto_%s(%s x, %s y) { return Long.signum(x - y); }", var.sid, var.finaltype, var.finaltype) end,	
})

tc.float = merge(tc.long, {
	compareto = function(var, tc) return string.format("\tpublic static int compareto_%s(%s x, %s y) { return %s.compare(x, y); }", var.sid, var.finaltype, var.finaltype, get_objtype(var.type)) end,
})

tc.double = merge(tc.float, {

})

tc.binary = merge(tc.byte, {
	finaltype = function () return "Octets" end,
	default = function() return "Octets.EMPTY" end,
	
	hashcode = function (var, tc) return string.format("\tpublic static int hashcode_%s(%s x) { return x.hashCode(); }", var.sid, var.finaltype) end,
	equals = function(var, tc) return string.format("\tpublic static boolean equals_%s(%s x, %s y) { return x.equals(y); }", var.sid, var.finaltype, var.finaltype) end,
	compareto = function(var, tc) return string.format("\tpublic static int compareto_%s(%s x, %s y) { return x.compareTo(y); }", var.sid, var.finaltype, var.finaltype) end,
	tostring = function(var, tc) return string.format("\tpublic static String tostring_%s(%s x) { return x.toString(); }", var.sid, var.finaltype) end,	
})

tc.string = merge(tc.binary, {
	finaltype = function () return "String" end,
	default = function () return "\"\"" end,
	
	tostring = function(var, tc) return string.format("\tpublic static String tostring_%s(%s x) { return x; }", var.sid, var.finaltype) end,
})

tc.vector = merge(tc.binary, {
	basetype = function (var) return var.type:sub(1, var.type:find("<") - 1) end,
	finalbasetype = "ArrayList",
	value = function (var) return var.type:gmatch("<(%w+)>")() end,
	finalvalue = function (var) return get_container_keytype(var.value) end,
	finaltype = function (var, tc) return string.format("%s<%s>", tc.finalbasetype, var.finalvalue) end,
	default = function (var) return string.format("new %s()", var.finaltype) end,

	clone = function (var, tc) return string.format("\tpublic static %s clone_%s(%s x) { %s y = new %s(); for(%s e : x) { y.add(clone_%s(e)); } return y; }", var.finaltype, var.sid, var.finaltype, var.finaltype, var.finaltype, var.finalvalue, get_sidtype(var.value))  end,
	hashcode = function (var, tc) return string.format("\tpublic static int hashcode_%s(%s x) { int h = 0x9e3779b1 * x.size(); for(%s e : x) h = h * 31 + hashcode_%s(e); return h; }", var.sid, var.finaltype, var.finalvalue, get_sidtype(var.value)) end,
	compareto = function(var, tc) return string.format("\tpublic static int compareto_%s(%s x, %s y) { return 0; }", var.sid, var.finaltype, var.finaltype) end,
	marshal = function(var, tc) return string.format("\tpublic static void marshal_%s(OctetsStream os, %s x) { os.marshalUInt(x.size()); for(%s e : x) marshal_%s(os, e); }", var.sid, var.finaltype, var.finalvalue, get_sidtype(var.value)) end,
	unmarshal = function(var, tc) return string.format("\tpublic static %s unmarshal_%s(OctetsStream os) throws MarshalException { %s y = new %s(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.add(unmarshal_%s(os)); return y; }", var.finaltype, var.sid, var.finaltype, var.finaltype, get_sidtype(var.value)) end,
	marshalscheme = function(var, tc) return string.format("\tpublic static void marshalscheme_%s(OctetsStream os, %s x) { os.marshalUInt(x.size()); for(%s e : x) marshalscheme_%s(os, e); }", var.sid, var.finaltype, var.finalvalue, get_sidtype(var.value)) end,
	unmarshalscheme = function(var, tc) return string.format("\tpublic static %s unmarshalscheme_%s(OctetsStream os) throws MarshalException { %s y = new %s(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.add(unmarshalscheme_%s(os)); return y; }", var.finaltype, var.sid, var.finaltype, var.finaltype, get_sidtype(var.value)) end,
	
	wrapperbasetype = "fiber.db.wrapper.WList",
	finalwrappertype = function (var, tc) return string.format("%s<%s>", tc.wrapperbasetype, var.finalvalue) end,
	wrappergettersetter = function(var, tc) 
		local finalwrappertype = tc.finalwrappertype(var, tc)
		local wrappervar = "_wrapper" .. var.name
		return string.format("\t\tprivate %s %s;\n", finalwrappertype, wrappervar)
		.. string.format("\t\tpublic %s get%s() { return %s != null ? %s : (%s = new %s(%s.class, data.get%s(), new Notifier() { @SuppressWarnings(\"unchecked\")",
			finalwrappertype, var.name, wrappervar, wrappervar, wrappervar, finalwrappertype, tc.finalbasetype, var.name)
		.. string.format("\t\t\tpublic void onChange(Object o) { checkModify(); data.set%s((%s)o); } })); }",
			 var.name, var.finaltype) end,
})

tc.hashset = merge(tc.vector, {
	finalbasetype = "HashSet",
	wrapperbasetype = "fiber.db.wrapper.WSet",
})

tc.treeset = merge(tc.hashset, {
	finalbasetype = "TreeSet",
})

tc.hashmap = merge(tc.treeset, {
	finalbasetype = "HashMap",
	key = function (var) return var.type:gmatch("<(%w+),(%w+)>")() end,
	value = function (var)  _, v = var.type:gmatch("<(%w+),(%w+)>")()  return v end,
	finalkey = function (var) return get_container_keytype(var.key) end,
	finalvalue = function (var) return get_container_keytype(var.value) end,
	finaltype = function (var, tc) return string.format("%s<%s, %s>", tc.finalbasetype, var.finalkey, var.finalvalue) end,
	
	clone = function (var, tc) return string.format("\tpublic static %s clone_%s(%s x) { %s y = new %s(); for(Map.Entry<%s, %s> e : x.entrySet()) { y.put(clone_%s(e.getKey()), clone_%s(e.getValue())); } return y; }", var.finaltype, var.sid, var.finaltype, var.finaltype, var.finaltype, var.finalkey, var.finalvalue, get_sidtype(var.key), get_sidtype(var.value))  end,
	hashcode = function (var, tc) return string.format("\tpublic static int hashcode_%s(%s x) { int h = 0x9e3779b1 * x.size(); for(Map.Entry<%s, %s> e : x.entrySet()) h = h * 31 + hashcode_%s(e.getKey()) + hashcode_%s(e.getValue()); return h; }", var.sid, var.finaltype, var.finalkey, var.finalvalue, get_sidtype(var.key), get_sidtype(var.value)) end,
	marshal = function(var, tc) return string.format("\tpublic static void marshal_%s(OctetsStream os, %s x) { os.marshalUInt(x.size()); for(Map.Entry<%s, %s> e : x.entrySet()) { marshal_%s(os, e.getKey()); marshal_%s(os, e.getValue()); } }", var.sid, var.finaltype, var.finalkey, var.finalvalue, get_sidtype(var.key), get_sidtype(var.value)) end,
	unmarshal = function(var, tc) return string.format("\tpublic static %s unmarshal_%s(OctetsStream os) throws MarshalException { %s y = new %s(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.put(unmarshal_%s(os), unmarshal_%s(os)); return y; }", var.finaltype, var.sid, var.finaltype, var.finaltype, get_sidtype(var.key), get_sidtype(var.value)) end,
	marshalscheme = function(var, tc) return string.format("\tpublic static void marshalscheme_%s(OctetsStream os, %s x) { os.marshalUInt(x.size()); for(Map.Entry<%s, %s> e : x.entrySet()) { marshal_%s(os, e.getKey()); marshalscheme_%s(os, e.getValue()); } }", var.sid, var.finaltype, var.finalkey, var.finalvalue, get_sidtype(var.key), get_sidtype(var.value)) end,
	unmarshalscheme = function(var, tc) return string.format("\tpublic static %s unmarshalscheme_%s(OctetsStream os) throws MarshalException { %s y = new %s(); for(int n = os.unmarshalUInt() ; n > 0 ; n--) y.put(unmarshal_%s(os), unmarshalscheme_%s(os)); return y; }", var.finaltype, var.sid, var.finaltype, var.finaltype, get_sidtype(var.key), get_sidtype(var.value)) end,
	
	wrapperbasetype = "fiber.db.wrapper.WMap",
	finalwrappertype = function (var, tc) return string.format("%s<%s, %s>", tc.wrapperbasetype, var.finalkey, var.finalvalue) end,
})

tc.treemap = merge(tc.hashmap, {
	finalbasetype = "TreeMap",
})

tc.bean = merge(tc.binary, {
	basetype = function (var) return "bean" end,
	finaltype = function (var) return var.type end,
	default = function(var) return string.format("new %s()", var.type) end,

	clone = function (var, tc) return string.format("\tpublic static %s clone_%s(%s x) { return x.clone(); }", var.finaltype, var.sid, var.finaltype)  end,
	hashcode = function (var, tc) return string.format("\tpublic static int hashcode_%s(%s x) { return x.hashCode(); }", var.sid, var.finaltype) end,
	equals = function(var, tc) return string.format("\tpublic static boolean equals_%s(%s x, %s y) { return x.equals(y); }", var.sid, var.finaltype, var.finaltype) end,
	compareto = function(var, tc) return string.format("\tpublic static int compareto_%s(%s x, %s y) { return x.compareTo(y); }", var.sid, var.finaltype, var.finaltype) end,
	marshal = function(var, tc) return string.format("\tpublic static void marshal_%s(OctetsStream os, %s x) { x.marshal(os); }", var.sid, var.finaltype) end,
	unmarshal = function(var, tc) return string.format("\tpublic static %s unmarshal_%s(OctetsStream os) throws MarshalException { %s o = new %s(); o.unmarshal(os); return o; }", var.finaltype, var.sid, var.finaltype, var.finaltype) end,
	marshalscheme = function(var, tc) return string.format("\tpublic static void marshalscheme_%s(OctetsStream os, %s x) { x.marshalScheme(os); }", var.sid, var.finaltype) end,
	unmarshalscheme = function(var, tc) return string.format("\tpublic static %s unmarshalscheme_%s(OctetsStream os) throws MarshalException { %s o = new %s(); o.unmarshalScheme(os); return o; }", var.finaltype, var.sid, var.finaltype, var.finaltype) end,
	
	wrappergettersetter = function(var, tc) 
		local finalwrappertype = "Wrapper" .. var.type
		local wrappervar = "_wrapper" .. var.name
		return string.format("\t\tprivate %s %s;\n", finalwrappertype, wrappervar)
		.. string.format("\t\tpublic %s get%s() { return %s != null ? %s : (%s = new %s(data.get%s(), new Notifier() {",
			finalwrappertype, var.name, wrappervar, wrappervar, wrappervar, finalwrappertype, var.name)
		.. string.format("\t\t\tpublic void onChange(Object o) { checkModify(); data.set%s((%s)o); } })); }",
			 var.name, var.finaltype) end,
})

tc.pvector = merge(tc.vector, {
	finalbasetype = "fiber.pcollections.ArrayList",	
	wrapperbasetype = "fiber.db.wrapper.WPList",
})

tc.phashset = merge(tc.hashset, {
	finalbasetype = "fiber.pcollections.HashSet",
	wrapperbasetype = "fiber.db.wrapper.WPSet",
})

tc.phashmap = merge(tc.hashmap, {
	finalbasetype = "fiber.pcollections.HashMap",
	wrapperbasetype = "fiber.db.wrapper.WPMap",
})

local function processvar(i, var)
	local helper = helperClass .. "."
	var.type = var.type:gsub("%s", "")
	var.basetype = get_basetype(var.type)
	local ttype = typeclass[var.basetype]
	for _, field in ipairs({"sid", "basetype", "key", "value", "finalkey", "finalvalue", "finaltype", "default", }) do
		var[field] = ttype[field] and ttype[field](var, ttype)
	end
	helptypes[var.sid] = var
	
	var.clone = helper .. "clone_" .. var.sid
	var.hashcode = helper .. "hashcode_" .. var.sid
	var.equals = helper .. "equals_" .. var.sid
	var.compareto = helper .. "compareto_" .. var.sid
	var.tostring = helper .. "tostring_" .. var.sid
	var.marshal = helper .. "marshal_" .. var.sid
	var.unmarshal = helper .. "unmarshal_" .. var.sid
	var.marshalscheme = helper .. "marshalscheme_" .. var.sid
	var.unmarshalscheme = helper .. "unmarshalscheme_" .. var.sid
	
	var.bean_define = string.format("\t%s %s;\n", var.finaltype, var.name)
	var.bean_arg = string.format("%s %s", var.finaltype, var.name)
	var.bean_init = string.format("\t\tthis.%s = %s;\n", var.name, var.name)
	var.bean_default_init = string.format("\t\tthis.%s = %s;\n", var.name, var.default)
	var.bean_clone = string.format("\t\to.%s = %s(this.%s);\n", var.name, var.clone, var.name)
	var.bean_hashcode = string.format("\t\th = h * 31 + %s(this.%s);\n", var.hashcode, var.name)
	var.bean_equals = string.format("\t\tif(!%s(this.%s, _o.%s)) return false;\n", var.equals, var.name, var.name)
	var.bean_compareto = string.format("\t\tif((c = %s(this.%s, _o.%s)) != 0) return c;\n", var.compareto, var.name, var.name)
	var.bean_tostring = string.format("\t\ts.append(\"%s = \"); s.append(%s(this.%s)); s.append(\", \");\n", var.name, var.tostring, var.name)
	var.bean_marshal = string.format("\t\t%s(os, this.%s);\n", var.marshal, var.name)
	var.bean_unmarshal = string.format("\t\tthis.%s = %s(os);\n", var.name, var.unmarshal)
	
	var.bean_marshalscheme = string.format("%s(os, this.%s);", var.marshalscheme, var.name)
	var.bean_unmarshalscheme = string.format("this.%s = %s(os);", var.name, var.unmarshalscheme)
	var.bean_getter_setter = string.format([=[ 
	public final %s get%s() { return this.%s; }
	public final void set%s(%s %s) { this.%s = %s; }
]=], var.finaltype, var.name, var.name, var.name, var.finaltype, var.name, var.name, var.name)
	if i ~= 1 then
		var.bean_arg = ", " .. var.bean_arg
	end
end

function bean(b) 
	if allbeans[b.name] then
		err("bean %s dumplicate!", b.name)
	end
	b.type = tostring(b.type)
	if beantypes[b.type] then
		err("bean<%s> type:%s dumplicate!", b.name, b.type)
	end
	beantypes[b.type] = b
	allbeans[b.name] = b

	if b.handlers then
		for _, handler in ipairs(b.handlers) do
			if not allhandlers[handler] then err("unknown handler:%s", handler) end
			table.insert(allhandlers[handler].beans, b)
		end
	end

	for i, var in ipairs(b) do
		processvar(i, var)
	end
	if not b.rpc then
		processvar(0, { name = "___", type = b.name, })
	end
end

function rpc(r) 
	r.rpc = true
	r.arg = r.arg and r.arg or r.name .. "Arg"
	r.res = r.res and r.res or r.name .. "Res"
	bean(r)
end

function handler(h)
	if allhandlers[h.name] then
		err("handler %s dumplicate", h.name)
	end
	h.beans = {}
	allhandlers[h.name] = h
end

function handlerset(s)
	return s
end

dofile("rpcalls.lua")



function make_var_fun(name)
	--print(name)
	context[name] = function(ctx) 
		local s = ""
		for i, var in ipairs(ctx.bean) do
			s = s .. var[name]
		end
		return s
	end
end

local simple_var_funs = {
	"bean_arg",
	"bean_define",
	"bean_init",
	"bean_default_init",
	"bean_clone",
	"bean_hashcode",
	"bean_equals",
	"bean_compareto",
	"bean_tostring",
	"bean_marshal",
	"bean_unmarshal",
	"bean_getter_setter",
}


for _, fun in pairs(simple_var_funs) do
	make_var_fun(fun)
end

	
context.bean_import = function (ctx)
	local s = ""
	for _, var in ipairs(ctx.bean) do
		if is_container(var.basetype) then
			s = s .. "import java.util.*;\n"
			break
		end
	end
	return s
end

context.bean_marshalscheme = function(ctx) 
	local s = "\t\tos.marshalUInt(" .. #ctx.bean .. ");\n"
	for i = #ctx.bean, 1, -1 do
		local var = ctx.bean[i]
		s = s .. "\t\t" .. var.bean_marshalscheme .. "\n"
	end
	return s
end


context.bean_unmarshalscheme = function(ctx) 
	local s = "\t\tswitch(os.unmarshalUInt()) {\n"
	for i = #ctx.bean, 1, -1 do
		local var = ctx.bean[i]
		s = s .. string.format("\t\t\tcase %d : %s\n", i, var.bean_unmarshalscheme)
	end
	s = s .. "\t\t\tcase 0 : break;\n"
	s = s .. "\t\t\tdefault: throw MarshalException.createEOF(false); \n"
	s = s .. "\t\t}\n"
	return s
end
	
context.beans_stub = function(ctx) 
	local s = {}
	for _, bean in pairs(allbeans) do
		table.insert(s, string.format("\t\tallbeans.put(%s, %s.STUB);", bean.type, bean.name))
	end
	return table.concat(s, "\n")
end

context.handlers_stub = function(ctx)
	local s = {}
	local handler = ctx.handler
	for _, bean in pairs(handler.beans) do
		table.insert(s, string.format("\t\tallhandlers.put(%s, new %sHandler());", bean.type, bean.name))
	end
	return table.concat(s, "\n")
end

context.helper_imports = function(ctx)
	local imports = {
		"fiber.io.*",
		"java.util.*",
		"fiber.db.Wrapper",
	}
	return "import " .. table.concat(imports, ";\nimport ") .. ";\n"
end

context.helper_methods = function(ctx)
	local s = {}
	for sid, var in pairs(helptypes) do 
		local ttype = typeclass[var.basetype]
		for _, method in ipairs({"clone", "hashcode", "equals", "compareto", "tostring", "marshal", "unmarshal", "marshalscheme", "unmarshalscheme"}) do
			table.insert(s, ttype[method](var, ttype))
		end
		table.insert(s, "")
	end
	return table.concat(s, "\n")
end


local template_wrapper = [=[  
	public static final class Wrapper$(bean.name) extends Wrapper<$(bean.name)> {
			public Wrapper$(bean.name)($(bean.name) w, Notifier n) {
			super(w, n);
		}

		@Override
		public $(bean.name) shallowClone() {
			return this.data != null ? this.data.shallowClone() : null;
		}
		
		@Override
		public void internalRefresh($(bean.name) o) {
			super.internalRefresh(o);
$(wrapper_refresh)
		}
		
$(wrapper_getter_setter)
	}
]=]

context.helper_wrappers = function(ctx)
	local s = {}
	for name, bean in pairs(allbeans) do 
		if not bean.rpc then
			ctx.bean = bean
			local content = gen_template(template_wrapper, ctx)
			table.insert(s, content)
		end
	end
	return table.concat(s, "\n")
end

context.wrapper_refresh = function(ctx)
	local s = ""
	for _, var in ipairs(ctx.bean) do
		if is_container(var.basetype) or var.basetype == "bean" then
			s = s .. string.format("\t\t\tif(_wrapper%s != null )_wrapper%s.internalRefresh(o != null ? o.get%s() : null);\n", var.name, var.name, var.name)
		end
	end
	return s
end

context.wrapper_getter_setter = function (ctx)
	local s = {}
	for _, var in ipairs(ctx.bean) do
		local ttype = typeclass[var.basetype]
		table.insert(s, ttype.wrappergettersetter(var, ttype))
	end
	return table.concat(s, "\n")
end

function gen_beans()
	local ctx = context
	local path = string.format("%s/%s", root, bean_path)
	gen_save(template_allbeans, string.format("%s/AllBeans.java", path), ctx)
	for _, bean in pairs(allbeans) do
		ctx.bean = bean
		if not bean.rpc then
			gen_save(template_bean, string.format("%s/%s.java", path, bean.name), ctx)
		else
			gen_save(template_rpcbean, string.format("%s/%s.java", path, bean.name), ctx)
		end
	end
end

function gen_handlers()
	local ctx = context
	for _, handler in pairs(allhandlers) do
		ctx.handler = handler
		local path = string.format("%s/%s/%s", root, handler_path, handler.path:gsub("%.", "/"))
		gen_save(template_allhandlers, string.format("%s/AllHandlers.java", path), ctx)
		for _, bean in ipairs(handler.beans) do
			ctx.bean = bean
			local noreplace = true
			if not bean.rpc then
				gen_save(template_bean_handler, string.format("%s/%sHandler.java", path, bean.name), ctx, noreplace)
			else
				gen_save(template_rpc_handler, string.format("%s/%sHandler.java", path, bean.name), ctx, noreplace)
			end
		end
	end
end

function gen_helpers()
	local ctx = context
	ctx.helper_class = helperClass
	local path = string.format("%s/%s/%s.java", root, bean_path, helperClass)
	gen_save(template_helper, path, ctx)
end

gen_beans()
gen_handlers()
gen_helpers()

print "completed!"
