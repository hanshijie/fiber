local log = require "log"
local assert = assert
local ipairs = ipairs
local pairs = pairs
local type = type
local setmetatable = setmetatable
local error = error
local pcall = pcall

local Bean = {}

local stub = {
	--[[
		-- bean example.
		1  = {
			_type = 1,
			_name = "Response",
			_marshal = function (self, os)   end,
			_unmarshal = function(self, os)  end,
			_process = function(self, session) end,
			
			-- for rpc
			_argtypename =  ,
			_restypename =  ,
		},
	
	--]]
	
}

local function check_field(obj, base)
	for _, v in ipairs({"_name", "_marshal", "_unmarshal"}) do
		if obj[v] and obj[v] ~= base[v] then
			log.err("can't override meta filed:%s", v)
			error("invalid filed:" .. v)
		end
	end

	for k, v in pairs(obj) do
		if base[k] == nil and type(k) == "string" and k:sub(1, 1) ~= "_" then
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
	for k, v in pairs(base) do
		if type(v) == "table" and not o[k] then
			o[k] = {}
		end
	end
	setmetatable(o, base)
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

local RPCID = 0

local rpcs = {}
function Bean.addwaitreplyrpc(rpc, timeout, handler)
	rpc._replytimeout = timeout
	rpc._handler = handler
	rpcs[rpc._rpcid] = rpc
end

function Bean.checkrpctimeout(now)
	for id, rpc in pairs(rpcs) do
		if rpc._replytimeout <= now then
            rpcs[id] = nil
            local handlername = "onTimeout" .. rpc._name
            local handler = Bean[handlername]
            if not handler then
              log.err("rpc Bean.%s undefine", handlername)
            else
                ret, err = pcall(handler, rpc.arg, rpc._session)
                if not ret then
                    log.err("Bean.%s process fail. err:%s", handlername, err)
                end
            end
		end
	end
end

function Bean.allocrpcid()
	RPCID = RPCID + 1
	return RPCID
end

function Bean.createrpc(type, obj)
	return Bean.create(type, obj)
end

local function marshal_rpc(rpc, os)
	local rpcid = rpc._rpcid
	assert(rpcid > 0)
	--log.print("rpcid:", rpcid)
	os:marshal_int(rpcid)
	Bean.marshal(os, rpc._argtype, rpc.arg)
end

local function unmarshal_rpc(rpc, os)
	local rpcid = os:unmarshal_int()
	if rpcid > 0 then 
		log.err("lua client don't support rpc server. rpcid:%s", rpcid)
		return
	end
	rpcid = rpcid + 0x80000000
	if rpcid <= 0 then
		log.err("invalid rpcid:%s", rpcid)
		return
	end
	rpc.res = Bean.unmarshal(os, rpc._restype)
    rpc._rpcid = rpcid
	return rpc
end

local function process_rpc(rpc, session)
	local rpcid = rpc._rpcid
	local oldrpc = rpcs[rpcid]
	if not oldrpc then
        log.err("process_rpc. invalid rpcid:%s", rpcid)
        return
    end
    rpcs[rpcid] = nil
	if rpc._type ~= oldrpc._type then
		log.err("rpc id:%s type unmatch. sendtype:%s receivetype:%s", rpcid, oldrpc._type, rpc._type)
		return
	end
    local handlername = "onClient" .. rpc._name
	local handler = oldrpc._handler or Bean[handlername]
    if not handler then
        log.err("rpc. Bean.%s undefine", handlername)
        return
    end
	handler(oldrpc.arg, rpc.res, oldrpc._session)
end

function Bean.registerrpc(id, name, argtype, restype, timeout)
	Bean.register(id, {
		_type = id,
		_name = name,
		_marshal = marshal_rpc,
		_unmarshal = unmarshal_rpc,
		_process = process_rpc,
		_ontimeout = function (self, session)  Bean["onTimeout" .. name](self, session) end,
		_argtype = argtype,
		_restype = restype,
		_timeout = timeout,
		
		_rpcid = 0,
		_session = nil,
		_handler = nil,
		arg = {},
		res = {},	
	}
)
end

return Bean
