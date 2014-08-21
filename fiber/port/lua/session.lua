local socket = require(socketlib or "socket")
local log = require("log")
local Stream = require("stream")

local ipairs = ipairs
local pairs = pairs
local table = table

local SID = 0
local Session = {}
Session.__index = Session

local STATUS_CONNECTED = 1
local STATUS_FAIL = 2
local STATUS_CLOSED = 3
local STATUS_CONNECTING = 4


local BACKOFF_INIT = 1
local BACKOFF_MAX = 32

local CONNECT_TIMEOUT = CONNECT_TIMEOUT or 3

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
	self.socket = NetWork.connect(addr, port)
	self.recvBuf = Stream.create()
	self.sendBuf = Stream.create()
	self.outFilter = nil
	self.inFilter = nil
	self.addr = addr
	self.port = port
	self.backoff = self.backoff or BACKOFF_INIT
	NetWork.sessions[self.socket] = self
	NetWork.addTimer(CONNECT_TIMEOUT, function (s)
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
	log.log("onread")
	if self.closed then return end
	local read, err = self.socket:receive(64 * 1024)
	if not read then
		log.log(err)
		self:close()
		return
	end
	log.log("read:" .. read .. "bytes:" .. #read)
	self.recvBuf:put(self.inFilter and self.inFilter(read) or read)
end

function Session:onSend()
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
	local write, err = self.socket:send(self.sendBuf:get())
	if not write then
		log.log(err)
		self:close()
		return
	end
	log.log("write bytes:" .. write)
	if not self.sendBuf:drain(write) then
		self:forbidSend()
	end
end

function Session:permitRecv()
	if self.closed then return end
	NetWork.register(self.socket, true)
end

function Session:forbidRecv()
	if self.closed then return end
	NetWork.register(self.socket, false)
end

function Session:permitSend()
	if self.closed then return end
	NetWork.register(self.socket, nil, true)
end

function Session:forbidSend()
	if self.closed then return end
	NetWork.register(self.socket, nil, false)
end

function Session:write(data)
	if type(data) == "string" then
		self.sendBuf:put(self.outFilter and self.outFilter(data) or data)
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
	log.log("onConnect " .. self.id)	
	self:write("xxabcdefg")
end

function Session:onClose()
	log.log("onClose " .. self.id)
	self:tryReconnect()
end

function Session:close()
	if self.closed then return end
	self.closed = true
	self.status = STATUS_CLOSED
	self:forbidRecv()
	self:forbidSend()
	NetWork.sessions[self.socket] = nil
	self.socket:close()
	self:onClose()
end

function Session:abort()
	if self.closed then return end
	self.closed = true
	self.status = STATUS_FAIL
	self:forbidRecv()
	self:forbidSend()
	NetWork.sessions[self.socket] = nil
	self.socket:close()
	self:onAbort()
end

function Session:onAbort()
	log.log("[session-%d] abort", self.id)
	self:tryReconnect()
end

function Session:tryReconnect()
	if not self.enablereconnect then
		log.log("[session-%d] don't reconnect", self.id)
		return
	end
	NetWork.addTimer(self.backoff, function (session) session:reconnect()  end, self)
	self.backoff = self.backoff * 2
	if self.backoff > BACKOFF_MAX then
		self.backoff = BACKOFF_MAX
	end
end

function Session:reconnect()
	log.log("[session-%d] reconnect", self.id)
	self:init(self.addr, self.port)
end

return Session