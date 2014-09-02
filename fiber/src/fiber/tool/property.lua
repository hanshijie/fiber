
-- 用于容器类数据, 默认方式为 END
local SIZE = SIZE  -- 先从当前单元格读到数据个数，然后读取指定个数据.
local END = END --  当前行结束,当前文件结束,或者 遇到  <END>

app {
	name = "AllDatas",  type = "AppData",
	{ name="mail", type="Mail", file="mail"},
	{ name="task", type="Task", file="task"},	
	{ name="role", type = "Role", file="role"},
}

prop { 
	name = "Role", 
	
	{ name ="id", type="int"},
	{ name="v1", type="bool"},
	{ skipline=3 }, -- 跳过N行, 此效果与 line=true可以叠加, 注意,不能在变量中定义此参数(即不能这样 { name="id", type="int", skipline=3}),必须独立配置
	{ name="v2", type="long"},
	{ name="v3", type="vector<int>", close=SIZE, line = true},
	{ name="v31", type="vector<int>", fromstring=true,},  -- fromstring 表示 数据从当前单元格的字符串中解析得到
	{ name="v4", type="hashmap<int,int>",  skip=true}, 
	{ name="v5", type="hashset<int>", line = true,},
	{ name="v6", type="vector<Task>", line = true},
	--{ name="v7", type="treeset<Task>", line = true,},
	{ name="v8", type="treemap<int,Mail>", line = true},
	{ name="v9", type="hashentry<Task>", key="id", line = true,},
	{ name="v10", type="int", skip=true} -- skip 表示此字段不从配置中加载
}

prop {
	name="Task", line=true,  -- line=true 表示这个纪录占一行，读完此记录后,忽略本行剩余数据,跳到下一行
	{ name = "id", type="int"},
	{ skiprow = 2 }, -- 跳过两个单元格
	{ name="date", type="int"},
}

prop {
	name = "Mail", line = true,
	{ name="max_mail_num" , type="int"},
	{name="timeout", type="int"},
}