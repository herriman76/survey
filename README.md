# Survey biz component
background survey components which use urocissa rpc middleware was all designed and coded by liujun.

server layer:accept survey task from outside and send subtask to middle layer.
middle layer:accept sub task and choose a client, then send it to the client who will do this job.
              job include online and offline subtask(survey by person from telepnone survey center).
client layer:accept sub task and finish it,then send the status of it to middle.

# **背调业务组件**（初始版本）

   序：由LIUJUN一人设计开发，第一次写组件，初版demo一周完工，现在看着也许蛮差的，也有不少优化没落实。当时学习的源码也少，还有其它很多开发任务，业务上的频繁变更，指导中底级开发，无心把这个不断完善，只能功能变更可用。

## 1. 业务目标：
   - 接收用户请求背调数据，产生任务对象
   - 进行持久化，并放入map池中
   - 拆分任务对象为多个子任务对象
   - 组成线程任务，提交线程池
   - 线程执行，按策略选择可用子任务处理服务，并发送过去
   - 异步接收处理结果，持久化并更新map池中任务，如果任务的所有在线子任务都完成，就从map中清除。
   
## 2. 功能特点：
   - 通讯底层使用公司的基于Netty，自定义协议的通讯组件urocissa，心跳同步处理，消息结果异步回调处理。
   - 子任务服务自动注册到中心，心跳子任务状态与配置，策略RoundRobin或者weight来选择
   - 中心分配任务有特殊线程进行重试与超时控制
   - 线程池拒绝服务可设置接收暂停，任务流量控制
   - 项目打包成maven的jar包，在Web项目pom中引入使用
   - 由web应用spring容器类提供持久化工具配置中心接口，与子任务处理接口的真正处理类
   - 由web应用的spring的生命周期类，控制中心启动与停止
   - 由web应用提供用户提供的背调数据，未来也可用其它协议
   - 接收外部请求采用waL思路存库再内存处理，守护线程可提取并处理系统崩溃重启后的数据。
   
## 3. 优化：
### 3.1 部分实施中
   此版本已经正式使用,但并非优化版本。后来进一步优化没包含在内，包括请求持久化从web移到组件内，类更规范，配置化参数，分发可控制暂停接收请求，容器启停接受spring生命周期管理，实时监控，但总体功能没大变化。
   
### 3.2 未来计划
   - **进一步整合进spring，实现自动配置，并在SmartLifecycle中实现启动与停止。当前是自定义一个@Component类实现InitializingBean与，autowire了所有组件要用的外部接口实现，afterPropertiesSet中配置给组件。在init中启动了本组件。**
   - 等通讯中间件进一步优化后（实现不同消息handler根据类型设置个性化的线程池）后，业务组件可以按业务量使用多个线程池按类型分发消息及处理返回值。
   - 等需要背调中心扩容时，注册功能也许需要单独考虑了,并且注册中心高可用，一般用zookeeper按类型建临时节点方式监控子应用。但本应用的子应用全量注册到背调中心，其实自身实现注册就可以了。

## 4. 核心类：
   最复杂的类在*.container包中，一般我会设计一个核心类统领全局(类似rocketmq中的controller类)，核心类的基本结构与考量如下：
   - 自身可能是工厂产生，或者是单例对象
   - 它持有其它功能类，其它功能类一般不相互引用，但它与功能类可相互引用
   - 包含多个线程，工作线程与自身守护线程，可能需要锁进行同步，Synchronized优化偏向锁，自旋锁，锁再升级，性能可以的
   - 包含共享变量，或者共享数据容器，注意volitile/concurrent/atom使用
   - 自身产生的同类对象享元模式存放，一般static map
   - 持有其它重要功能类，可能会被设置回调类，让核心类感知变化
   - 有生命周期，优雅的启动与关闭
   - 自身持有的重要类可替换，如果是策略模式，加载根据情况，可通过spi，或者awarespring容器，或者工厂，或者配置参数，或者如dubbo的extension。
   
## 5. 参与所使用的通讯中间件优化：
   在开发此组件过程中，对公司的通讯组件urocissa，我也进行了修改。包括：
   - 业务心跳合并到底层心跳，提供心跳数据采集接口，因为当前业务处理情况也要上报，还有可能其它状态数据。
   - 异步futurn，在轮询结果并get外，增加异步回调设置
   - 通讯层不仅可以注册处理者processor，还可以设置对应的线程池，这样个性化线程名称与阻塞队列容量。（模仿rocketmq）
   - 原组件的加密的是协议中body部分，是msg->java序列化->byte[]->des加密->byte[]->marshaller写byteBuf过程，居然中间出现了低效的Jdk序列化，我将java序列化改为hassian.

