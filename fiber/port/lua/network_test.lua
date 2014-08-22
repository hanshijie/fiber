local net = require("network")
local log = require("log")

local client = net.client("127.0.0.1", 1314, true)
while true do
	net.sleep(1)
	net.poll(0)
end




