#### JRedis
JRedis是一个基于Netty，使用RESP协议开发的K,V数据库，实现了基本的K,V存储，同时支持数据持久化备份以及恢复。

#### Feature:
1. 支持K,V数据存储，采用hessian进行数据的序列化，RESP协议进行通信。
2. 支持数据持久化，使用RedoLog进行操作的记录，并以一定的频率持久化到磁盘中。
3. 支持设置数据的过期时间，目前采用lazy-delete模式。
#### todolist
1. 数据淘汰策略，LRU等