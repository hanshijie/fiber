local Stream = {}
Stream.__index = Stream


function Stream.create(data) 
	local o = { data = data or "" }
	setmetatable(o, Stream)
	return o
end

function Stream:put(data)
	self.data = self.data .. data
end

function Stream:get()
	return self.data
end

function Stream:drain(size)
	if size < #self.data then
		self.data = self.data:sub(size + 1)
		return true
	else
		self.data = ""
		return false
	end
end


function Stream:marshal_uint(x)
	if x < 0x80 then
		marshal1(x > 0 and x or 0)
	elseif x < 0x4000 then
	    marshal2(x + 0x8000)
	elseif x < 0x200000 then  
		marshal3(x + 0xc00000)
	elseif x < 0x1000000 then
		marshal4(x + 0xe0000000)
	else
		marshal5(0xf0, x)
	end
end

function Stream:unmarshal_uint(x)

end


function Stream:unmarshalBean(type)
	return {}
end

function Stream.marshalBean(bean)

end

return Stream