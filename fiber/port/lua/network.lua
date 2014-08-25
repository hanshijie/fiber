local socket = require(socketlib or "socket")
local log = require("log")
local Stream = require("Stream")

local ipairs = ipairs
local pairs = pairs
local table = table
local type = type
local insert = table.insert
local setmetatable = setmetatable
local pcall = pcall

local Network = {}


local TIMERID = 1
local timers = {}  -- key:id     value = { timeout, function }
local deltimers = {} -- {id, id, ..}

function Network.addtimer(delay, fun, obj)
	TIMERID = TIMERID + 1
	local id = TIMERID
	timers[id] = { timeout = socket.gettime() + delay, func = fun, obj = obj}
	return id
end

function Network.deltimer(id)
	table.insert(deltimers, id)
end

function Network.sleep(time)
	socket.sleep(time)
end

function Network.gettime()
	return socket.gettime()
end

local function polltimer()
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

local recvt = {}
local sendt = {}

local recvt_change = {}  		-- recvt 的变动表
local sendt_change = {} 		-- sendt 的变动表

local sessions = {}

local OPER_ADD = 1
local OPER_DEL = 2

local function get_keys(t)
	local n = {}
	for k, _ in pairs(t) do
		insert(n, k)
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

local function register(s, recv, send)
	if recv == true then
		recvt_change[s] = OPER_ADD
	elseif recv == false then
		recvt_change[s] = OPER_DEL
	end
	
	if send == true then
		sendt_change[s] = OPER_ADD
	elseif send == false then
		sendt_change[s] = OPER_DEL
	end
end

local function connect(addr, port, timeout)
	local sock = socket.tcp()
	sock:settimeout(timeout or 0)
	sock:connect(addr, port)
	return sock
end

