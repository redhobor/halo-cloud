## 功能介绍 ##

建立起一个与服务器通信的协议信道，供其他应用服务与服务器交互时使用。如果将一般的服务，如内容过滤、IP反查等理解为网络应用软件（像QQ，迅雷等），则信道服务为底层的TCP/IP协议，负责应用软件的网络通讯工作。

通信信道服务分为客户端和服务器端。在一个应用系统中，可以配置多个客户端，连接多个服务器。一般应用程序只需要配置客户端即可。对于guzzservices提供的所有服务，如果没有特别声明都只需要配置客户端。

通信信道服务目前实现了TCP Socket长连接池和Http短连接方式，后续还将考虑基于hessian等RPC协议。

## 客户端配置 ##

| 通道协议 | 优点 | 缺点 |
|:-------------|:-------|:-------|
| [客户端Socket Pool通信信道](ManSocketChannel.md) | 速度快，并发高，适合大规模系统使用 | 客户端实现复杂(java客户端已经实现)；需要网络开通相关端口 |
| [客户端HTTP协议通信信道](ManHttpChannel.md) | 跨语言协议容易实现；短连接方式，不需要维护连接池。适合服务调用比较少的系统使用。 | 速度比socket方式慢 |

## 服务器端配置 ##

服务器端配置，仅在你需要对外提供服务时使用。例如你编写一个公共服务，供其他项目使用，则此服务需要配置服务端，而使用者只需要配置客户端。

### 配置服务器端，以支持客户端socket连接： ###
1. 在guzz.xml中增加此服务：
```
<service name="commandServerService" configName="commandServerService" class="com.guzzservices.rpc.server.nio.MinaCommandServerServiceImpl"/>
```

服务端为Apache Mina框架实现，使用时需要加入Mima的jar包。

2. 配置服务参数（guzz的properties文件）：
```
#command server channel for general used services
[commandServerService]
port=11546
idleTimeSeconds=300
```

## 编写服务的客户端API ##

1. 定义你的服务；在实现服务时，通过服务依赖，注入CommandService到你的服务中（上面第二节配置的）
```
<service name="yourService" dependsOn="commandSocketChannelForServices"  class="xxxxx"/>
```

2. 注入后，在用户调用API时，封装参数并转调CommandService的3个API调用远程服务，封装结果并返回。CommandService的类为：
```
com.guzzservices.rpc.CommandService
```

；两个方法为：
```
public interface CommandService {

/**
* Execute a command, and return the result as a string.
*
* @param command command name
* @param param parameters
* @throws Exception
*/
public String executeCommand(String command, String param) throws Exception ;

/**
* Execute a command, and return the result as a byte array.
*
* @param command command name
* @param param parameters
* @throws Exception
*/
public byte[] executeCommand(String command, byte[] param) throws Exception ;

	
/**
* Execute a command, and return the result as a ByteBuffer.
* 
* @param command command name
* @param param parameters
* @throws Exception
*/
public ByteBuffer executeCommand(String command, ByteBuffer param) throws Exception ;

}
```

3个API都需要传入一个命令，以及一个参数；如果服务器执行错误，或出现网络错误，抛出异常。

API函数执行为同步执行，只有完成远程调用才会返回。

## 编写服务的服务端实现： ##

服务器端与客户端服务对应，用于处理客户端的远程调用。服务器端通过客户端传入的command命令区分服务。

对于一个服务，需要实现一个com.guzzservices.rpc.server.CommandHandler，并且在commandServerService中注册该handler能够处理的command命令。这样，当某个远程调用发生时，commandServerService根据command查找到对应的CommandHandler，执行调用。

CommandHandler需要实现的方法，与前面CommandService的3个方法相对应。CommandHandler允许抛出异常，抛出异常时，客户端将对应的抛出异常。

编写方式：
<ol>
<blockquote><li>编写类，实现com.guzzservices.rpc.server.CommandHandler接口。一般选择通过继承 com.guzzservices.rpc.server.CommandHandlerAdapter 的方式实现。实现类覆盖支持的调用方法，实现具体服务。</li>
<li>获得类的实例，例如spring中为一个bean。</li>
<li>获得上面配置的commandServerService服务的示例。commandServerService的java类型为：com.guzzservices.rpc.server.CommandServerService</li>
<li>调用commandServerService的方法注册步骤1实现Handler可以处理的命令。例如：</li>
</ol>
<pre><code>commandServerService.addCommandHandler(“query.cn.ip”, cnIpLocateHandler) ;<br>
</code></pre></blockquote>


一般情况下，我们会通过依赖注入的方式注册服务器端服务。注册方式：
<ol>
<blockquote><li>在CommandHandler中增加setCommandServerService(CommandServerService css)方法。并通过spring IOC等，注入commandServerService</li>
<li>在这个方法中调用：css.addCommandHandler(command, cnIpLocateHandler) ;</li>
</ol></blockquote>

服务器端实现参考：

http://code.google.com/p/halo-cloud/source/browse/trunk/src/com/guzzservices/manager/impl/ip/ChinaIPLocateImpl.java

http://code.google.com/p/halo-cloud/source/browse/trunk/src/com/guzzservices/manager/impl/ConfigurationManagerImpl.java