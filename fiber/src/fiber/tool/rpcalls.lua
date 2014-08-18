local bean = bean or function() end
local rpc = rpc or function() end
local handler = handler or function() end
local handlerset = handlerset or function() end
local namespace = namespace or "fiber"

handler {
	name="Server", path="server"
}

handler {
	name="Client", path="client"
}

local hs1 = handlerset {"Server", "Client" }

bean {
	name="TestBean", type="2", maxsize="100", handlers = hs1,
	{ name="v1",  type="bool",                        comment="1字节布尔,0表示假,1表示真,其它默认表示真" },
}

bean{  
	name="TestType", type=3, initsize=256, maxsize=65536, handlers= hs1,
	
	{ name="v1",  type="bool",                        comment="1字节布尔,0表示假,1表示真,其它默认表示真" },
	{ name="v2",  type="byte",                        comment="1字节整数" },
	{ name="v3",  type="short",                       comment="2字节整数" },
	{ name="v4",  type="int",                         comment="4字节整数" },
	{ name="v5",  type="long",                        comment="8字节整数" },
	{ name="v6",  type="float",                       comment="4字节浮点数" },
	{ name="v7",  type="double",                      comment="8字节浮点数" },
	{ name="v8",  type="binary",                   	comment="二进制数据(Octets)" },
	{ name="v9",  type="string",                      comment="字符串(String)" },
	{ name="v10", type="vector<bool>",            	comment="数组容器(ArrayList)" },
	{ name="v13", type="hashset<long>",               comment="无序集合容器(HashSet)" },
	{ name="v14", type="treeset<float>",              comment="排序集合容器(TreeSet)" },
	{ name="v16", type="hashmap<long,string>",        comment="无序映射容器(HashMap)" },
	{ name="v17", type="treemap<TestBean,bool>",      comment="排序映射容器(TreeMap)" },
	{ name="v19", type="TestBean",                    comment="嵌入其它bean" },
}

bean {
	name="HelloArg", type=41, maxsize="100",
	{ name="v1",  type="bool",                        comment="1字节布尔,0表示假,1表示真,其它默认表示真" },
}
bean {
	name="HelloRes", type=42, maxsize="100",
	{ name="v1",  type="bool",                        comment="1字节布尔,0表示假,1表示真,其它默认表示真" },
	{ name="v3",  type="short",                       comment="2字节整数" },
}

rpc {
	name="Hello", type=4, maxsize=100, arg="HelloArg", res="HelloRes", timeout="30", handlers= hs1,
}