# Survey biz component
background survey core code via urocissa rpc service which was finished by liujun all.

server layer:accept survey task from outside and send subtask to middle layer.
middle layer:accept sub task and choose a client, then send it to the client who will do this job.
              job include online and offline subtask(survey by person from telepnone survey center).
client layer:accept sub task and finish it,then send the status of it to middle.

【remark】
1。以下所有功能全部由作者完成。功能图见：survey_profile.png & survey_detail.png。最初的服务端客户端一周完成，后面多层次，多策略，多任务类型改进。消息通讯用到了以同事为主开发的消息中间件(RPC)工具urocissa，我分析了其源码，并根据使用中发现问题，也对urocissa进行了修改。

2。此项目接收外部的总任务请求，进行持久化（spring实现接口类并注入）后，拆分成各种子任务发给执行某类型任务的中间服务应用（对上是客户，以下是服务），中间服务再选择执行这种子任务的可用的客户端进行发送，发异步消息，并同步等待online任务的结果。最后online任务执行情况反馈到server端进行执久化，中间层对于offline任务（人工背调子任务）只发送即可返回。

3。项目打包成maven的jar包，用在背调Web项目中，由Web项目中的spring组件启动，并传入实现接口的外部类

4。难点：涉及到各层客户端的在线状态的维护与监听网络连接；总任务拆分与多线程处理；子任务的重试次数时间控制；客户端RoundRobin或者weight选择策略；不同任务类型记数记时；对外提供人工干预子任务的接口；提供了各层客户端的业务心跳（业务数据）
比如复杂的类在如下包中：
survey-server/src/main/java/com/sanyinggroup/corp/survey/server/container/
survey-middle/src/main/java/com/sanyinggroup/corp/survey/middle/container/
