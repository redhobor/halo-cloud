## 介绍 ##

Socket Pool通过建立TCP Socket长连接池，供上层应用通信使用。由于使用长连接，Socket Pool方式对系统性能影响最小，并且速度最快。但编程和对网络端口开放的要求较高。

## Socket方式客户端配置 ##

1. 在guzz.xml中增加此服务：
```
<service name="commandSocketChannelForServices" configName="commandSocketChannelForServices" class="com.guzzservices.rpc.socket.CommandServiceClientImpl"/></pre>
```

Socket连接池基于Apache commons-pool实现，部署时需要包含此jar包。

2. 配置服务参数（guzz的properties文件）：
```
#socket channel to the commandServerService
[commandSocketChannelForServices]
host=cloud.guzzservices.com
port=11546
pool.maxActive=10
```

**注意：使用前，请确认您的应用程序能够通过TCP方式，访问cloud.guzzservices.com的11546端口！**

## 负载均衡与解决单点故障 ##

由于socket方式一般用于内网，上面的host配置一般会配置成内网IP地址，指向单台机器。为了解决单台机器单点故障的问题，在配置客户端服务时，可以同时指向多台服务器，如：

```
#socket channels to the commandServerService
[commandSocketChannelForServices]
host=192.168.11.1
port=6618
pool.maxActive=10

[commandSocketChannelForServices]
host=192.168.11.2
port=6618
pool.maxActive=10
```

通过重复[commandSocketChannelForServices](commandSocketChannelForServices.md)配置组，可以配置任意多台机器。信道接口根据pool.maxActive的大小进行多台机器之间的负载均衡，pool.maxActive越大则表示负载能力越强。如果某些机器出现故障，自动屏蔽并自动检测复活。

pool.maxActive的最大值允许为10000。如果不设置，则默认计算为其他已经设置的pool.maxActive的平均值；如果所有pool.maxActive都没有设置，所有pool.maxActive默认为500。

无论设置多少个[commandSocketChannelForServices](commandSocketChannelForServices.md)组，第一组[commandSocketChannelForServices](commandSocketChannelForServices.md)使用以下特殊参数控制整个连接池的属性：

| 参数 | 含义 | 备注 |
|:-------|:-------|:-------|
| pool.maxIdle | 最大空闲连接数 | 包含到所有机器的连接（下同） |
| pool.minIdle | 最少空闲连接数 | - |
| pool.whenExhaustedAction | 当连接用完时，连接池策略 | 1: 报错 2: 自动增长（连接池上限将无效）3: 等待（默认策略） |


对于每一组[commandSocketChannelForServices](commandSocketChannelForServices.md)，通过以下参数控制连接属性：

| 参数 | 含义 | 备注 |
|:-------|:-------|:-------|
| host | 连接到的机器 | 必须有 |
| port | 连接到的端口 | 默认为：6618 |
| charset | 当通过字符串调用时，转成字节时，使用的编码 | 默认为：UTF-8 |
| soTimeoutSeconds | 超时时间，单位秒。具体参看：<a href='http://download.oracle.com/javase/1.4.2/docs/api/java/net/Socket.html#setSoTimeout(int)'>Socket.setSoTimeout</a> | 默认为：15 |