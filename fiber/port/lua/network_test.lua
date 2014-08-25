--require("genrpc")
 
loadfile("genrpc.lua")()

local net = require("network")
local log = require("log")
local bean = require("bean")

function bean.processSessionInfo(data, session)
	log.log("SessionInfoHandler. uid:%s logintime:%s", data.uid, data.logintime)
	session:write({_type = 7, uid = 1218, roleids = {1, 2, 3, 4, 5, 6}})
end

local client = net.client("client", "127.0.0.1", 1314)

while true do
	net.sleep(0.1)
	net.poll(0)
end




