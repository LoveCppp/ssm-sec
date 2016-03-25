# 0x00 简介
SSM 即 SPRING、 SPRINGMVC、 MYBATIS 三大框架的整合。 随着其开发带来的各种便利以及好处， 越来越多的公司以及RD使用其开发各种线上产品和后台管理系统， 但也带来了许多安全性的问题。虽然JAVA 本身规范化程度比较高， 按照框架的标准进行开发确实能减少许多的SQL注入问题，但在其他方面基本上是没有做任何安全考虑。 使用SSM 通过MYBATIS的配置生成SQL语句， 基本能屏蔽掉大部分的SQL注入问题。 但由于表单提交等大多使用POJO和DTO绑定的方式， 虽然SPRINGMVC自带有个编码处理方法， 但由于使用有限， String类型参数的却不会有任何的安全性处理， 这样大片的XSS问题随之产生 。 CSRF 的问题上， SRPINGMVC亦没有内置的接口。 在其他的逻辑产生的安全问题就不再这里面的分析范畴内了。

## 0x01 SQL注入问题
MYBATIS支持配置XML查询和通用MAPPER查询两种。 

###1. 配置XML

XML 文件中， 可以使用两种符号接收#和$ ， # 符号类似于参数绑定的方式也就是JAVA的预编译处理， 这样是不会带来SQL注入问题的， 而$却相当于只把值传进来， 不做任何处理， 类似于拼接SQL语句。

测试使用相关代码如下

- CONTROLLER代码
```java
@RequestMapping("/get1")
    public String username1(HttpServletRequest request, Model model) {
        String name = request.getParameter("name");
        User user = this.userserivce.getUserByUsername1(name);
        model.addAttribute("user", user);
        return "showuser";
    }
    
    @RequestMapping("/get2")
    public String username2(HttpServletRequest request, Model model) {
        String name = request.getParameter("name");
        User user = this.userserivce.getUserByUsername2(name);
        model.addAttribute("user", user);
        return "showuser";
    }
```

- DAO代码
```
User selectByUsername1(String name);
User selectByUsername2(@Param("name")  String name);
```


- MYBATIS XML 配置
```
<select id="selectByUsername1" resultMap="BaseResultMap" parameterType="java.lang.String" >
    select 
    <include refid="Base_Column_List" />
    from user
    where user_name = #{name,jdbcType=VARCHAR}
  </select>
  
  <select id="selectByUsername2" resultMap="BaseResultMap" parameterType="java.lang.String" >
    select 
    <include refid="Base_Column_List" />
    from user
    where user_name = '${name}'
  </select>
```

#### 测试使用#传参
正常访问

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/sql1.png)

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/sql2.png)

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/sql3.png)


加入payload访问

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/sql4.png)

无结果

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/sql5.png)

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/sql6.png)

可以看到数据执行的时候已经做了转义的处理。


#### 测试使用$传参
正常访问

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/sql7.png)

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/sql8.png)

可以看到是拼接的SQL语句

加入payload测试访问

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/sql9.png)

依然正常访问， 已有SQL注入问题

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/sql10.png)

在开发的过程中咱们尽量使用#传参， 减少$传参的使用， 如有需要， 也注意下出入参数的转义处理。


### 0x02 XSS问题
测试使用相关代码如下

- CONTROLLER
```
@RequestMapping("/show")
    public String show(HttpServletRequest request){
        return "xssshow";
    }
    
    @RequestMapping("get")
    public String get(HttpServletRequest request, Model model){
        Integer id = Integer.parseInt(request.getParameter("id"));
        User user = this.userserivce.getUserById(id);
        model.addAttribute("user", user);
        return "getuser";
    }
    
    @RequestMapping("/add")
    public String add(User user){
        int i = this.userserivce.insert(user);
        return "redirect:/xsstest/show";
    }
```

插入payload测试 `toor"'><svg/onload=alert(/xss/)>`

数据库里可看到并未做任何处理

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/xss1.png)

查询访问

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/xss2.png)

存在存储型XSS

#### 一些安全的措施
1. SPRINGMVC和JSTL进行编码处理
- WEB.XML中添加
```
    <context-param>
        <param-name>defaultHtmlEscape</param-name>
        <param-value>true</param-value>
    </context-param>
```
- JSP模板中添加
 - 针对所有FORM
