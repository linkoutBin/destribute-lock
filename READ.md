#分布式锁的应用场景
  当业务方法无法保证原子性操作而又需要运行分布式系统中(多个节点)下
#实现方式
##数据库乐观锁


##redis
###单节点实现（主从，集群）
 - 获取锁（unique_value可以是UUID等）
 SET resource_name unique_value NX PX 30000
 
 - 释放锁（lua脚本中，一定要比较value，防止误解锁）
 if redis.call("get",KEYS[1]) == ARGV[1] then
     return redis.call("del",KEYS[1])
 else
     return 0
 end
###可能得问题
- 节点故障 ，同步延迟导致的数据不一致
- 客户端阻塞引起锁超时
###解决方法之一：redlock算法
- 原则：向多个单独节点（节点数为奇数）同时获取锁，如果多数节点能够成功获取锁，则加锁成功，否则加锁失败，全部解锁
- 实现：redisson
- 存在的问题：由于持久化延迟导致节点崩溃后丢失数据，重启后可能会导致多个客户端同时获取到锁。

##zookeeper
