local Bean = require("bean")

local allbeans = {
	HELLO = 1,
	GETUSER  = 2,
	USERLOGIN = 3,
}

local o = { 
	_type = 7,
	_name = "SessionInfo",
	
	uid = 0,
	logintime = 0,
	_marshal = function (self, os)
		os:marshal_int(self.uid)
		os:marshal_int(self.logintime)
	end,
	_unmarshal = function (self, os)
		self.uid = os:unmarshal_int()
		self.logintime = os:unmarshal_int()
	end
}

Bean.register(7, o)

function SessionInfoHandler(session, data)
	print("receive SessionInfo. uid:%d logintime:%d", data.uid, data.logintime)
	for i = 1 , 5 do
		session:write({_type = 7, logintime = 100000,})
	end
end

return allbeans