```
 <spring:htmlEscape defaultHtmlEscape="true" />
```
 - 针对单个INPUT等
 ```
   <form:input path="name" htmlEscape="true" />
 ```
 - 直接输出的标签
```
    <c:out value="${user.userName}" />
```
2. 重写HttpServletRequestWrapper或HttpServletResponseWrapper使用FILTER进行过滤
- WEB.XML中添加
```
<filter>
        <filter-name>XSS</filter-name>
        <filter-class>ssm.sec.filter.XSSFilter</filter-class>
    </filter>

    <filter-mapping>
        <filter-name>XSS</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```

- RequestWrapper.java

```
    private static Logger logger = Logger.getLogger(RequestWrapper.class);
    public RequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
    }
    public String[] getParameterValues(String parameter) {
        logger.info("InarameterValues .. parameter .......");
        String[] values = super.getParameterValues(parameter);
        if (values == null) {
            return null;
        }
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = cleanXSS(values[i]);
        }
        return encodedValues;
    }
    public String getParameter(String parameter) {
        logger.info("Inarameter .. parameter .......");
        String value = super.getParameter(parameter);
        if (value == null) {
            return null;
        }
        logger.info("Inarameter RequestWrapper ........ value .......");
        return cleanXSS(value);
    }

    public String getHeader(String name) {
        logger.info("Ineader .. parameter .......");
        String value = super.getHeader(name);
        if (value == null)
            return null;
        logger.info("Ineader RequestWrapper ........... value ....");
        return cleanXSS(value);
    }

    private String cleanXSS(String value) {
        logger.info("InnXSS RequestWrapper ..............." + value);

        value = value.replaceAll("eval\\((.*)\\)", "");
        value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");

        value = value.replaceAll("(?i)<script.*?>.*?<script.*?>", "");
        value = value.replaceAll("(?i)<script.*?>.*?</script.*?>", "");
        value = value.replaceAll("(?i)<.*?javascript:.*?>.*?</.*?>", "");
        value = value.replaceAll("(?i)<.*?\\s+on.*?>.*?</.*?>", "");
        
        value = value.replaceAll("<", "& lt;").replaceAll(">", "& gt;");
        value = value.replaceAll("\\(", "& #40;").replaceAll("\\)", "& #41;");
        value = value.replaceAll("'", "& #39;");
        logger.info("OutnXSS RequestWrapper ........ value ......." + value);
        return value;
    }
}

```


- XSSFilter.java

```
package ssm.sec.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class XSSFilter implements Filter {
    private static Logger logger = Logger.getLogger(XSSFilter.class);
    private FilterConfig filterConfig;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void destroy() {
        this.filterConfig = null;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        logger.info("Inlter CrossScriptingFilter  ...............");
        chain.doFilter(new RequestWrapper((HttpServletRequest) request), response);
        logger.info("Outlter CrossScriptingFilter ...............");
    }

}

```

- 重新插入数据， 会发现已经进行了过滤处理

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/xss3.png)

数据库查看都是过滤和转义的结果

