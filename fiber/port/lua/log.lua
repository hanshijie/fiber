local log = {}
function log.log(...)
	print(string.format(...))
end

function log.err(...)
	print("[err]" .. string.format(...))
end

function log.print(...) 
	print(...) 
end

return log