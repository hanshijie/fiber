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
local prop_path = prop_path or "fiber/prop"
local helperClass = "_"
local namespace = namespace or "fiber"
local suffix = '.csv'

local allprops = {}
local allapps = {}
local helptypes = {}
local typeclass = {}

SIZE = 1
END = 2

-- 用于指示读取容器类数据的终结方式
local SIZE = SIZE  -- 先从当前单元格读到数据个数，然后读取指定个数据.
local END = END -- 当前行结束,当前文件结束,或者 遇到  <END>

local context = {
	prop = nil,  	-- current prop
	var = nil, 		-- current variable
}


local template_prop = [=[
package fiber.prop;

import fiber.common.CSVParser;
$(prop_import)

public final class $(prop.name){

$(prop_define)

	public $(prop.name)() {
$(prop_default_init)
	}
		
$(prop_getter_setter)

	public void unmarshal(CSVParser os) throws Exception {
		try {
$(prop_unmarshal)
$(prop_nextline)
		} catch(Exception e) {
			os.dump();
			throw e;
		}
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("$(prop.name){");
$(prop_tostring)
		return s.append('}').toString();
	}
}
]=]

local template_app = [=[
package fiber.prop;
import fiber.common.CSVParser;
$(prop_import)

public final class $(prop.name){
	private String _basepath = "";
	public void setBasePath(String path) { _basepath = path; }
	
$(prop_define)

	public $(prop.name)() {
$(prop_default_init)
	}
		
$(prop_getter_setter)

	public void unmarshal() throws Exception {
$(app_unmarshal)
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("$(prop.name){");
$(prop_tostring)
		return s.append('}').toString();
	}
}
]=]