![](https://coding.net/u/b0lu/p/ssm-sec/git/raw/master/images/xss4.png)

> 当然有时候由于业务需要，可能允许用户输入这种危险的字符， 这种通用的并不适用， 但可以考虑把一些特殊的符号转义成中文符号，     也许也恰好能满足业务的需求， 如< 转义成 <等，看起来和英文的一样， 但由于解释器并不识别， 而又变成安全的了。

3. SPRINGMVC 视图中传递变量一般使用EL表达式
> 虽然EL中内置许多默认的函数， 但并没有编码处理的函数， 因此只能够自定义函数进行处理。

- 开发函数处理类，处理类就是普通的类，每个函数对应类中的一个静态方法
```
public class XSSEL {
    private static Logger logger = Logger.getLogger(XSSEL.class);
    
    public static  String HTMLEncode(String val){
        logger.info("In EL func values ====>"+ val);
        if(val != null){
            val = val.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                    .replace("/", "&#x2F;").replace("'", "&#x27;");
            logger.info("Out EL func values ====>"+ val);
            return val;
        }else{
            return "";
        }
    }
}
```
- 建立TLD文件，定义表达式函数

```xml
<?xml version="1.0" encoding="UTF-8"?>
<taglib xmlns="http://java.sun.com/xml/ns/j2ee"       
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"       
    xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee   
    http://java.sun.com/xml/ns/j2ee/web-jsptaglibrary_2_0.xsd"       
    version="2.0">  
  
  <tlib-version>1.0</tlib-version>
  <short-name>sec</short-name>

  
  <function>
    <name>HTMLEncode</name>
    <function-class>ssm.sec.utils.XSSEL</function-class>
    <function-signature>java.lang.String HTMLEncode(java.lang.String)</function-signature>
  </function>
  
</taglib>
```

- 在WEB.XML文件中配置（可省略）
```
    <jsp-config>
        <taglib>
            <!-- 配置标签的引用地址 JSP页面中引用时使用 -->
            <taglib-uri>/eltag</taglib-uri>
            <!-- 配置标签的TLD文件地址 -->
            <taglib-location>/WEB-INF/XSSEL.tld</taglib-location>
        </taglib>
    </jsp-config>
```
- 在JSP页面内导入并且使用
```
<%@ taglib uri="/eltag" prefix="sec" %>  
<%@ taglib uri="/WEB-INF/XSSEL.tld" prefix="sec" %>WEB.XML文件中不配置的情况  
${sec:HTMLEncode(user.userName)} 
```

> 针对复杂的业务也可以考虑定义一系列安全编码的方法， 避免重复造轮子的同时也可以增强程序的安全性。

## CSRF问题
> 由于SPRINGMVC 本身不提供像DJANGO那样的CSRF安全接口， 因此在这方面就要脆弱了许多。 这和其他框架的存在和处理的方式亦差不多， 也或者说写出的CSRF代码也更具有通用性。 所以呢主要是浅谈对CSRF问题的处理思路以及相应的好与坏。

### 1.验证HTTP REFERER字段

HTTP协议的REFERER字段会记录请求来源地址。在通常请况下，一般情况下安全请求来自于同一个网站，如果REFERER来源为第三方网站，则很可能是CSRF攻击，即使REFERER是来自于同一个网站，也有很能是站内的CSRF攻击，那么只能通过正则匹配出来源是否为我们指定的允许请求的链接。

### 2.添加验证码

CSRF攻击的过程是在用户不知情的情况下构造网络请求，所以验证码机制是最有效最快捷的方法。但很多时候，出于用户体验的情况下，并不能给所有的操作都添加上验证码，除非某个操作的敏感性、重要性都十分的高，那么可以考虑加上验证码的方式来验证。

### 3.添加Token值

#### A）.token储存在cookie中

Token储存在cookie中，必须考虑两个问题：

1) 假如同域名下存在XSS，那么cookie则可以被盗取；

2) 表单的token值如果和cookie储存的相等，那么当cookie值被盗取的时候，则可以构造出表单cookie；

针对以上两个问题，那么在cookie储存token的实现过程中，必须的有相应的解决方法：

1.  对cookie值储存token设置httponly；
2.  设置httponly在某些情况下还是可以泄露cookie的，因此表单的token和cookie的token得有一个加密的过程。

#### B）.token储存在session中

Token储存在session中，其实和cookie差不多是一个原理，但有点小差别的是session是储存在服务器的，因此诞生的一个问题便是在分布式环境中session不复制的话，那么此机制便失效了，并且会对正常的业务产生影响。

因此，当使用sesseion储存的时候，必须考虑搭建的web环境为非分布式环境。

#### C）.one-time token(token储存在memcache等中)

One-time token即一次性token值，请求一次一页面即获取不同的token值。One-time token的难点在“并行会话”中，如用户在同一个站点当中打开两个一样的页面，那么必然两个表单内容也一样，CSRF防护措施不能导致只有一个页面才能提交表单，其它的表单都包含的是无效的token值。因此one-time token必须要在保证不影响“并行会话”的基础上实现。

以下是实现思路：

1) 生成token值，以此token值作为memcached的key和value储存；

2) 在表单中加入此token值的hidden表单；

3) 验证时比较表单和memcached的token值。
