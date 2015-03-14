## 功能介绍 ##

提供对一段内容的关键词检查与标红服务。对于涉及内容安全与审核的系统，以及关键词分析的系统，对文字进行关键词过滤是一项必要功能，关键词服务即用于完成此项功能。

本服务提供关键词的分组在线管理，词汇分级，内容过滤，涉及词汇提取，以及正文标红等功能。

在过滤时，附加支持：不区分大小写 + 可检测在词汇中插入特殊字符 + 不破坏HTML和UBB代码。

## 示例代码 ##

```
//第1行，获取服务（JSP中写的）
WordFilterService wordFilterService = (WordFilterService) GuzzWebApplicationContextUtil.getGuzzContext(session.getServletContext()).getService("wordFilterService") ;

//第2行，进行内容过滤。
MatchResult result = (MatchResult) wordFilterService.filterHtml("你好，我是guzz，a*a，你是谁？", new String[]{"your groupId, something like 'b3vh5xmun0r2z4pkil2g5rpxnt2mu76n0r7qqoa'"}, true) ;

if(result != null){
	//含有关键词
	out.println("<br>最高警告级别:" + result.getHighestLevel()) ;
	out.println("<br>匹配到的内容组成的字符串列表:" + result.getHittedContentList()) ;
	out.println("<br>标记以后的内容:" + result.getMarkedContent()) ;
	out.println("<br>匹配的关键词列表:" + result.getMatchedContentList(",", 5)) ;
}else{
	//不包含关键词
	out.println("<br>passed!") ;
}
```

## 配置服务 ##
1. 配置本服务依赖的[通信信道服务](ManServiceChannel.md)。假设配置好的信道服务名称为”commandSocketChannelForServices”.

2. 访问<a href='http://cloud.guzzservices.com/services/console/filterWordGroupList.jsp'><a href='http://cloud.guzzservices.com/services/console/filterWordGroupList.jsp'>http://cloud.guzzservices.com/services/console/filterWordGroupList.jsp</a></a>创建关键词组，增加关键词。如果部署了自己的私有服务端，从自己的私有服务器上配置。

3. 在guzz.xml中增加此服务：
```
<service name="wordFilterService" dependsOn="commandSocketChannelForServices" class="com.guzzservices.secure.wordFilter.WordFilterServiceImpl"/>
```

4. 配置服务参数（guzz的properties文件）：**不需要配置**

## 服务API ##

在需要内容审核的地方，获取或注入wordFilterService，java接口为：

`com.guzzservices.secure.WordFilterService`

API定义：

```
package com.guzzservices.secure;
public interface WordFilterService {
	
	/**
	 * 过滤一段文字，根据参数决定是否标红。如果不含有任何关键词，返回null。
	 * 
	 * @param content 检测内容
	 * @param groupIds 配置的关键词组编号
	 * @param markContent 是否同时标红过滤的内容。
	 * @return MatchResult
	 */
	public MatchResult filterText(String content, String[] groupIds, boolean markContent) throws Exception ;
	
	/**
	 * 过滤一段html代码段，根据参数决定是否标红。如果不含有任何关键词，返回null。
	 * 
	 * @param content 检测内容
	 * @param groupIds 配置的关键词组编号
	 * @param markContent 是否同时标红过滤的内容。
	 * @return MatchResult
	 */
	public MatchResult filterHtml(String content, String[] groupIds, boolean markContent) throws Exception ;
	
}
```

服务接口返回null则表示传入的内容不包含敏感词，否则返回MatchResult提供过滤细节。

“同时标红过滤的内容”将增加网络流量与延迟，如果仅仅是检查内容，不需要将标红的正文显示出来，传入false速度会更快。

**MatchResult定义：**
```
package com.guzzservices.secure.wordFilter;
public class MatchResult {
	
	/**
	 * 将发现的关键词列表进行Distinct排重处理，同时统计每个词的出现次数
	 * 
	 * @return 返回Map<String, 出现次数> 包含 Distinct 处理后的关键词列表以及相应的出现次数
	 */
	public Map<String, Integer> groupMatchedFilterWords() ;

	/**
	 * 返回得到的关键词中最高警告级别
	 */
	public int getHighestLevel() ;
	
	/**是否可以通过给定的关键词等级*/
	public boolean canPass(int level) ;
	
	/**标红以后的内容。如果调用服务接口时，参数markContent传入false，则此方法返回null。*/
	public String getMarkedContent() ;

	/**成功匹配到的关键词*/
	public List<String> getMatchedFilterWords() ;

	/**匹配到的内容组成的字符串列表*/
	public String getHittedContentList() ;
	
	/**
	 * 返回匹配的关键词列表。方法会自动删除重复的关键词，并且将返回字符串长度限制在@param maxLength范围内。
	 * @param wordSep 关键词之间用什么符号连接，如", "。
	 * @maxLength 返回的串最长允许多长。如果需要将返回结果存入到数据库中，则此参数一般传入数据库字段允许的最大长度。
	 */
	public String getMatchedContentList(String wordSep, int maxLength) ;
}

```