local template_helper = [=[
package fiber.prop;

$(helper_imports)

public class $(helper_class) {
$(helper_methods)
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
	int = "Integer",
	long = "Long",
	float = "Float",
	double = "Double",
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
	return pos and type:sub(1, pos - 1) or (typeclass[type] and type or "prop")
end
	
local function get_sidtype(type)
	return type:gsub(">", ""):gsub("[^%w]", "_")
end

local function get_objtype(rawtype)
	return get_container_keytype(rawtype)
end

local containtypes = { vector = true, hashset = true, treeset = true, hashmap = true, treemap = true, treeentry = true, hashentry = true}
local function is_container(type)
	return containtypes[type]
end


local tc = typeclass

tc.bool = {
	sid = function (var) return get_sidtype(var.type) end,
	basetype = function (var) return var.type end,
	finaltype = function () return "boolean" end,
	default = function () return "false" end,
	
	tostring = function(var, tc) return string.format("\tpublic static String tostring_%s(%s x) { return %s.toString(x); }", var.sid, var.finaltype, get_objtype(var.type)) end,
	unmarshal = function(var, tc) return string.format("\tpublic static %s unmarshal_%s(CSVParser os) throws Exception { return os.read%s(); }", var.finaltype, var.sid, get_objtype(var.type)) end,
}

tc.int = merge(tc.bool, {
	finaltype = function (var) return var.type end,
	default = function(var) return 0 end,
})

tc.long = merge(tc.int, {})

tc.float = merge(tc.long, {})

tc.double = merge(tc.float, {})

tc.string = merge(tc.int, {
	finaltype = function () return "String" end,
	default = function () return "\"\"" end,
	
	tostring = function(var, tc) return string.format("\tpublic static String tostring_%s(%s x) { return x; }", var.sid, var.finaltype) end,
})

tc.vector = merge(tc.int, {
	basetype = function (var) return var.type:sub(1, var.type:find("<") - 1) end,
	finalbasetype = "ArrayList",
	value = function (var) return var.type:gmatch("<(%w+)>")() end,
	finalvalue = function (var) return get_container_keytype(var.value) end,
	finaltype = function (var, tc) return string.format("%s<%s>", tc.finalbasetype, var.finalvalue) end,
	default = function (var) return string.format("new %s()", var.finaltype) end,

	tostring = function(var, tc) return string.format("\tpublic static String tostring_%s(%s x) { return x.toString(); }", var.sid, var.finaltype) end,
	unmarshal = function(var, tc)
	local value_sid = get_sidtype(var.value)
	return string.format("\tpublic static %s unmarshal_%s(int mode, CSVParser os) throws Exception { %s y = new %s();" .. 
	"if(mode == 1) { for(int n = os.readInteger() ; n > 0 ; n--) y.add(unmarshal_%s(os)); }" .. 
	"else { while(!os.isEnd()) y.add(unmarshal_%s(os));  } return y; }",
	 var.finaltype, var.sid, var.finaltype, var.finaltype, value_sid, value_sid) end,
})

tc.hashset = merge(tc.vector, {
	finalbasetype = "HashSet",
})

tc.treeset = merge(tc.hashset, {
	finalbasetype = "TreeSet",
})

tc.hashmap = merge(tc.treeset, {
	finalbasetype = "HashMap",
	key = function (var) return var.type:gmatch("<(%w+),(%w+)>")() end,
	value = function (var)  local _, v = var.type:gmatch("<(%w+),(%w+)>")()  return v end,
	finalkey = function (var) return get_container_keytype(var.key) end,
	finalvalue = function (var) return get_container_keytype(var.value) end,
	finaltype = function (var, tc) return string.format("%s<%s, %s>", tc.finalbasetype, var.finalkey, var.finalvalue) end,

	unmarshal = function(var, tc)
	local key_sid = get_sidtype(var.key)
	local value_sid = get_sidtype(var.value)
	return string.format("\tpublic static %s unmarshal_%s(int mode, CSVParser os) throws Exception { %s y = new %s();" .. 
	"if(mode == 1) { for(int n = os.readInteger() ; n > 0 ; n--) y.put(unmarshal_%s(os), unmarshal_%s(os)); } " .. 
	"else {while(!os.isEnd()) y.put(unmarshal_%s(os), unmarshal_%s(os)); } return y; }", 
	var.finaltype, var.sid, var.finaltype, var.finaltype, key_sid, value_sid, key_sid, value_sid) end,
})

tc.treemap = merge(tc.hashmap, {
	finalbasetype = "TreeMap",
})

tc.hashentry = merge(tc.hashmap, {
	key = function (var) return "int" end,
	value = function (var) return var.type:gmatch("<(%w+)>")() end,
		
	unmarshal = function(var, tc) 
	local value_sid = get_sidtype(var.value)
	return string.format("\tpublic static %s unmarshal_%s(int mode, CSVParser os) throws Exception { %s y = new %s(); " .. 
	"if(mode == 1) { for(int n = os.readInteger() ; n > 0 ; n--)  { %s v = unmarshal_%s(os); y.put(v.getid(), v); } }" .. 
	"else { while(!os.isEnd()) { %s v = unmarshal_%s(os); y.put(v.getid(), v); } } return y; }",
	var.finaltype, var.sid, var.finaltype, var.finaltype, var.finalvalue, var.finalvalue, value_sid, var.finalvalue, value_sid) end,
})

tc.treeentry = merge(tc.hashentry, {
	finalbasetype = "TreeMap",
})

tc.prop = merge(tc.int, {
	basetype = function (var) return "prop" end,
	finaltype = function (var) return var.type end,
	default = function(var) return string.format("new %s()", var.type) end,

	tostring = function(var, tc) return string.format("\tpublic static String tostring_%s(%s x) { return x.toString(); }", var.sid, var.finaltype) end,
	unmarshal = function(var, tc) return string.format("\tpublic static %s unmarshal_%s(CSVParser os) throws Exception { %s o = new %s(); o.unmarshal(os); return o; }", var.finaltype, var.sid, var.finaltype, var.finaltype) end,
})

local function processvar(i, var)
	local helper = helperClass .. "."	
	if var.skipline or var.skiprow then
		var.prop_define = ""
		var.prop_default_init = ""
		var.prop_tostring = ""
		var.prop_unmarshal = var.skipline and string.format("\t\tos.skipline(%d);\n", var.skipline) or string.format("\t\tos.skiprow(%d);\n", var.skiprow)
		var.prop_getter_setter = ""
	else
		var.type = var.type:gsub("%s", "")
		var.basetype = get_basetype(var.type)
		local ttype = typeclass[var.basetype]
		for _, field in ipairs({"sid", "basetype", "key", "value", "finalkey", "finalvalue", "finaltype", "default", }) do
			var[field] = ttype[field] and ttype[field](var, ttype)
		end
		helptypes[var.sid] = var		

		var.close = var.close or END
		var.tostring = helper .. "tostring_" .. var.sid
		var.unmarshal = helper .. "unmarshal_" .. var.sid
		
		var.prop_define = string.format("\t%s %s;\n", var.finaltype, var.name)
		var.prop_default_init = string.format("\t\tthis.%s = %s;\n", var.name, var.default)
		var.prop_tostring = string.format("\t\ts.append(\"%s = \"); s.append(%s(this.%s)); s.append(\", \");\n", var.name, var.tostring, var.name)
		if var.skip then
			var.prop_unmarshal = ""
		else
			local nextline = var.line and "os.skipline(1);" or ""
			if not is_container(var.basetype) then
			 	var.prop_unmarshal = string.format("\t\tthis.%s = %s(os);%s\n", var.name, var.unmarshal, nextline)
			else
				if not var.fromstring then
					var.prop_unmarshal = string.format("\t\tthis.%s = %s(%s, os);%s\n", var.name, var.unmarshal, var.close, nextline)
				else
					var.prop_unmarshal = string.format("\t\tthis.%s = %s(%s, os.getRecordParser());%s\n", var.name, var.unmarshal, var.close, nextline)
				end
			end
		end
		var.prop_getter_setter = string.format([=[ 
	public final %s get%s() { return this.%s; }
	public final void set%s(%s %s) { this.%s = %s; }
]=], var.finaltype, var.name, var.name, var.name, var.finaltype, var.name, var.name, var.name)
	end
end

function prop(b) 
	if allprops[b.name] then
		err("prop %s dumplicate!", b.name)
	end
	allprops[b.name] = b

	for i, var in ipairs(b) do
		processvar(i, var)
	end
	processvar(0, {name="___", type=b.name})
end

function app(a)
	if allapps[a.name] then
		err("prop %s dumplicate!", a.name)
	end
	allapps[a.name] = a

	for i, var in ipairs(a) do
		processvar(i, var)
		if var.basetype ~= "prop" then err("app<%s> var:%s type:%s must be prop type", a.name, var.type, var.type) end
		if not var.file then err("app<%s> var:%s miss attr<file>", a.name, var.name) end
		var.csvfile = var.file .. suffix
	end
end


dofile("property.lua")



function make_var_fun(name)
	context[name] = function(ctx) 
		local s = ""
		for i, var in ipairs(ctx.prop) do
			--if not var.skipline and not var.skiprow then
				s = s .. var[name]
			--end
		end
		return s
	end
end

local simple_var_funs = {
	"prop_define",
	"prop_default_init",
	"prop_tostring",
	"prop_unmarshal",
	"prop_getter_setter",
}


for _, fun in pairs(simple_var_funs) do
	make_var_fun(fun)
end

context.prop_nextline = function (ctx)
	return ctx.prop.line and "\t\tos.skipline(1);" or ""
end

	
context.prop_import = function (ctx)
	local s = ""
	for _, var in ipairs(ctx.prop) do
		if is_container(var.basetype) then
			s = s .. "import java.util.*;\n"
			break
		end
	end
	return s
end

context.helper_imports = function(ctx)
	local imports = {
		"java.util.*",
		"fiber.common.CSVParser",
	}
	return "import " .. table.concat(imports, ";\nimport ") .. ";\n"
end

context.helper_methods = function(ctx)
	local s = {}
	for sid, var in pairs(helptypes) do 
		local ttype = typeclass[var.basetype]
		for _, method in ipairs({"tostring", "unmarshal"}) do
			table.insert(s, ttype[method](var, ttype))
		end
		table.insert(s, "")
	end
	return table.concat(s, "\n")
end

context.app_unmarshal = function(ctx)
	local s = ""
	for _, var in ipairs(ctx.prop) do
		if not var.skip then
			s = s .. string.format("\t\tthis.%s = _.unmarshal_%s(CSVParser.fromFile(_basepath + \"%s\"));\n", var.name, var.type, var.csvfile)
		end
	end
	return s
end


function gen_props()
	local ctx = context
	local path = string.format("%s/%s", root, prop_path)
	for _, prop in pairs(allprops) do
		ctx.prop = prop
		gen_save(template_prop, string.format("%s/%s.java", path, prop.name), ctx)
	end
end

function gen_apps()
	local ctx = context
	local path = string.format("%s/%s", root, prop_path)
	for _, prop in pairs(allapps) do
		ctx.prop = prop
		gen_save(template_app, string.format("%s/%s.java", path, prop.name), ctx)
	end
end

function gen_helpers()
	local ctx = context
	ctx.helper_class = helperClass
	local path = string.format("%s/%s/%s.java", root, prop_path, helperClass)
	gen_save(template_helper, path, ctx)
end

gen_props()
gen_apps()
gen_helpers()

print "completed!"
