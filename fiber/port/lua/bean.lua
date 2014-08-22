local log = require "log"
local assert = assert

local Bean = { }
  
local B = Bean
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

function B.create(type, obj)
	local o = obj or {}
	local base = stub[type]
	if not base then
		log.err("bean type<%d> not exist", type)
		return nil 
	end
	check_field(o, base)
	base.__index = base
	setmetatable(o, base)
	return o
end

function B.marshal(stream, type, obj)
	local o = B.create(type, obj)
	if not o then return nil end
	o:_marshal(stream)
end

function B.unmarshal(stream, type)
	local o = B.create(type)
	if not o then return nil end
	o:_unmarshal(stream)
	return o
end

function B.register(id, bean)
	assert(id and (not stub[id]) and bean)
	assert(id == bean._type)
	assert(bean._name)
	assert(type(bean._marshal) == "function")
	assert(type(bean._unmarshal) == "function")
	stub[id] = bean
end


return Bean