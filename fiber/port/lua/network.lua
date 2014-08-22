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

nw.recvt_change = {}  		-- recvt 的变动表
nw.sendt_change = {} 		-- sendt 的变动表

nw.sessions = {}

local TIMERID = 1
local timers = {}  -- key:id     value = { timeout, function }
local deltimers = {} -- {id, id, ..}

local OPER_ADD = 1
local OPER_DEL = 2

local function get_keys(t)
	local n = {}
	for k, _ in pairs(t) do
		table.insert(n, k)
	end
	return n
end

local function do_change(t, c)
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
	return get_keys(n)
end

function nw.register(s, recv, send)
	if recv == true then
		nw.recvt_change[s] = OPER_ADD
	elseif recv == false then
		nw.recvt_change[s] = OPER_DEL
	end
	
	if send == true then
		nw.sendt_change[s] = OPER_ADD
	elseif send == false then
		nw.sendt_change[s] = OPER_DEL
	end
end

function nw.poll(timeout)
	nw.polltimer()
	nw.recvt = do_change(nw.recvt, nw.recvt_change)
	nw.sendt = do_change(nw.sendt, nw.sendt_change)
	local recv_sockets, send_sockets, err = socket.select(nw.recvt, nw.sendt, timeout)
	if err then return end
	log.log("poll. socket active! recvs:%d sends:%d", #recv_sockets, #send_sockets)
	for _, s in ipairs(recv_sockets) do
		local session = nw.sessions[s]
		if session then
			session:onrecv()
		end
	end
	
	for _, s in ipairs(send_sockets) do
		local session = nw.sessions[s]
		if session then
			session:onsend()
		end	
	end
end

function nw.connect(addr, port, timeout)
	local s = socket.tcp()
	s:settimeout(timeout or 0)
	s:connect(addr, port)
	return s
end

function nw.addtimer(delay, fun, obj)
	TIMERID = TIMERID + 1
	local id = TIMERID
	timers[id] = { timeout = socket.gettime() + delay, func = fun, obj = obj}
	return id
end

function nw.deltimer(id)
	table.insert(deltimers, id)
end

function nw.sleep(time)
	socket.sleep(time)
end

function nw.gettime()
	return socket.gettime()
end

function nw.polltimer()
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
