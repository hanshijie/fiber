local net = require("network")
local log = require("log")

function SessionInfoHandler(session, data)
	log.log("SessionInfoHandler. uid:%s logintime:%s", data.uid, data.logintime)
	session:write({_type = 7, uid = 1218})
end

local client = net.client("127.0.0.1", 1314, true)
while true do
	net.sleep(1)
	net.poll(0)
end




