package com.sanyinggroup.corp.survey.server.container;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.sanyinggroup.corp.survey.server.SurveyException;
import com.sanyinggroup.corp.survey.server.TaskDealInterface;
import com.sanyinggroup.corp.survey.server.TaskPersistenceInterface;
import com.sanyinggroup.corp.survey.server.model.SubTaskInfo;
import com.sanyinggroup.corp.survey.server.model.SurveyApp;
import com.sanyinggroup.corp.survey.server.model.SurveyClient;
import com.sanyinggroup.corp.survey.server.model.TaskInfo;
import com.sanyinggroup.corp.urocissa.server.ServerInit;

/**
 * 背调中间件总容器，负责中间层的初始化与内部的App容器，Task容器的初始化
 * @author liujun
 *
 */
public class ContainerInit implements TaskDealInterface {

	/**单例线程锁*/
	private static Boolean lock = true; // 用户单例线程锁
	private static ContainerInit containerInit;
	/**任务容器*/
	public TaskContainer taskContainer;
	/**app容器*/
	public AppContainer appContainer;

	private static final Logger logger = Logger.getLogger(ContainerInit.class);

	public static ContainerInit init(List<SurveyApp> surveyApp,String ip,String port,TaskPersistenceInterface taskPersistenceInterface) {
		logger.info("任务处理模块启动中...");
		AppContainer.registeAppClientInfo(surveyApp);
		if (containerInit == null) {
			synchronized (lock) {
				if (containerInit == null) {
					containerInit = new ContainerInit();
					TaskContainer taskContainer = new TaskContainer();
					taskContainer.setTaskPersistenceInterface(taskPersistenceInterface);
					taskContainer.initTaskComsuptionThread();//启动任务池消费
					containerInit.taskContainer = taskContainer;
					
					AppContainer appContainer=AppContainer.init(taskContainer);
					appContainer.setIPPort(ip, port);
					appContainer.initMiddlerWareStart();
					containerInit.appContainer = appContainer;
				}
			}
//			if (!containerInit.appContainer.isMiddlewearStarted) {
//				logger.error("通讯中间件启动异常");
//				containerInit = null;
//			}
		}
		logger.info("任务处理模块启动完成...");
		startDaemonThread();
		return containerInit;
	}
	
	/**
	 * 开启守护线程，输出当前在线客户端
	 * <P></P>
	 */
	private static void startDaemonThread(){
		//设置守护线程，定时打出系统信息
		Thread daemon =new Thread(new Runnable() {
		    @Override
		    public void run() {
		        // TODO Auto-generated method stub
		    	try {
		    		for(;;){
		    			Thread.sleep(15000L);
		    			logger.debug("");logger.debug("");
		    			logger.debug("【守护线程-BEGIN】-----------------------------");
		    			
		    			//在线客户端情况
		    			logger.debug("【守护】----1.在线客户端---");
		    			Iterator iter = ContainerInit.getInstance().appContainer.onlineClientMap.entrySet().iterator();
		    			while (iter.hasNext()) {
		    				Map.Entry<String,  ConcurrentHashMap<String, SurveyClient>> entry = (Map.Entry<String,  ConcurrentHashMap<String, SurveyClient>>) iter.next();
		    				SurveyApp surveyApp=ContainerInit.getInstance().appContainer.appHolder.get(entry.getKey());
		    				String appCode=surveyApp.getCode();
		    				String appName=surveyApp.getName();
		    				String appKey=surveyApp.getAppKey();
		    				logger.debug("【守护】当前每个(APP的Code|名称|在线客户端数|key)："+appCode+"|"+appKey+"|-------|"+appName+"|"+entry.getValue().size());
		    			}
		    			
		    			logger.debug("【守护】----2.当前任务池中的任务---");
		    			//内存中的任务。正常情况下，下面的代码注释，因为可能太多任务。
		    			iter = ContainerInit.getInstance().taskContainer.taskInfoMap.entrySet().iterator();
		    			while (iter.hasNext()) {
		    				Map.Entry<String,TaskInfo> entry = (Map.Entry<String,TaskInfo>) iter.next();
		    				logger.debug("【守护】当前任务【ID|子任务数】："+entry.getKey()+"|"+entry.getValue().getSubTaskMap().size());
		    				ContainerInit.getInstance().taskContainer.dealTimeoutTask(entry.getValue());
		    			}
		    			
		    			logger.debug("【守护】----3.失败待处理对列---");
		    			//失败队列中的情况
		    			iter = ContainerInit.getInstance().taskContainer.subTaskInfoQueue.iterator();
		    			while (iter.hasNext()) {
		    				SubTaskInfo subTaskInfo = (SubTaskInfo) iter.next();
		    				logger.debug("【守护】当前队列中子任务ToString："+subTaskInfo.toString());
		    			}
		    			logger.debug("【守护】----3.失败待处理对列线程状态---");
		    			
		    			logger.debug("【守护线程-END】-----------------------------");
		    			logger.debug("");logger.debug("");
		    			
		    			
		    			
		    			//测试不断加入新的任务
//		    			ContainerInit.getInstance().putTask2Pool(null);
		    		}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.debug("【守护线程-END】异常：-----------------------------"+e.toString());
				}
		    }
		});
		daemon.setDaemon(true);
		daemon.setName("Biz Heartbeat Thread");
		daemon.start();
	}
	
	
	private ContainerInit(){
	}
	
