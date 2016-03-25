# 0x00 简介
SSM 即 SPRING、 SPRINGMVC、 MYBATIS 三大框架的整合。 随着其开发带来的各种便利以及好处， 越来越多的公司以及RD使用其开发各种线上产品和后台管理系统， 但也带来了许多安全性的问题。虽然JAVA 本身规范化程度比较高， 按照框架的标准进行开发确实能减少许多的SQL注入问题，但在其他方面基本上是没有做任何安全考虑。 使用SSM 通过MYBATIS的配置生成SQL语句， 基本能屏蔽掉大部分的SQL注入问题。 但由于表单提交等大多使用POJO和DTO绑定的方式， String类型参数的却不会有任何的安全性处理， 这样大片的XSS问题随之产生 。 CSRF 的问题上， SRPINGMVC亦没有内置的接口。 在其他的逻辑产生的安全问题就不再这里面的分析范畴内了。

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
http://www.myhack58.com/Article/html/3/7/2012/36142_6.htm
http://m.blog.csdn.net/article/details?id=44176385
http://m.blog.csdn.net/article/details?id=45102385  
