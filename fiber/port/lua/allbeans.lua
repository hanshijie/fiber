local Bean = require("bean")
local Stream = require("stream")

local Types = {}
local insert = table.insert
local pairs = pairs
local ipairs = ipairs

local function tablelength(t)
	local c = 0 for _ in pairs(t) do
		c = c + 1
	end 
	return c 
end


Types.Hello = 4
Types.HelloRes = 42
Bean.register(42, 
{ 
	_type = 42,
	_name = "HelloRes",

	v1 = false,
	v3 = 0,

	_marshal = function (self, os)

	os:marshal_bool(self.v1)
	os:marshal_short(self.v3)

	end,
	_unmarshal = function (self, os)

	self.v1 = os:unmarshal_bool()
	self.v3 = os:unmarshal_short()

	end,
	_process = function (bean, session) 
		Bean.processHelloRes(bean, session)
	end,
})
function Stream:marshal_HelloRes(x) x._type = 42; Stream:marshalbean(x) end
function Stream:unmarshal_HelloRes() return Stream:unmarshalbean(42) end
		  
Types.HelloArg = 41
Bean.register(41, 
{ 
	_type = 41,
	_name = "HelloArg",

	v1 = false,

	_marshal = function (self, os)

	os:marshal_bool(self.v1)

	end,
	_unmarshal = function (self, os)

	self.v1 = os:unmarshal_bool()

	end,
	_process = function (bean, session) 
		Bean.processHelloArg(bean, session)
	end,
})
function Stream:marshal_HelloArg(x) x._type = 41; Stream:marshalbean(x) end
function Stream:unmarshal_HelloArg() return Stream:unmarshalbean(41) end
		  