function Network.poll(timeout)
	polltimer()
	recvt = do_change(recvt, recvt_change)
	sendt = do_change(sendt, sendt_change)
	local recv_sockets, send_sockets, err = socket.select(recvt, sendt, timeout)
	if err then return end
	log.log("poll. socket active! recvs:%d sends:%d", #recv_sockets, #send_sockets)
	for _, s in ipairs(recv_sockets) do
		local session = sessions[s]
		if session then
			session:onrecv()
		end
	end
	
	for _, s in ipairs(send_sockets) do
		local session = sessions[s]
		if session then
			session:onsend()
		end	
	end
end



local SID = 0
local Session = {}
Session.__index = Session

local STATUS_CONNECTED = 1
local STATUS_FAIL = 2
local STATUS_CLOSED = 3
local STATUS_CONNECTING = 4


local BACKOFF_INIT = 1
local BACKOFF_MAX = 64

local CONNECT_TIMEOUT = CONNECT_TIMEOUT or 30

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
	self.socket = connect(addr, port)
	self.recv_buffer = Stream.create()
	self.send_buffer = Stream.create()
	self.out_filter = nil
	self.in_filter = nil
	self.addr = addr
	self.port = port
	self.backoff = self.backoff or BACKOFF_INIT
	sessions[self.socket] = self
	Network.addtimer(CONNECT_TIMEOUT, function (s)
			if s.status == STATUS_CONNECTING then
				s:abort()
			end
		end, self)
	self:permitsend()
end

function Session:setinfilter(filter)
	self.in_filter = filter
end

function Session:setoutfilter(filter)
	self.out_filter = filter
end

function Session:onrecv()
	if self.closed then return end
	local read, err, partial = self.socket:receive()
	if err == "closed" then
		log.err(err)
		self:close()
		return
	else
		read = read or partial
		if not read then
			log.log("read nothing. next round")
			return
		end
	end
	log.log("read bytes:" .. #read)
	self.recv_buffer:put(self.in_filter and self.in_filter(read) or read)
	self:decodeprotocol()
end

function Session:onsend()
	log.log("onwrite")
	if self.closed then return end
	if self.status == STATUS_CONNECTING then
		if not self.socket:getpeername() then
			log.log("connect fail!")
			self.status = STATUS_FAIL
			self:abort()
		else
			self.status = STATUS_CONNECTED
			self:connect()
		end
		return
	end
	self.send_buffer:flush()
	local write, err = self.socket:send(self.send_buffer:get())
	if not write then
		log.log(err)
		self:close()
		return
	end
	log.log("write bytes:" .. write)
	if not self.send_buffer:drain(write) then
		self:forbidsend()
	end
end

local DECODE_SUCC = 0
local DECODE_NOENOUGH = 1
local DECODE_ERROR = 2
function Session:decodeprotocol()
	local os = self.recv_buffer
	os:flush()
	local head = os.head
	while not os:empty() do
		local ret, status, b = pcall(function ()  
			--os:dump()
			local type = os:unmarshal_uint()
			local size = os:unmarshal_uint()
			log.log("decodeProtocol. type:%d size:%d", type, size)
			if os:remain() < size then return DECODE_NOENOUGH end
			local  bos = os:sub(size)
			local bret, bean = pcall(bos.unmarshalbean, bos, type)
			if not bret or not bean then
				log.err("unmarshal bean<%d> fail. msg:%s", type, bean)
				return DECODE_ERROR
			end
			os:skip(size)
			return DECODE_SUCC, bean
		end
		)
		if ret then
			head = os.head
			if status == DECODE_ERROR then
				log.log("decode error. close session-%d", self.id)
				self:close()
				return
			elseif status == DECODE_SUCC then
				local ret, err = pcall(b._process, b, self)
				if not ret then
					log.err("process <%s> fail! %s", handler, err)
				end
			end
		else
			log.err("decodeProcotols. err msg:%s", status)
			os.head = head
			break
		end
	end
	os:trunct()
end

function Session:permitrecv()
	if self.closed then return end
	register(self.socket, true)
end

function Session:forbidrecv()
	if self.closed then return end
	register(self.socket, false)
end

function Session:permitsend()
	if self.closed then return end
	register(self.socket, nil, true)
end

function Session:forbidsend()
	if self.closed then return end
	register(self.socket, nil, false)
end

function Session:write(data)
	if type(data) == "string" then
		self.send_buffer:put(self.out_filter and self.out_filter(data) or data)
		self:permitsend()
	elseif type(data) == "table" then
		self:permitsend()
		local os = Stream.create()
		os:marshalbean(data)
		local out = self.send_buffer
		out:marshal_uint(data._type)
		out:marshal_uint(os:size())
		out:put(os:get())	
	else
		
	end
end

function Session:connect()
	self.backoff = BACKOFF_INIT
	self:permitrecv()
	self:forbidsend()
	self:onconnect()
end

function Session:onconnect()
	log.log("onconnect " .. self.id)	
	--self:write("xxabcdefg")
end

function Session:onclose()
	log.log("onclose " .. self.id)
	self:tryreconnect()
end

function Session:close()
	if self.closed then return end
	self.closed = true
	self.status = STATUS_CLOSED
	self:forbidrecv()
	self:forbidsend()
	sessions[self.socket] = nil
	self.socket:close()
	self:onclose()
end

function Session:abort()
	if self.closed then return end
	self.closed = true
	self.status = STATUS_FAIL
	self:forbidrecv()
	self:forbidsend()
	sessions[self.socket] = nil
	self.socket:close()
	self:onabort()
end

function Session:onabort()
	log.log("[session-%d] abort", self.id)
	self:tryreconnect()
end

function Session:tryreconnect()
	if not self.enablereconnect then
		log.log("[session-%d] don't reconnect", self.id)
		return
	end
	Network.addtimer(self.backoff, function (session) session:reconnect()  end, self)
	self.backoff = self.backoff * 2
	if self.backoff > BACKOFF_MAX then
		self.backoff = BACKOFF_MAX
	end
end

function Session:reconnect()
	log.log("[session-%d] reconnect", self.id)
	self:init(self.addr, self.port)
end

function Network.client(addr, port, reconnect)
	return Session.create(addr, port, reconnect)
end

require "allbeans"

return Network
