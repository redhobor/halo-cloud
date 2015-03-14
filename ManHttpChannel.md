## 介绍 ##

HTTP方式通过客户端向服务器发送http请求，并接受http响应，与服务器进行通信。http方式对网络要求更低，代码实现简单，并且不需要客户端维持连接池。适合网络有限制，或者需要通信的内容不多的场景使用。

## Java版HTTP方式客户端配置 ##

1. 在guzz.xml中增加此服务：
```
<service name="commandServletChannelForServices" configName="commandServletChannelForServices" class="com.guzzservices.rpc.http.ServletCommandServiceClientImpl"/></pre>
```

Java版本客户端的http基于<a href='http://hc.apache.org/downloads.cgi'>Apache HttpClient4.0</a>实现，部署时需要包含相关jar包。

2. 配置服务参数（guzz的properties文件）：
```
[commandServletChannelForServices]
servletUrl=http://cloud.guzzservices.com/services/command/http.jsp
```

**注意：使用前，请确认您的应用程序能够通过Http方式，访问cloud.guzzservices.com的80端口！**

## PHP客户端 ##

参看 下载 中的php代码

## 其他语言版HTTP方式客户端配置 ##

对于其他语言，需要实现自己的客户端。协议接口如下：

**发送到服务器：** 以POST方式提交请求到[http://cloud.guzzservices.com/services/command/http.jsp](RawHttpJsp.md) ，POST请求中包含3个参数：
| 参数 | 取值 |
|:-------|:-------|
| command | 请求服务器执行的命令 |
| isStringParam | 参数是否是字符串。1表示param参数为字符串；0表示param参数为字节数组。 |
| param | 命令的参数值。如果参数为null，不传此参数；如果isStringParam为false，此参数为进行Base64编码后的字节数组。 |


**服务器响应：** 服务器响应4个值，如下图：
| 存储位置 | 参数 | 取值 |
|:-------------|:-------|:-------|
| Http Header头 | guzzCommandServiceException | 1表示出现异常；0表示正常执行。 |
| Http Header头 | guzzCommandServiceString | 结果长度。-1表示结果为null；0表示结果为空字符串或空字节数组；其他为正常结果长度。 |
| Http Body | 响应内容 | 响应结果的字符串。如果guzzCommandServiceString为0，则为Base64编码后的字节数组。 |

http.jsp的实现代码如下：[RawHttpJsp](RawHttpJsp.md) ，私有服务器端可以在此基础上开发其他接口，或者增加客户端调用的身份认证等。
