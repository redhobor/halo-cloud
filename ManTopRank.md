## 功能介绍 ##

通用的可以手工干预排行结果，定期自动发布的排行榜。干预结果时，可以隐藏作弊的排名；被隐藏的记录，在下次自动生成排行榜时自动隐藏。

每个排行榜最多允许200条记录进行榜单。

## 使用方式 ##

登录：http://cloud.guzzservices.com/services/，进入“系统管理->排行榜”。创建排行榜分组，在分组下创建具体的排行榜。

排行榜分组用于定义一批“对同一项内容”的排行。如我们同时会有”登录最多的用户排行“，”经验值最高用户排行“，”粉丝最多用户排行“等，则可以创建一个”用户排行“分组，然后在这个分组下创建这些用户相关的排行榜。这样的好处是，同一组下的作弊名单是共享的；假如你在”登录最多的用户排行“中隐藏了用户”Alice“，禁止她参与排名，则同一组下的其他排行榜也会自动禁止Alice参与排名。

使用排行榜不需要引入任何API，和语言也无关。排行榜创建以后，系统自动通过HTTP GET ”数据提供者URL“填写的url 获取排行的原始数据（JSON格式），完成排行榜干预后，自动或者用户手动点击发布时，通过HTTP协议将排行榜合成的内容，POST到 ”数据POST发布到URL“ 填写的url。

排行榜支持定期自动刷新排行，调度采用 http://www.quartz-scheduler.org/ 调用，调度表达式基于Cron Trigger。填写cron trigger时，省略第1位秒，这个由系统自动赋值。也就说调度周期最频繁为分钟级别。例如填写”0  **?**“，则quartz实际的调度策略为”0 0  **?**“。

## 获取排行原始数据 ##

在读取原始排行数据时，系统将发起HTTP GET请求，并附带5个参数。参数如下：

| 参数 | 含义 | 备注 |
|:-------|:-------|:-------|
| authKey | 验证key，数据提供者可以通过此参数验证请求是否合法 | 在创建排行榜时自己设定 |
| statId | 排行榜编号 | 系统自动生成，以后不会变化 |
| time | 提取多长时间内的排行数据 | 创建排行榜时自己设定填写的”统计最近多长时间的记录“。 |
| programId | 自定义字段 | 创建排行榜时自己设定填写的programId |
| pageSize | 读取多少记录 | 创建排行榜时自己设定填写的”最大读取记录数“。 |

HTTP GET请求的url应该返回json格式的原始排行数据。数据内容为一个记录列表，每个记录的属性如下：
```
	/**统计对象的主id*/
	private String objectId ;
		
	/**操作次数，将按照此字段进行排序*/
	private int opTimes ;
		
	//添加一些常用显示字段
	private String objectTitle ;
	
	/**对象细览地址*/
	private String objectURL ;
	
	private String objectCreatedTime ;
	
	/**自定义字段，每个统计对象可以添加一些其他信息供自己展示使用*/
	private String extra1 ;
	
	private String extra2 ;
	
	private String extra3 ;
```

举例来说：

```
List m_topics = (List) request.getAttribute("m_topics") ;
LinkedList records = new LinkedList() ;

for(Topic t : m_topics){
	HashMap r = new HashMap() ;
	
	r.put("objectId", t.getId()) ;
	r.put("opTimes", t.getTotalActionsCount()) ;
	r.put("objectTitle", t.getDisplayName()) ;
	r.put("objectURL", "http://xxx/topic.do?tid=" + t.getId()) ;
	r.put("objectCreatedTime", t.getCreatedTime().toString()) ;
	r.put("extra1", t.getTemplate()) ;
	r.put("extra2", t.getActionsCount()) ;
	r.put("extra3", "") ;
	
	records.addLast(r) ;
}

out.print(com.guzzservices.rpc.util.JsonUtil.toJson(records)) ;
```


## 发布排行榜 ##

发布时，系统读取允许发布的排行榜数据，根据创建排行榜时设定的”Velocity发布模板“，用Apache Velocity引擎将排行榜翻译为一段文本内容，将内容通过HTTP POST方式提交到用户设置的”数据POST发布到URL“的URL地址，完成发布。

接受POST的地址应该处理此请求，将翻译后的文本内容，按照业务需要进行排行更新。

发起POST时，将附带6个参数。具体参数如下：

| 参数 | 含义 | 备注 |
|:-------|:-------|:-------|
| authKey | 验证key，数据提供者可以通过此参数验证请求是否合法 | 在创建排行榜时自己设定 |
| statId | 排行榜编号 | 系统自动生成，以后不会变化 |
| statName | 排行榜名称 | 录入的名称 |
| time | 多长时间内的排行数据 | 创建排行榜时自己填写的”统计最近多长时间的记录“。 |
| programId | 自定义字段 | 创建排行榜时自己填写的programId |
| content | 排行文本内容 | Velociy模板翻译后的排行数据 |