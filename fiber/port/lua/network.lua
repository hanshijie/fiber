local socket = require(socketlib or "socket")
local log = require("log")
local Session = require("session")

local ipairs = ipairs
local pairs = pairs
local table = table



NetWork = {}
local nw = NetWork

nw.recvt = {}
nw.sendt = {}

nw.recvtChange = {}  		-- recvt 的变动表
nw.sendtChange = {} 		-- sendt 的变动表

nw.sessions = {}

local TIMERID = 1
local timers = {}  -- key:id     value = { timeout, function }
local deltimers = {} -- {id, id, ..}

local OPER_ADD = 1
local OPER_DEL = 2

local function getKeys(t)
	local n = {}
	for k, _ in pairs(t) do
		table.insert(n, k)
	end
	return n
end

local function doChange(t, c)
	local n = {}
	for _, v in ipairs(t) do
		n[v] = true
	end
	for k, v in pairs(c) do
		if v == OPER_ADD then
			n[k] = true
		elseif v == OPER_DEL then
			n[k] = nil
		end
	end
	return getKeys(n)
end

function nw.register(s, recv, send)
	if recv == true then
		nw.recvtChange[s] = OPER_ADD
	elseif recv == false then
		nw.recvtChange[s] = OPER_DEL
	end
	
	if send == true then
		nw.sendtChange[s] = OPER_ADD
	elseif send == false then
		nw.sendtChange[s] = OPER_DEL
	end
end

function nw.poll(timeout)
	nw.doTimer()
	nw.recvt = doChange(nw.recvt, nw.recvtChange)
	nw.sendt = doChange(nw.sendt, nw.sendtChange)
	local recvSockets, sendSockets, err = socket.select(nw.recvt, nw.sendt, timeout)
	if err then return end
	log.log("poll. socket active!")
	for _, s in ipairs(recvSockets) do
		local session = nw.sessions[s]
		if session then
			session:onRecv()
		end
	end
	
	for _, s in ipairs(sendSockets) do
		local session = nw.sessions[s]
		if session then
			session:onSend()
		end	
	end
end

function nw.connect(addr, port, timeout)
	local s = socket.tcp()
	s:settimeout(timeout or 0)
	print(s:connect(addr, port))
	return s
end

function nw.addTimer(delay, fun, obj)
	TIMERID = TIMERID + 1
	local id = TIMERID
	timers[id] = { timeout = socket.gettime() + delay, func = fun, obj = obj}
	return id
end

function nw.delTimer(id)
	table.insert(deltimers, id)
end

function nw.sleep(time)
	socket.sleep(time)
end

function nw.gettime()
	return socket.gettime()
end

function nw.doTimer()
	for _, id in ipairs(deltimers) do
		timers[id] = nil
	end
	local now = socket.gettime()
	for id, t in pairs(timers) do
		if t.timeout <= now then
			t.func(t.obj)
			table.insert(deltimers, id)
		end
	end
end

function nw.client(addr, port, reconnect)
	local session = Session.create(addr, port, reconnect)
	return session
end

return NetWork
