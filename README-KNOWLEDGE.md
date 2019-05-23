
							> > > 高并发秒杀系统 < < <
					 
	1. 使用的框架：SpringMVC + Spring + MyBatis	+ JSP + Bootstrap + JQuery
	
	2. 系统框架的技术：
		(1)MySQL层：表设计、SQL技术、事务和行级锁
		(2)MyBatis层：DAO层的设计、MyBatis合理使用、MyBatis和Spring的整合
		(3)Spring层：SpringIOC整合Service、声明式事务的运用
		(4)SpringMVC层：Restful接口的设计与使用、框架运作流程、Controller开发技巧
		(5)前端：交互设计、Bootstrap、jQuery
		(6)高并发：高并发点和高并发分析、优化思路并实现
		
	3. maven项目的搭建:
		mvn archetype:generate 
		-DgroupId=com.gdufe.seckill -DartifactId=seckill
		-DarchetypeArtifactId=maven-archetype-webapp
		-DinteractiveMode=false
	
	4. 将项目导入到IntelliJ IDEA中，并：
	   (1) 修改web.xml的servlet版本为最新的版本[从官网获取或者从包含servlet项目中获取]
	   (2) 补全maven项目的目录骨架[Project Structure设置目录的级别]
	   (3) 在pom.xml中添加项目开发的相关依赖[Get From MVN-Website]
					
	5. 秒杀业务的难点: 高并发下的效率问题 + 事务 + 行级锁
	
	6. 需要思考的问题：各个框架的具体使用场景和功能
	
				
一、Java高并发秒杀API之DAO层
	
	1. 数据库表的设计[SQL编写(engine=InnoDB + auto_increment=1000 + comment)]
	
	2. Dao接口的设计: 根据数据库表进行映射关系的接口方法设计(@Param参数的作用)
	
	3. MyBatis的mapper.xml文件的SQL编写
		>> #{seckillId}、<![CDATA[<=]]>
		>> resultType、parameterType、statementType
		>> limit、ignore、as、[table1] inner join [table2] on [连接条件]
		>> call 存储过程: #{seckillId,jdbcType=BIGINT,mode=IN}

	4. mybatis-config.xml用于配置mybatis的全局设置，如驼峰命名转换等
						<setting name="mapUnderscoreToCamelCase" value="true"/>
		
	5. Spring-MyBatis整合流程
		>> 配置dataSource -> com.mchange.v2.c3p0.ComboPooledDataSource
		>> 配置sqlSessionFactory -> org.mybatis.spring.SqlSessionFactoryBean
		>> 配置spring-mybatis映射的接口实现类的Mapper -> 
							org.mybatis.spring.mapper.MapperScannerConfigurer
							(注意配置spring扫描的接口包的location : basePackage)
	
	6. myBatis整合Spring框架（要点+难点）
	   1.上线测试出现的BUG及解决办法：
		(1) 在xxxDao.xml接口中(Ctl+Shift+T) Create Test时没有找到可选择的方法？
			· 将接口interface暂时改为class，再使用同样的方法去做就可以了，然后把interface改回来
			· 我用的是2019新版本的IDEA，据说是版本的问题
			
		(2) mapUnderscoreToCamelCase 而不是 mapUnderscoreCamelCase
		
		(3) 数据库连接失败：Access denied for user "LU131@localhost" ...
			· 检查数据库服务开启否? Driver + url + username + password 是否正确
			· 我的Error--> url=jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=utf8
			  修改url为：url=jdbc:mysql://localhost:3306/seckill?serverTimezone=GMT%2B8 连接成功
			· 最好的url:url=jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
			  
		(4) at com.mchange.v2.c3p0.impl.NewProxyResultSet.isClosed?
			· 出现该BUG的原因是c3p0对JDBC的支持性原因，将pom.xml的c3p0版本改为0.9.2.x以上的版本即可解决该问题
			· 参考GitHub社区解析：https://github.com/swaldman/c3p0/issues/18
			

