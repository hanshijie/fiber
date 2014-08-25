local type = type
local string = string
local error = error
local pairs = pairs
local table = table
local concat = table.concat
local insert = table.insert
local ipairs = ipairs
local print = print

local namespace = namespace or "fiber"

local Types = {}
local typeclass = {}
local sids = {}

local types = { 
	bool = true, byte = true, short = true, int = true, long = true, binary = true, string = true,
 	treemap = true, hashmap = true, hashset = true, treeset = true, vector = true,
 	pvector = true, phashset = true, phashmap = true
 }
 
local function get_basetype(type)
	local pos = type:find("<", 1, true)
	type = pos and type:sub(1, pos - 1) or type
	if types[type] then return type end
	return "bean"
end

function bean(b) 
	b.type = tostring(b.type)
	Types[b.name] = b
	sids[b.name] = { name = "xxx", type=b.name, basetype = "bean", sid = b.name, default= "{}"}
	for i, var in ipairs(b) do
		var.type = var.type:gsub("%s", "")
		var.basetype = get_basetype(var.type)
		if not var.basetype then
			error("unknown type:" .. var.type ..":")
		end
		local ttype = typeclass[var.basetype]
		for _, field in ipairs({"sid", "key", "value", "default", }) do
			var[field] = ttype[field] and ttype[field](var, ttype)
		end
		sids[var.sid] = var
	end
end

function rpc(r) 
	bean(r)
	r.rpc = true
	r.arg = r.arg and r.arg or r.name .. "Arg"
	r.res = r.res and r.res or r.name .. "Res"
end

function handler(h)

end

function handlerset(s)
	return s
end

local function get_sidtype(type)
	return type:gsub(">", ""):gsub("[^%w]", "_")
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

local tc = typeclass

tc.bool = {
	sid = function (var) return get_sidtype(var.type) end,
	default = function () return "false" end,
	
	marshal = function(var, tc) return string.format("os:marshal_%s(self.%s)", var.sid, var.name) end,
	unmarshal = function(var, tc) return string.format("self.%s = os:unmarshal_%s()", var.name, var.sid) end,
}

tc.byte = merge(tc.bool, {
	default = function(var) return 0 end,
})

tc.short = tc.byte

tc.int = tc.byte

tc.long = tc.byte

tc.float = tc.byte

tc.double = tc.float

tc.binary = merge(tc.byte, {
	default = function() return "\"\"" end,
})

tc.string = merge(tc.binary, {
	default = function () return "\"\"" end,
})

tc.vector = merge(tc.binary, {
	value = function (var) return var.type:gmatch("<(%w+)>")() end,
	default = function (var) return "{}" end,
	
	marshal = function(var, tc) return string.format("os:marshal_uint(#self.%s); for _, v in ipairs(self.%s) do os:marshal_%s(v) end", var.name, var.name, var.value) end,
	unmarshal = function(var, tc) return string.format("for i = 1, os:unmarshal_uint() do insert(self.%s, os:unmarshal_%s()) end", var.name, var.value) end,
})

tc.hashset = merge(tc.vector, {
	marshal = function(var, tc) return string.format("os:marshal_uint(tablelength(self.%s)); for k, _ in ipairs(self.%s) do os:marshal_%s(k) end", var.name, var.name, var.value) end,
	unmarshal = function(var, tc) return string.format("for i = 1, os:unmarshal_uint() do self.%s[os:marshal_%s()] = true end", var.name, var.value) end,
})

tc.treeset = tc.vector

tc.hashmap = merge(tc.treeset, {
	key = function (var) return var.type:gmatch("<(%w+),(%w+)>")() end,
	value = function (var)  _, v = var.type:gmatch("<(%w+),(%w+)>")()  return v end,
	marshal = function(var, tc) return string.format("os:marshal_uint(tablelength(self.%s)); for k, v in ipairs(self.%s) do os:marshal_%s(k) os:marshal_%s(v) end", var.name, var.name, var.key, var.value) end,
	unmarshal = function(var, tc) return string.format("for i = 1, os:unmarshal_uint() do self.%s[os:marshal_%s()] = os:marshal_%s() end", var.name, var.key, var.value) end,
})

tc.treemap = tc.hashmap

tc.bean = merge(tc.binary, {
	default = function (var) return "{}" end,
})

tc.pvector = merge(tc.vector, {
})

tc.phashset = merge(tc.hashset, {
})

tc.phashmap = merge(tc.hashmap, {
})

dofile("../../src/fiber/tool/rpcalls.lua")

local function gen()
	local c = {}
	insert(c, [=[
local Bean = require("bean")
local Stream = require("stream")

local Types = {}
local insert = table.insert
local pairs = pairs
local ipairs = ipairs

local function tablelength(t)
	local c = 0 for _ in pairs(t) do
		c = c + 1
	end 
	return c 
end

]=])

	for name, bean in pairs(Types) do
		insert(c, string.format("Types.%s = %s", name, bean.type))
		if not bean.rpc then
			local svars = "\n"
			local smarshal = "\n"
			local sunmarshal = "\n"
			for _, var in ipairs(bean) do
				local ttype = typeclass[var.basetype]
				svars = svars .. string.format("\t%s = %s,\n", var.name, var.default)
				
				smarshal = smarshal .. string.format("\t%s\n", ttype.marshal(var, ttype) )
				sunmarshal = sunmarshal .. string.format("\t%s\n", ttype.unmarshal(var, ttype))
			end
	
		insert(c, string.format([=[
Bean.register(%s, 
{ 
	_type = %s,
	_name = "%s",
%s
	_marshal = function (self, os)
%s
	end,
	_unmarshal = function (self, os)
%s
	end,
	_process = function (bean, session) 
		Bean.process%s(bean, session)
	end,
})
function Stream:marshal_%s(x) x._type = %s; Stream:marshalbean(x) end
function Stream:unmarshal_%s() return Stream:unmarshalbean(%s) end
		  ]=], bean.type, bean.type, bean.name, svars, smarshal, sunmarshal, bean.name, bean.name, bean.type, bean.name, bean.type))
		end
	end
	
	insert(c, "return Types")

	return concat(c, "\n")
end

local function save(content, file)
	local f = io.open(file, "w")
	f:write(content)
	f:close()
end

save(gen(), "allbeans.lua")

bean = nil
rpc = nil
handler = nil
handlerset = nil

print("complete!")


