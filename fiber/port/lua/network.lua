local socketlib = socketlib or "socket"
local socket = require(socketlib)
local ipairs = ipairs
local pairs = pairs
local table = table

local function log(...)
	print(string.format(...))
end

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

local STATUS_CONNECTED = 1
local STATUS_FAIL = 2
local STATUS_CLOSED = 3
local STATUS_CONNECTING = 4

local CONNECT_TIMEOUT = CONNECT_TIMEOUT or 3

local BACKOFF_INIT = 1
local BACKOFF_MAX = 32

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
	log("poll. socket active!")
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

local SID = 0
local Session = {}
Session.__index = Session

function Session.create(addr, port, reconnect)
	local o = {}
	setmetatable(o, Session)
	o.enablereconnect = reconnect
	o:init(addr, port)
	return o
end

function Session:init(addr, port)
	SID = SID + 1
	self.id = SID
	self.closed = false
	self.status = STATUS_CONNECTING
	self.socket = nw.connect(addr, port)
	self.recvBuf = ""
	self.sendBuf = ""
	self.outFilter = nil
	self.inFilter = nil
	self.addr = addr
	self.port = port
	self.backoff = self.backoff or BACKOFF_INIT
	nw.sessions[self.socket] = self
	nw.addTimer(CONNECT_TIMEOUT, function (s)
			if s.status == STATUS_CONNECTING then
				s:abort()
			end
		end, self)
	self:permitSend()
end

function Session:setInFilter(filter)
	self.inFilter = filter
end

function Session:setOutFilter(filter)
	self.outFilter = filter
end

function Session:onRecv()
	log("onread")
	if self.closed then return end
	local read, err = self.socket:receive(64 * 1024)
	if not read then
		log(err)
		self:close()
		return
	end
	log("read:" .. read .. "bytes:" .. #read)
	self.recvBuf = self.recvBuf .. (self.inFilter and self.inFilter(read) or read)
end

function Session:onSend()
	log("onwrite")
	if self.closed then return end
	if self.status == STATUS_CONNECTING then
		if not self.socket:getpeername() then
			log("connect fail!")
			self.status = STATUS_FAIL
			self:abort()
		else
			self.status = STATUS_CONNECTED
			self:connect()
		end
		return
	end
	local write, err = self.socket:send(self.sendBuf)
	if not write then
		log(err)
		self:close()
		return
	end
	log("write bytes:" .. write)
	if write < #self.sendBuf then
		self.sendBuf = self.sendBuf:sub(write + 1)
	else
		self.sendBuf = ""
		self:forbidSend()
	end
end

function Session:permitRecv()
	if self.closed then return end
	nw.register(self.socket, true)
end

function Session:forbidRecv()
	if self.closed then return end
	nw.register(self.socket, false)
end

function Session:permitSend()
	if self.closed then return end
	nw.register(self.socket, nil, true)
end

function Session:forbidSend()
	if self.closed then return end
	nw.register(self.socket, nil, false)
end

function Session:write(data)
	if type(data) == "string" then
		self.sendBuf = self.sendBuf .. (self.outFilter and self.outFilter(data) or data)
		self:permitSend()
	elseif type(data) == "table" then
	
		self:permitSend()
	else
		
	end
end

function Session:connect()
	self.backoff = BACKOFF_INIT
	self:permitRecv()
	self:forbidSend()
	self:onConnect()
end

function Session:onConnect()
	log("onConnect " .. self.id)	
	self:write("xxabcdefg")
end

function Session:onClose()
	log("onClose " .. self.id)
	self:tryReconnect()
end

function Session:close()
	if self.closed then return end
	self.closed = true
	self.status = STATUS_CLOSED
	self:forbidRecv()
	self:forbidSend()
	nw.sessions[self.socket] = nil
	self.socket:close()
	self:onClose()
end

function Session:abort()
	if self.closed then return end
	self.closed = true
	self.status = STATUS_FAIL
	self:forbidRecv()
	self:forbidSend()
	nw.sessions[self.socket] = nil
	self.socket:close()
	self:onAbort()
end

function Session:onAbort()
	log("[session-%d] abort", self.id)
	self:tryReconnect()
end

function Session:tryReconnect()
	if not self.enablereconnect then
		log("[session-%d] don't reconnect", self.id)
		return
	end
	nw.addTimer(self.backoff, function (session) session:reconnect()  end, self)
	self.backoff = self.backoff * 2
	if self.backoff > BACKOFF_MAX then
		self.backoff = BACKOFF_MAX
	end
end

function Session:reconnect()
	log("[session-%d] reconnect", self.id)
	self:init(self.addr, self.port)
end

function nw.client(addr, port, reconnect)
	local session = Session.create(addr, port, reconnect)
	return session
end

local client = nw.client("127.0.0.1", 1314, true)
while true do
	socket.sleep(1)
	nw.poll(0)
	log("round...")
end



return NetWork