二、Java高并发秒杀API之Service层	
	
	1. Java中的异常: 编译期异常 + 运行期异常
		>> 运行期的异常不需要手动去try+catch
		   参考: https://blog.csdn.net/yuming226/article/details/83313273
	    >> Spring声明式事务只接受程序运行期异常回滚策略
	
	2. 为什么要使用Spring IOC (也称为依赖注入DI)?
		(1) 对象创建统一托管，而不是在程序中通过new的方式去创建
		(2) 规范的生命周期管理，比如init()，destroy()
		(3) 灵活的依赖注入
		(4) 一致的获取对象，可以在IOC容器中获取需要的依赖实例对象，这些对象实例
			默认都是singleton(单例模式)
			
	3. Spring中的各种常用注解解析:https://www.cnblogs.com/xiaoxi/p/5935009.html
	
		>> resources文件夹下的spring-xxx.xml能扫描到的package只在src/main目录下,
		  test目录的测试package扫描不到,故test文件夹下的class使用@Service注解无
		  效率,而是采用 @ContextConfiguration({"classpath:spring/spring-dao.xml"})
		  去寻找spring配置文件的location
		  
		>> @Resource   @Autowired 的区别 [bean匹配方式不同: byName  or  byType]
	
	5. Spring声明式事务 @Transactional
	  (1)在秒杀系统中，一个正常的事务 = 减库存 + 插入秒杀记录 ，而这两个操作在数据库中
	     可以是单独的事务行为，如果不采用Spring声明式事务来声明事务，若只执行了减库存事
		 务操作而在插入秒杀记录事务操作时发生异常，则会导致数据的不一致。采用Spring声明
		 式事务的注解方式来声明一个完整的事务，出错时可以进行回滚，数据保持一致。
		 
	  (2) 配置:
		<!-- 配置事务管理器, MyBatis默认使用的是JDBC的事务管理方法 -->
		<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
			<!-- 注入数据库连接池,
				编写时会出现提示红色的dataSource,因为当前的xml找不到该bean
				但是在运行时Spring会去加载spring-dao.xml容器,所以程序不会出错-->
			<property name="dataSource" ref="dataSource"/>
		</bean>
		<!-- 配置基于注解的声明式事务
			默认使用注解的方式来管理事务行为-->
		<tx:annotation-driven transaction-manager="transactionManager"/>
	
	6. MD5加密(了解盐值字符串)
		>> String md5 = DigestUtils.md5DigestAsHex(base.getBytes())
		>> MD5: 把明文内容按照某种规则生成128位的哈希值==32位的16进制数
		>> 了解MD5的的大致原理
			https://www.cnblogs.com/second-tomorrow/p/9129043.html
			https://www.cnblogs.com/qianjinyan/p/10216293.html
		>> 盐值: https://libuchao.com/2013/07/05/password-salt
	
	7. Enum的使用
	
	8. 集成测试中logback打印日志的应用

	