	public static ContainerInit getInstance(){
		return containerInit;
	}

	
	/**
	 * 用户调用此方法，开始背调。标准请求进入
	 * 无论是多线程调用，还是单线程调用，都由内部的线程池分配子任务处理。
	 * （如果外部是Web调用(bio/nio)，本来就是多线程，是否可以不用线程池？可以一对一处理）
	 * 
	 * 1.所有的子任务都会进来推送
	 * 2.如果线上的都完成了，就移除，进行持久化，如果只有线上就报告。
	 * 3.如果线下的完成，直接接口调用数据库，进行持久化，并且都完成了就报告。
	 * 4.有线下的任务不需要在内存中长时间存在。
	 * 
	 * @see com.sanyinggroup.corp.magpie.bdcenter.logic.component.back.SurveyServerComponent
	 * 主任务参数结构如下：根据功能要求灵活设置jason，但规范性差，对修改人员要求高
	 * 	JSONObject jsonParam = new JSONObject();
		String taskId=Utils.getShortUUID();
		jsonParam.put("taskId",taskId);//设置总任务ID 
		jsonParam.put("investigateId", entity.getInvestigateId());
		jsonParam.put("type", entity.getType());
		//查询人公司信息
		jsonParam.put("userId", entity.getQueryUserId());
		jsonParam.put("userName", entity.getQueryPhone());
		jsonParam.put("companyId", entity.getCompanyId());
		jsonParam.put("companyName", entity.getCompanyName());
		//被查人信息
		jsonParam.put("personId", entity.getPersonId());
		jsonParam.put("personName", entity.getRealName());
		jsonParam.put("personIdCard", entity.getiCard());
		jsonParam.put("personMobile", entity.getTelephone());
		jsonParam.put("personPosition", entity.getPosition());
	 * 每个子任务的参数结构如下：
		JSONObject eachJ = new JSONObject();
		eachJ.put("subTaskId", Utils.getShortUUID());// 设置每个子任务ID
		
		eachJ.put("personIdCard", entity.getiCard());
		eachJ.put("personName", entity.getRealName());
		eachJ.put("personMobile", entity.getTelephone());
		//新加查询公司与账号信息，主要是Basic中存放，用于构建背调总jason用。
		eachJ.put("userId", entity.getQueryUserId());
		eachJ.put("userName", entity.getQueryPhone());
		eachJ.put("companyId", entity.getCompanyId());
		eachJ.put("companyName", entity.getCompanyName());
		eachJ.put("personPosition", entity.getPosition());
		
		eachJ.put("epVipRank", entity.getEpVIPRank());//用于客户端策略，这个只到服务端或者中间层。但为了兼容没有中间层，所以此参数也到子应用。
		eachJ.put("epArea", entity.getEpArea());//用于社保优先查询什么接口。这个参数要到相关的子应用
		
		eachJ.put("subProductType", code.getSubProductType());
		eachJ.put("subTaskCode", code.getSubProductCode());
		eachJ.put("subTaskName", code.getSubProductName());
		//下面是个性化的调查参数，如果是线上，目前可以不用。只用上面公共的数据，如姓名、身份证
		eachJ.put("subProductRequestPara", code.getSubProductRequestPara());
		jsonParam.put(code.getSubProductCode(), eachJ);// 对应的app
		
	 * 
	 * @throws SurveyException 
	 */
	@Override
	public void putTask2Pool(JSONObject jsonParam) throws SurveyException {
		// TODO Auto-generated method stub
		if (jsonParam == null) {
			logger.info("【发起任务】背调任务请求参数：" + jsonParam);
			return;
		}

		String queryJsonStr = jsonParam.toString();
		logger.info("【发起任务】背调任务请求参数：" + queryJsonStr);

		if (containerInit != null) {
			logger.info("---------------------------");
			logger.info("---------【用户发起背调了...】--------");
			TaskInfo taskInfo = TaskFactory.creatTaskInfo(jsonParam);
			taskContainer.createAndPutTaskPool(taskInfo);
		} else {
//			ContainerInit.init(null, null);
//			// 回调自己，等待
//			putTask2Pool(jsonParam);
			logger.warn("【发起任务】背调中心没有启动");
		}
	}
	
	
	/**
	 * 此方法是为了从数据库中恢复数据后调用。内存对象先开发，与数据库并不完全一致。
	 */
	@Override
	public void putTask2Pool(TaskInfo taskInfo) throws SurveyException {
		// TODO Auto-generated method stub
		if(taskInfo==null || taskInfo.getSubTaskMap()==null || taskInfo.getSubTaskMap().size()==0){
			logger.warn("【发起任务】背调任务对象为空,或者子任务空："+taskInfo);
			return;
		}
			
			logger.info("【发起任务】背调任务请求参数："+taskInfo.getTaskId());
			
	//		String[] queryAppCode ={"loan","dishonest"};
	//		String[] queryAppCode ={"loan"};
		
		if (containerInit != null){
			logger.info("---------------------------");
			logger.info("---------【用户发起背调了...】--------");
			taskContainer.createAndPutTaskPool(taskInfo);
		}
		else
		{
//			ContainerInit.init(null,null);
//			//回调自己，等待
//			putTask2Pool(taskInfo);
			logger.warn("【发起任务】背调中心没有启动");
		}
	}
	
	

	/**
	 * 系统测试用
	 * <P></P>
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		//初始化
		ContainerInit.init(null,null,null,null);
		//启动守护线程显示容器内的信息，并测试执行默认的任务
		startDaemonThread();
	}

}