Types.UserLoginRe = 6
Bean.register(6, 
{ 
	_type = 6,
	_name = "UserLoginRe",

	retcode = 0,
	time = 0,
	roleids = {},

	_marshal = function (self, os)

	os:marshal_int(self.retcode)
	os:marshal_int(self.time)
	os:marshal_uint(#self.roleids); for _, v in ipairs(self.roleids) do os:marshal_long(v) end

	end,
	_unmarshal = function (self, os)

	self.retcode = os:unmarshal_int()
	self.time = os:unmarshal_int()
	for i = 1, os:unmarshal_uint() do insert(self.roleids, os:unmarshal_long()) end

	end,
	_process = function (bean, session) 
		Bean.processUserLoginRe(bean, session)
	end,
})
function Stream:marshal_UserLoginRe(x) x._type = 6; Stream:marshalbean(x) end
function Stream:unmarshal_UserLoginRe() return Stream:unmarshalbean(6) end
		  
Types.UserLogin = 5
Bean.register(5, 
{ 
	_type = 5,
	_name = "UserLogin",

	uid = 0,
	auth = "",

	_marshal = function (self, os)

	os:marshal_int(self.uid)
	os:marshal_binary(self.auth)

	end,
	_unmarshal = function (self, os)

	self.uid = os:unmarshal_int()
	self.auth = os:unmarshal_binary()

	end,
	_process = function (bean, session) 
		Bean.processUserLogin(bean, session)
	end,
})
function Stream:marshal_UserLogin(x) x._type = 5; Stream:marshalbean(x) end
function Stream:unmarshal_UserLogin() return Stream:unmarshalbean(5) end
		  
Types.SessionInfo = 7
Bean.register(7, 
{ 
	_type = 7,
	_name = "SessionInfo",

	uid = 0,
	logintime = 0,
	roleids = {},

	_marshal = function (self, os)

	os:marshal_int(self.uid)
	os:marshal_int(self.logintime)
	os:marshal_uint(#self.roleids); for _, v in ipairs(self.roleids) do os:marshal_int(v) end

	end,
	_unmarshal = function (self, os)

	self.uid = os:unmarshal_int()
	self.logintime = os:unmarshal_int()
	for i = 1, os:unmarshal_uint() do insert(self.roleids, os:unmarshal_int()) end

	end,
	_process = function (bean, session) 
		Bean.processSessionInfo(bean, session)
	end,
})
function Stream:marshal_SessionInfo(x) x._type = 7; Stream:marshalbean(x) end
function Stream:unmarshal_SessionInfo() return Stream:unmarshalbean(7) end
		  
Types.TestBean = 2
Bean.register(2, 
{ 
	_type = 2,
	_name = "TestBean",

	v1 = false,

	_marshal = function (self, os)

	os:marshal_bool(self.v1)

	end,
	_unmarshal = function (self, os)

	self.v1 = os:unmarshal_bool()

	end,
	_process = function (bean, session) 
		Bean.processTestBean(bean, session)
	end,
})
function Stream:marshal_TestBean(x) x._type = 2; Stream:marshalbean(x) end
function Stream:unmarshal_TestBean() return Stream:unmarshalbean(2) end
		  
Types.TestType = 3
Bean.register(3, 
{ 
	_type = 3,
	_name = "TestType",

	v1 = false,
	v2 = 0,
	v3 = 0,
	v4 = 0,
	v5 = 0,
	v6 = {},
	v7 = {},
	v8 = "",
	v9 = "",
	v10 = {},
	v13 = {},
	v14 = {},
	v16 = {},
	v17 = {},
	v19 = {},
	v20 = {},
	v23 = {},
	v26 = {},

	_marshal = function (self, os)

	os:marshal_bool(self.v1)
	os:marshal_byte(self.v2)
	os:marshal_short(self.v3)
	os:marshal_int(self.v4)
	os:marshal_long(self.v5)
	os:marshal_float(self.v6)
	os:marshal_double(self.v7)
	os:marshal_binary(self.v8)
	os:marshal_string(self.v9)
	os:marshal_uint(#self.v10); for _, v in ipairs(self.v10) do os:marshal_bool(v) end
	os:marshal_uint(tablelength(self.v13)); for k, _ in ipairs(self.v13) do os:marshal_long(k) end
	os:marshal_uint(#self.v14); for _, v in ipairs(self.v14) do os:marshal_float(v) end
	os:marshal_uint(tablelength(self.v16)); for k, v in ipairs(self.v16) do os:marshal_long(k) os:marshal_string(v) end
	os:marshal_uint(tablelength(self.v17)); for k, v in ipairs(self.v17) do os:marshal_TestBean(k) os:marshal_bool(v) end
	os:marshal_TestBean(self.v19)
	os:marshal_uint(#self.v20); for _, v in ipairs(self.v20) do os:marshal_bool(v) end
	os:marshal_uint(tablelength(self.v23)); for k, _ in ipairs(self.v23) do os:marshal_long(k) end
	os:marshal_uint(tablelength(self.v26)); for k, v in ipairs(self.v26) do os:marshal_long(k) os:marshal_string(v) end

	end,
	_unmarshal = function (self, os)

	self.v1 = os:unmarshal_bool()
	self.v2 = os:unmarshal_byte()
	self.v3 = os:unmarshal_short()
	self.v4 = os:unmarshal_int()
	self.v5 = os:unmarshal_long()
	self.v6 = os:unmarshal_float()
	self.v7 = os:unmarshal_double()
	self.v8 = os:unmarshal_binary()
	self.v9 = os:unmarshal_string()
	for i = 1, os:unmarshal_uint() do insert(self.v10, os:unmarshal_bool()) end
	for i = 1, os:unmarshal_uint() do self.v13[os:marshal_long()] = true end
	for i = 1, os:unmarshal_uint() do insert(self.v14, os:unmarshal_float()) end
	for i = 1, os:unmarshal_uint() do self.v16[os:marshal_long()] = os:marshal_string() end
	for i = 1, os:unmarshal_uint() do self.v17[os:marshal_TestBean()] = os:marshal_bool() end
	self.v19 = os:unmarshal_TestBean()
	for i = 1, os:unmarshal_uint() do insert(self.v20, os:unmarshal_bool()) end
	for i = 1, os:unmarshal_uint() do self.v23[os:marshal_long()] = true end
	for i = 1, os:unmarshal_uint() do self.v26[os:marshal_long()] = os:marshal_string() end

	end,
	_process = function (bean, session) 
		Bean.processTestType(bean, session)
	end,
})
function Stream:marshal_TestType(x) x._type = 3; Stream:marshalbean(x) end
function Stream:unmarshal_TestType() return Stream:unmarshalbean(3) end
		  
return Types