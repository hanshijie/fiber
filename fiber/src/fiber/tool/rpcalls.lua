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

bean{  
	name="TestType", type=3, initsize=256, maxsize=65536, handlers= hs1,
	
	{ id= 1,    name="v1",  type="bool",                        comment="1字节布尔,0表示假,1表示真,其它默认表示真" },
	{ id= 2,    name="v2",  type="byte",                        comment="1字节整数" },
	{ id= 3,    name="v3",  type="short",                       comment="2字节整数" },
	{ id= 4,    name="v4",  type="int",                         comment="4字节整数" },
	{ id= 5,    name="v5",  type="long",                        comment="8字节整数" },
	{ id= 6,    name="v6",  type="float",                       comment="4字节浮点数" },
	{ id= 7,    name="v7",  type="double",                      comment="8字节浮点数" },
	{ id= 8,    name="v8",  type="binary",                   	comment="二进制数据(Octets)" },
	{ id= 9,    name="v9",  type="string",                      comment="字符串(String)" },
	{ id=10,    name="v10", type="vector<bool>",            	comment="数组容器(ArrayList)" },
	{ id=11,    name="v11", type="list<byte>",                  comment="链表容器(LinkedList)" },
	{ id=13,    name="v13", type="hashset<long>",               comment="无序集合容器(HashSet)" },
	{ id=14,    name="v14", type="treeset<float>",              comment="排序集合容器(TreeSet)" },
	{ id=16,    name="v16", type="hashmap<long,string>",        comment="无序映射容器(HashMap)" },
	{ id=17,    name="v17", type="treemap<TestBean,bool>",      comment="排序映射容器(TreeMap)" },
	{ id=19,    name="v19", type="TestBean",                    comment="嵌入其它bean" },
}

rpc {
	name="Hello", type=4, maxsize=100, arg="HelloArg", res="HelloRes", timeout="30", handlers= hs1,
}