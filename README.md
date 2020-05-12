# Survey biz component
background survey components which use urocissa rpc middleware was all designed and coded by liujun.

server layer:accept survey task from outside and send subtask to middle layer.
middle layer:accept sub task and choose a client, then send it to the client who will do this job.
              job include online and offline subtask(survey by person from telepnone survey center).
client layer:accept sub task and finish it,then send the status of it to middle.

# **背调业务组件**   ----由LIUJUN一人设计开发

## 1. 业务目标：
   - 接收用户请求背调数据，产生任务对象
   - 进行持久化，并放入map池中
   - 拆分任务对象为多个子任务对象
   - 组成线程任务，提交线程池
   - 线程执行，按策略选择可用子任务处理服务，并发送过去
   - 异步接收处理结果，持久化并更新map池中任务，如果任务的所有在线子任务都完成，就从map中清除。
   
## 2. 功能特点：
   - 通讯底层使用公司的基于Netty，自定义协议的通讯组件urocissa
   - 子任务服务自动注册到中心，心跳子任务状态与配置，策略RoundRobin或者weight来选择
   - 中心分配任务有特殊线程进行重试与超时控制
   - 线程池拒绝服务可设置接收暂停，任务流量控制
   - 项目打包成maven的jar包，在Web项目pom中引入使用
   - 由web应用spring容器类提供持久化工具配置中心接口，与子任务处理接口的真正处理类
   - 由web应用的spring的生命周期类，控制中心启动与停止
   - 由web应用提供用户提供的背调数据，未来也可用其它协议
   
## 3. 进一步优化：
   此版本已经正式使用,但并非优化版本。后来进一步优化没包含在内，包括请求持久化从web移到组件内，类更规范，配置化参数，分发可控制暂停接收请求，容器启停接受spring生命周期管理，实时监控，但总体功能没大变化。
   **未来计划进一步整合进spring，实现自动配置，并在SmartLifecycle中实现启动与停止。当前是自定义一个@Component类实现InitializingBean与，autowire了所有组件要用的外部接口实现，afterPropertiesSet中配置给组件。在init中启动了本组件。**
   
   
## 4. 参与通讯中间件优化：
   在开发此组件过程中，对公司的通讯组件urocissa，我也进行了修改。包括：
   - 业务心跳合并到底层心跳，提供心跳数据采集接口
   - 异步futurn，在get外，增加异步回调设置
   - 通讯层不仅可以注册处理者processor，还可以设置对应的线程池，这样个性化线程名称与阻塞队列容量。（模仿rocketmq）
   - 原组件的加密的是协议中body部分，是msg->java序列化->byte[]->des加密->byte[]->marshaller写byteBuf过程，我将java序列化改为hassian.

## 5. 核心类：
   复杂的类在如下包中：
   - survey-server/src/main/java/com/sanyinggroup/corp/survey/server/container/
   - survey-middle/src/main/java/com/sanyinggroup/corp/survey/middle/container/
