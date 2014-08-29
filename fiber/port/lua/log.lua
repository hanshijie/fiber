local print = print
local format = string.format

local log = {}
function log.log(...)
	print(format(...))
end

function log.err(...)
	print("[err]", format(...))
end

function log.print(...) 
	print(...) 
end

return log