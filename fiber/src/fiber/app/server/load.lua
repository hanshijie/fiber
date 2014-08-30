
db = luajava.bindClass("fiber.app.server.AllTables"):get()
txn = db:getTxn()

retexception = luajava.bindClass("fiber.db.RetException")

function error(retcode)
	retexception:trigger(retcode)
end

function commit(fun) 
	txn:addTask(luajava.createProxy("java.lang.Runnable", {	run = fun }))
end
-- delay 毫秒
function schedule(fun, delay)
	txn:addScheduleTask(luajava.createProxy("java.lang.Runnable", {	run = fun }), delay)
end

function HandlerHello(sid, arg, res)
	local id = arg:getId() 
	local bag = BagManager.getBag(id)
	
	local type = math.random(1, 4)
	local tplId = math.random(1, 12)
	local num = math.random(1, 10)
	BagManager.add(bag, type, tplId, num)
	BagManager.del(bag, type, tplId, num)
	
     for i = 1, 10 do
     	BagManager.add(bag, 1, i, num)
        BagManager.add(bag, 2, i, num)
        BagManager.add(bag, 3, i, num)
    end
end

BagManager = {}

function BagManager.getBag(uid)
	local bag = db:getTableBag(uid);
	if bag:isNULL() then bag:reset() end
	return bag;
end

function BagManager.getTypeBag(bag, type)
	local subBags = bag:getBags()
	local sb = subBags:getInt(type);
	if sb:isNULL() then sb:reset() end
	return sb
end

function BagManager.add(bag, type, tplId, num)
	--print(type, tplId, num)
	local subBag = BagManager.getTypeBag(bag, type)
	local slots = subBag:getSlots()
	local find = false
	local empty_slot_idx = -1
	for i = 0, slots:size() - 1 do
		local slot = slots:get(i)
		local stplId = slot:getTplId()
		if(stplId == 0) then
			empty_slot_idx = i
		elseif stplId == tplId then
			slot:setNum(slot:getNum() + num)
			find = true
			break
		end
	end
	if not find then
		local ns = luajava.newInstance("fiber.bean.Slot")
		ns:setId(subBag:getNextId())
		subBag:setNextId(subBag:getNextId() + 1);
		ns:setTplId(tplId)
		ns:setNum(num)
		if empty_slot_idx < 0 then
			slots:add(ns);
			--print("add last");
		else
			slots:set(empty_slot_idx, ns);
			--print("add at:%d", empty_slot_idx);
		end
	end
	--print(bag)
end

function BagManager.del(bag, type, tplId, num)
	local subBag = BagManager.getTypeBag(bag, type)
	local slots = subBag:getSlots()
	
	for i = 0, slots:size() - 1 do
		local slot = slots:get(i)
		local stplId = slot:getTplId()
		if stplId == tplId then
			num = num - slot:getNum()
			if num >= 0 then
				slot:setTplId(0)
				slot:setNum(0)
			else
				slot:setNum(-num)
			end
			if num <= 0 then return end
		end
	end
	--print(bag)
	error(12)
end
