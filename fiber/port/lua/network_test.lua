loadfile("genrpc.lua")()

local net = require("network")
local log = require("log")
local bean = require("bean")
local Types = require("allbeans")

function bean.processSessionInfo(data, session)
	log.log("SessionInfoHandler. uid:%s logintime:%s", data.uid, data.logintime)
	session:write({_type = 7, uid = 1218, roleids = {1, 2, 3, 4, 5, 6}})
end

function bean.onClientHello(arg, res, session)
    log.log("onClientHello. arg.v1:%s", arg.v1)
end

function bean.onTimeoutHello(arg, session)
    log.print("onTimeoutHello")
end

local client = net.client("client", "192.168.28.129", 1314, 3, false)

client:sendrpc(Types.Hello, { v1 = true })
client:sendrpc(Types.Hello, { v1 = false }, function (arg, res, session) log.print("yes. i do. v1:", arg.v1) end)
while true do
	net.sleep(0.1)
	net.poll(0)
end




