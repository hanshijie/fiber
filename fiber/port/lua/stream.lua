local floor = math.floor
local table = table
local char = string.char
local concat = table.concat
local insert = table.insert
local byte = string.byte
local assert = assert
local require = require
local error = error
local pairs = pairs
local ipairs = ipairs
local Bean = require("bean")
local setmetatable = setmetatable
local log = require("log")

local Stream = {}
Stream.__index = Stream


function Stream.new(data) 
	local o = { data = data or "", extra = {}, head = 0 }
	setmetatable(o, Stream)
	return o
end

function Stream:size()
	self:flush()
	return #self.data - self.head
end

function Stream:empty()
	return self.head == #self.data
end

function Stream:remain()
	return #self.data - self.head
end

function Stream:skip(n)
	self.head = self.head + n
end

function Stream:sub(n)
	return Stream.new(self.data:sub(self.head + 1, self.head + n))
end

-- put, get, drain 仅用于
-- 配合session.onRecv, io.onSend 使用
function Stream:put(data)
	--assert(#self.extra == 0)
	insert(self.extra, data)
end

function Stream:get()
	return self.data
end

function Stream:drain(size)
	assert(#self.extra == 0)
	if size < #self.data then
		self.data = self.data:sub(size + 1)
		return true
	else
		self.data = ""
		return false
	end
end

function Stream:flush()
	self.data = self.data .. concat(self.extra)
	self.extra = {}
end

function Stream:trunct()
	if self.head > 0 then
		if self:empty() then
			self.data = ""
		else
			self.data = self.data:sub(self.head + 1)
		end
		self.head = 0
	end
end

function Stream:marshal(x, n)
	--print("marshal:", x, n)
	if n == 1 then
		table.insert(self.extra, char(x % 256))
		return
	end
	local s = ""
	for i = 1, n do
		local c = x % 256
		--print("byte", c, x)
		s = char(c) .. s
		x = floor(x / 256)
	end
	insert(self.extra, s)
end

function Stream:unmarshal(n)
	local head = self.head
	if(head + n > #self.data) then 
		self:dump()
		error("unmarshal")
	end
	if n == 1 then
		head = head + 1
		self.head = head
		return byte(self.data, head)
	end
	local x = 0
	for i = 1, n do
		--print("unmarshal", i, x)
		head = head + 1
		x = x * 256 + byte(self.data, head)
	end
	self.head = head
	return x
end

function Stream:marshal_uint(x)
	if x < 0x80 then
		self:marshal(x > 0 and x or 0, 1)
	elseif x < 0x4000 then
	    self:marshal(x + 0x8000, 2)
	elseif x < 0x200000 then  
		self:marshal(x + 0xc00000, 3)
	elseif x < 0x10000000 then
		self:marshal(x + 0xe0000000, 4)
	else
		self:marshal(0xf0, 1)
		self:marshal(x, 4)
	end
end

function Stream:unmarshal_uint()
	local x = self:unmarshal(1)
	if x < 0x80 then
		return x
	elseif x < 0xc0 then
		return x % 0x40 * 0x100 + self:unmarshal(1)
	elseif x < 0xe0 then
		return x % 0x20 * 0x10000 + self:unmarshal(2)
	elseif x < 0xf0 then
		return x % 0x10 * 0x1000000 + self:unmarshal(3)
	else
		return self:unmarshal(4)
	end
end

function Stream:marshal_int(x)
	if x >= 0 then
	    if x < 0x40 then
	    	self:marshal(x, 1) 
	    elseif x < 0x2000 then
	        self:marshal(x + 0x4000, 2)
	    elseif x < 0x100000 then
	      	self:marshal(x + 0x600000, 3)
	    elseif x < 0x8000000 then
	     	self:marshal(x + 0x70000000, 4)
	    elseif x < 0x400000000 then
	    	self:marshal(x + 0x7800000000, 5)
	    elseif x < 0x20000000000 then
	        self:marshal(x + 0x7c0000000000, 6)
	    elseif x < 0x1000000000000 then 
	    	self:marshal(x + 0x7e000000000000, 7)
	    elseif x < 0x80000000000000 then
	     	self:marshal(x + 0x7f00000000000000, 8)
	    else
	    	self:marshal(0x7f, 1)
	    	self:marshal(x + 0x8000000000000000, 8)
	   	end
	else
		if x >= -0x40 then
			self:marshal(x + 0x100, 1)
		elseif x >= -0x2000 then
			self:marshal(x + 0xc000, 2)
		elseif x >= -0x100000 then
		    self:marshal(x + 0xa00000, 3)
		elseif x >= -0x8000000 then
			self:marshal(x + 0x9000000, 4)
		elseif x >= -0x400000000 then
			self:marshal(x + 0x880000000, 5)
		elseif x >= -0x20000000000 then
			self:marshal(x + 0x84000000000, 6)
		elseif x >= -0x1000000000000 then
			self:marshal(x + 0x100000000000, 7)
		elseif x >= -0x80000000000000 then
			self:marshal(x + 0x1000000000000, 8)
		else
			self:marshal(0x80, 1)
			self:marshal(x, 8)
		end
	end
end

function Stream:unmarshal_int()
	local v = self:unmarshal(1)
	if v <  0x40 or v >= 0xc0 then v = v < 0x80 and v or v - 0x100
	elseif v <= 0x5f then v = (v - 0x40) * 0x100         + self:unmarshal(1)
	elseif v >= 0xa0 then v = (v + 0x40) * 0x100         + self:unmarshal (1) - 0x10000
	elseif v <= 0x6f then v = (v - 0x60) * 0x10000       + self:unmarshal(2)
	elseif v >= 0x90 then v = (v + 0x60) * 0x10000       + self:unmarshal(2) - 0x1000000
	elseif v <= 0x77 then v = (v - 0x70) * 0x1000000     + self:unmarshal(3)
	elseif v >= 0x88 then v = (v + 0x70) * 0x1000000     + self:unmarshal(3) - 0x100000000
	elseif v <= 0x7b then v = (v - 0x78) * 0x100000000   + self:unmarshal(4)
	elseif v >= 0x84 then v = (v + 0x78) * 0x100000000   + self:unmarshal(4) - 0x10000000000
	elseif v <= 0x7d then v = (v - 0x7c) * 0x10000000000 + self:unmarshal(5)
	elseif v >= 0x82 then v = (v + 0x7c) * 0x10000000000 + self:unmarshal(5) - 0x1000000000000
	elseif v == 0x7e then v = self:unmarshal(6)
	elseif v == 0x81 then v = self:unmarshal(6) - 0x1000000000000
	end
	return v
end

function Stream:marshal_bool(x)
	self:marshal_int((x and x ~= 0) and 1 or 0)
end

function Stream:unmarshal_bool()
	return self:marshal_int() ~= 0 and true or false
end

function Stream:marshal_byte(x)
	self:marshal_int(x)
end

function Stream:unmarshal_byte()
	return self:unmarshal_int()
end

function Stream:marshal_short(x)
	self:marshal_int(x)
end

function Stream:unmarshal_short()
	return self:unmarshal_int()
end

function Stream:marshal_long(x)
	self:marshal_int(x)
end

function Stream:unmarshal_long()
	return self:unmarshal_int()
end

function Stream:marshal_float(x)
	self:marshal(x, 4)
end

function Stream:unmarshal_float()
	return self:unmarshal(4)
end

function Stream:marshal_double(x)
	self:marshal(x, 8)
end

function Stream:unmarshal_double()
	return self:unmarshal(8)
end

function Stream:marshal_string(x)
	self:marshal_binary(x)
end

function Stream:unmarshal_string()
	return self:marshal_binary()
end

function Stream:marshal_binary(x)
	self:marshal_uint(#x)
	self:put(x)
end

function Stream:unmarshal_binary()
	local size = self:unmarshal_uint()
	if self:remain() < size then
		error("unmarshal fail")
	end
	local head = self.head
	self.head  = head + size
	return self.data:sub(head + 1, head + size)
end

function Stream:unmarshalbean(type)
	return Bean.unmarshal(self, type)
end

function Stream:marshalbean(bean)
	local btype = bean._type
	assert(btype)
	Bean.marshal(self, btype, bean)
end

function Stream:dump()
	log.print("data:", #self.data)
	log.print("head:", self.head)
	log.print("extra:", #concat(self.extra))
end

local function test()
	local o = Stream.create()
	for _, v in ipairs({0, 1, 40, 0x80, 0x1000, 0x4000, 0x50000, 0x200000, 0x10000000, 0x70000000}) do
		o:marshal_uint(v)
		o:flush()
		local x = o:unmarshal_uint()
		if x ~= v then
			log.print("###marshal", v, x)
		end
	end
		for _, v in ipairs({0, 1, 40, 0x80, 0x1000, 0x4000, 0x50000, 0x200000, 0x10000000, 0x70000000, 0x1000000000, 0x10000000000}) do
		log.print("=========", v)
		o:marshal_int(v)
		o:flush()
		local x = o:unmarshal_int()
		if x ~= v then
			log.print("###marshal", v, x)
		end
	end
end

return Stream