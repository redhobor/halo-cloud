## 功能介绍 ##
提供应用系统的配置项管理，当配置调整时进行自动推送，满足系统在线调整配置项并自动生效的功能。系统配置服务按照组进行配置管理，可以为一个应用配置一个或者多个组；每个组内允许最多200个配置项。配置项支持定义类型，类型包括：数字，浮点数，布尔，枚举，字符串，以及大文本。

程序在使用时，通过key对配置进行读取。不同的配置组之间，key可以重复。

系统配置服务支持应用集群。本服务会在应用端缓存相关的配置项，不用担心性能问题。

配置项在管理台修改后，一般在10秒内生效；如果需要调整生效时间，请查看源代码。

## 示例代码 ##
```
//第1行，获取配置管理服务（JSP中写的）
ConfigurationService s = (ConfigurationService) GuzzWebApplicationContextUtil.getGuzzContext(session.getServletContext()).getService("configurationService") ;

//第2行，读取配置。
out.println("<br>评论审核策略:" + s.getInt("comments.check.policy", 1)) ;
out.println("<br>邮件服务器IP:" + s.getString("mail.server.ip")) ;
out.println("<br>页面banner头代码:" + s.getString("page.head.banner")) ;
```

## 配置服务 ##
1. 配置本服务依赖的[通信信道服务](ManServiceChannel.md)。假设配置好的信道服务名称为”commandSocketChannelForServices”.

2. 访问<a href='http://cloud.guzzservices.com/services/console/configGroupList.jsp'><a href='http://cloud.guzzservices.com/services/console/configGroupList.jsp'>http://cloud.guzzservices.com/services/console/configGroupList.jsp</a></a>创建配置组，增加需要的配置项。如果部署了自己的私有服务端，从自己的私有服务器上配置。


3. 在guzz.xml中增加此服务：
```
<service name="configurationService" configName="configurationService" dependsOn="commandSocketChannelForServices" class="com.guzzservices.management.config.ConfigurationServiceImpl"/>
```

4. 配置服务参数（guzz的properties文件）：
```
[configurationService]
groupId=your groupId(类似gxfay0u5yflb6s3mgb7xs4howqvoj9xxwfbr8dh4x4ay9zpmjue3up47edz0e8的串)
```

5. 如果需要使用多个配置项组，重复第3第4步，定义多个服务实例。

## 服务API ##

在需要检查配置项的地方，获取或注入configurationService，java接口为：

```
com.guzzservices.management.ConfigurationService
```

API定义：

```
package com.guzzservices.management;
public interface ConfigurationService {
	
	/**get string type parameter. including string and text.*/
	public String getString(String parameter) ;

	/**get short type parameter. including string and text.*/
	public short getShort(String parameter, short defaultValue) ;
	
	public int getInt(String parameter, int defaultValue) ;
	
	public long getLong(String parameter, long defaultValue) ;
	
	public float getFloat(String parameter, float defaultValue) ;
	
	public double getDouble(String parameter, double defaultValue) ;

	public boolean getBoolean(String parameter, boolean defaultValue) ;

}
```