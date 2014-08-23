local log = require "log"
local assert = assert
local ipairs = ipairs
local pairs = pairs
local type = type

Bean = { }
local Bean = Bean

local stub = {
	--[[
		-- bean example.
		1  = {
			_type = 1,
			_name = "Response",
			_marshal = function (os)   end,
			_unmarshal = function(os)  end,
		},
	
	--]]
	
}

local function check_field(obj, base)
	for _, v in ipairs({"_name", "_marshal", "_unmarshal"}) do
		if obj[v] then
			log.err("can't override meta filed:%s", v)
			error("invalid filed:" .. v)
		end
	end
	for k, v in pairs(obj) do
		if not base[k] then
			log.err("bean<%s> hasn't field<%s>", base._name, k)
			error("invalid filed: [" .. k .. "] = " .. v)
		end
	end
end

function Bean.create(btype, obj)
	local o = obj or {}
	local base = stub[btype]
	if not base then
		log.err("bean type<%d> not exist", btype)
		return nil 
	end
	check_field(o, base)
	base.__index = base
	setmetatable(o, base)
	for k, v in pairs(base) do
		if type(v) == "table" then
			o.k = {}
		end
	end
	return o
end

function Bean.marshal(stream, type, obj)
	local o = Bean.create(type, obj)
	if not o then return nil end
	o:_marshal(stream)
end

function Bean.unmarshal(stream, type)
	local o = Bean.create(type)
	if not o then return nil end
	o:_unmarshal(stream)
	return o
end

function Bean.register(id, bean)
	assert(id and (not stub[id]) and bean)
	assert(id == bean._type)
	assert(bean._name)
	assert(type(bean._marshal) == "function")
	assert(type(bean._unmarshal) == "function")
	stub[id] = bean
end


return Bean