三、Java高并发秒杀API之Web层

	1. SpringMVC的执行流程: http://baijiahao.baidu.com/s?id=1599271835307699639&wfr=spider&for=pc
	
	2. SpringMVC跟Struts2的区别[入口、基于方法开发/类开发、值栈+OGNL / ModelAndView + JSTL]
	
	3. 学习classpath属性 : https://segmentfault.com/a/1190000015802324
	
	4. 关于Ajax:(在jsp中使用的script脚本function()函数就是ajax的体现)
		学习Ajax : https://www.runoob.com/ajax/ajax-tutorial.html
		· AJAX = Asynchronous JavaScript and XML（异步的 JavaScript 和 XML）。
		· AJAX 不是新的编程语言，而是一种使用现有标准的新方法。
		· AJAX 最大的优点是在不重新加载整个页面的情况下，可以与服务器交换数据并更新部分网页内容。
		· AJAX 不需要任何浏览器插件，但需要用户允许JavaScript在浏览器上执行。
		
	5. 关于Restful风格知多少 ：https://www.jianshu.com/p/91600da4df95
	
	6. URL   VS   URI : https://www.cnblogs.com/wuyun-blog/p/5706703.html
	
	7. 幂等性  vs  非幂等性 : 
			https://blog.csdn.net/SasukeN/article/details/80919889?utm_source=blogxgwz2
	
	8. Restful规范:
		· GET --> 查询操作
		· POST --> 添加/修改操作
			<!-- POST 和 PUT 的区别:体现在幂等性跟非幂等性的差别，区分不大 -->
		· PUT --> 修改操作
		· DELETE --> 删除操作
		
	9. SpringMVC中URL的设计规范:
		/模块/资源/{标识}/集合1/集合2/...
		/user/{uid}/friends --> 好友列表
		/user/{uid}/followers -->关注者列表
		
	10. json是干吗用的 ? https://baike.baidu.com/item/JSON/2462549?fr=aladdin
	   @RequestMapping中的produces属性 ： https://blog.51cto.com/wangguangshuo/2047667
	
	11. 学习ResponseBody注解 : 
		>> 返回的是对象(json / xml 的数据对象)
		>> 不再走ViewResolver
		>> 直接写入到输入流中,response的body区
		http://www.cnblogs.com/qiankun-site/p/5774325.html
		
	
	12. @RequestMapping参数学习:
		https://blog.csdn.net/weixin_43453386/article/details/83419060#1value_26
		
	13. Long  VS  long ： https://www.cnblogs.com/likun10579/p/5846686.html
	
	14. Ajax接口 ？ json数据的封装是怎么完成的 ？ 
	
	15. Tomcat部署时的BUG:
		(1) web.xml中匹配的路径/*有问题-->关于Tomcat部署Maven-Web项目的步骤学习
			web.xml匹配路径<url-pattern>时的 '/*' 和 '/' 的区别: 
				https://www.cnblogs.com/zuojunyuan/p/6440055.html
		
		(2) EL表达式的使用问题
		
	16. Jquery的JS引入需要在其他JS前引入,否则会出现类似显示不了弹出框的情况
	
	17. 关于 isNaN(number) : number是数字返回false,非数字返回true !
	
	18. 服务器端的时间跟客户端的时间是不一样的！！！
		
		
三、Java高并发秒杀API之高并发优化

	1. 系统中会发生高并发访问的点/分析：获取系统时间、秒杀接口地址、执行秒杀操作
	
	2. 优化分析涉及知识点：CDN缓存、Redis缓存(重温redis的应用场景有哪些)
	
	3. 执行秒杀高级方案分析及其高成本分析 -- > (QPS、分布式MQ)
		(F:\Project_Space\Redis高并发秒杀系统\知识点捕捉图集)
		
	4. 为什么不使用 [ Spring声明式事务 + MySQL ] 来操作数据库 ?
		(1) java控制事务行为分析 ：(F:\Project_Space\Redis高并发秒杀系统\知识点捕捉图集)
		(2) 瓶颈分析：主要是网络延迟跟GC的时间浪费，不是MySQL数据库本身的执行慢
		(3) 优化分析：减少行级锁的持有时间，使得update操作尽快的commit/rollback
		
	5. 优化思路：把客户端的逻辑放到MySQL服务端，避免网络延迟和GC
		>> 怎么放？
		>> 存储过程，让MySQL服务端来控制事务而不是客户端来控制事务，因为
		>> MySQL是很高效的
		
	6. 优化总结
		>> 前端控制：暴露接口、按钮防重复
		>> 动静态数据分离：CDN缓存、后端redis缓存
		>> 事务竞争优化：减少事务锁时间[MySQL控制事务 + 存储过程]
		
	7. protostuff反序列化插件的使用&优点分析(节省序列化的空间 + 序列化速度快)
	
	8. RuntimeSchema
	
	9. Redis的连接超时BUG：0.0.0.0
	
	10. 将Redis中数据同步到MySQL中
	
	11. >> 秒杀操作优化1：减少行级锁持有时间-->调换update和insert的执行顺序，
		   insert的时候不需要获取行级锁，挡住了一些重复的秒杀单，使行级锁的
		   持有时间从之前的2倍转换为1倍(依然由spring控制事务，什么时候开始rollback ???)
		   
		(深度优化：SQL在MySQL端执行 + 存储过程)
		>> 秒杀操作优化2：调用存储过程
		
	12. delimiter $$
		>> https://www.cnblogs.com/rootq/archive/2009/05/27/1490523.html
	
	13. 为什么不将秒杀的对MySQL的操作[update + insert]放到redis中执行？
	
	14. 项目中没有显示用到多线程的地方
		
	