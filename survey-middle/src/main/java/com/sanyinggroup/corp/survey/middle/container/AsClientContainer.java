package com.sanyinggroup.corp.survey.middle.container;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sanyinggroup.corp.survey.middle.Constants;
import com.sanyinggroup.corp.survey.middle.DateFormatInterface;
import com.sanyinggroup.corp.survey.middle.MiddleConfig;
import com.sanyinggroup.corp.survey.middle.OfflineSendInterface;
import com.sanyinggroup.corp.survey.middle.SurveyException;
import com.sanyinggroup.corp.survey.middle.handle.MiddlePushClientTaskCallback;
import com.sanyinggroup.corp.survey.middle.handle.MiddleRecvServerPushFeedbackHandler;
import com.sanyinggroup.corp.survey.middle.handle.ReportClientInfoFeedbackHandler;
import com.sanyinggroup.corp.survey.middle.handle.ReportSubtaskInfoFeedbackHandler;
import com.sanyinggroup.corp.survey.middle.model.ClientRealData;
import com.sanyinggroup.corp.survey.middle.model.CommonReturnData;
import com.sanyinggroup.corp.survey.middle.model.PushFuture;
import com.sanyinggroup.corp.survey.middle.model.SurveyClient;
import com.sanyinggroup.corp.survey.middle.model.SyncPushFuture;
import com.sanyinggroup.corp.survey.middle.tools.JsonUtils;
import com.sanyinggroup.corp.urocissa.client.api.handler.PushReceiverCenter;
import com.sanyinggroup.corp.urocissa.client.init.Client;
import com.sanyinggroup.corp.urocissa.client.init.ClientCenter;
import com.sanyinggroup.corp.urocissa.client.model.MiddleMsg;
import com.sanyinggroup.corp.urocissa.client.model.ResultObject;
import com.sanyinggroup.corp.urocissa.server.api.handler.ServerPushHandler;

/**
 * 背调中间层容器-管理通讯层并处理背调任务
 * @author liujun
 *
 */
public class AsClientContainer {

	private static final Logger logger = Logger.getLogger(AsClientContainer.class);

	public static final long THEARTBEAT_INTERVAL = 3 * 1000L;
	
	private static JSONArray REMORTSERVERS=new JSONArray();
	
	public static Map<String,JSONObject> REMORTSERVERS_MAP =new HashMap<String,JSONObject>();
	
//	getAClient(String ip, int port, String appKey, String appSecret, Class<? extends AbstractEventHandler> eventHandlerClass)

//	static{
////		JSONObject serverJson = new JSONObject();
////		serverJson.put("ip", "192.168.117.35");
////		serverJson.put("port", "9166");
////		serverJson.put("appKey", "5732f9a0bb7348258cfe53fa0160162d");
////		serverJson.put("appSecret","eeeeewerw34rwer");
////		REMORTSERVERS.add(serverJson);
//		REMORTSERVERS=JSONArray.fromObject(MiddleConfig.getServers());
//	}
	/*子任务存放*/
//	public volatile static Map<String, JSONObject> subTaskInfoMap = new ConcurrentHashMap<String, JSONObject>();
	/**使用阻塞队列，放置所有要处理的子任务*/
	public volatile static BlockingQueue<JSONObject> subTaskInfoQueue = new LinkedBlockingQueue<JSONObject>();
	/**
	 * 需要人工干预的子任务池
	 */
	public volatile static Map<String, JSONObject> subTaskInfoMap = new ConcurrentHashMap<String,JSONObject>();
	/*同步对象存放*/
	public volatile static Map<String, PushFuture<CommonReturnData>> syncKey = new ConcurrentHashMap<String, PushFuture<CommonReturnData>>();
	
	/**用户单例线程锁*/
	private static Boolean lockSigleton = true;
	/**用户单例对象*/
	private static AsClientContainer clientContainer;

	public TaskConsumer taskConsumer = new TaskConsumer();
	
	public TimeoutSubTask timeoutSubTask = new TimeoutSubTask();
	
	public OfflineSendInterface offlineSendInterface;
	
	public DateFormatInterface dateFormatInterface;
	
	/** 失败的任务用线程池 */
	private static ExecutorService executor = Executors.newCachedThreadPool();
	
	private ScheduledExecutorService executorTimeout = Executors.newScheduledThreadPool(1);
	
	public static AtomicLong                 subTaskNum                = new AtomicLong();
	
	/**
	 * 初始化客户端容器
	 * <P></P>
	 * @param clientCode
	 * @param appkey
	 * @param weight
	 * @param description
	 * @param apiInvorkerInterface
	 * @return
	 */
	public static AsClientContainer getInstance() {
		logger.info("【客户端容器】获取单例中...");
		if (clientContainer == null) {
			logger.info("【客户端容器】需要启动中...");
			synchronized (lockSigleton) {
				if (clientContainer == null) {
					clientContainer = new AsClientContainer();
				}
			}
			clientContainer.initConnections();
			clientContainer.registSubTaskDealClass();
			clientContainer.initTaskComsuptionThread();
		}
		logger.info("【客户端容器】启动完成...");
		return clientContainer;
	}


	/**
	 * 注册一个接收服务器推送信息处理的类
	 * 处理后的消息会自动发回给服务器
	 * <P></P>
	 */
	public void registSubTaskDealClass() {
		try {
			PushReceiverCenter.registReceiver("assignTaskToClient", new MiddleRecvServerPushFeedbackHandler());
			//回头补充接收推送后的直接回复消息。
//			PushReceiverCenter.registReceiver(actionName, receiver)	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 启动客户端的状态上报心跳
	 */
	public synchronized void initConnections() {
		logger.info("【初始化连接】连接...");
		for(int i=0;i<REMORTSERVERS.size();i++){
			JSONObject ser=(JSONObject) REMORTSERVERS.get(i);
			logger.info("【初始化连接】ServerJasonPara:"+ser.toString());
			REMORTSERVERS_MAP.put(ser.getString("ip")+"_"+ser.getInt("port"), ser);
			logger.debug(i+"【初始化连接】连接..."+ser.toString());
			Client client=ClientCenter.getAClient(ser.getString("ip"), ser.getInt("port"), ser.getString("appKey"),ser.getString("appSecret"),null);
			if (client == null || client.getResult() == null || client.getResult().getStatus() != 200) {
				logger.warn("【初始化连接】连接服务端失败了。"+ser.getString("ip"));
			}
			else{
				logger.warn("【初始化连接】连接服务端OK。"+ser.getString("ip"));
			}
		}
	}
	/**
	 * 客户端容器的构造函数
	 * @param serverId
	 * @param clientCode
	 * @param appkey
	 * @param appSecret
	 * @param weight
	 * @param description
	 * @param apiInvorkerInterface
	 */
	private AsClientContainer(){

	}
	
	
	
	public static JSONArray getREMORTSERVERS() {
		return REMORTSERVERS;
	}


	public static void setREMORTSERVERS(JSONArray rEMORTSERVERS) {
		REMORTSERVERS = rEMORTSERVERS;
	}




	public OfflineSendInterface getOfflineSendInterface() {
		return offlineSendInterface;
	}


	public void setOfflineSendInterface(OfflineSendInterface offlineSendInterface) {
		this.offlineSendInterface = offlineSendInterface;
	}




	public DateFormatInterface getDateFormatInterface() {
		return dateFormatInterface;
	}


	public void setDateFormatInterface(DateFormatInterface dateFormatInterface) {
		this.dateFormatInterface = dateFormatInterface;
	}




	/**
	 * <P>用于处理阻塞对列里的子任务，再扔进线程池。</P>
	 * @author liujun
	 * @date 2018年1月22日 上午9:59:10
	 */
	private class TaskConsumer implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					//因为这个队列中都是出问题的子任务，所以要等待一下处理。
					Thread.sleep(1500);// 调节频率，过快容易撑死~~
//					logger.debug("【子任务队列】的任务数1：" + taskNum);
					JSONObject subtaskInfo=subTaskInfoQueue.take();
					logger.debug("【子任务队列消费】取出子任务JASON："+subtaskInfo.toString());
					
					String subTaskType=subtaskInfo.containsKey("subTaskType")?subtaskInfo.getString("subTaskType"):null;
					//！！！！！！检查这个子任务是还否可以复制之前的结果，如果可以就复制出来，返回一个成功的结果。
					//这里交冯实现的接口，由于工作变动，还没出来。
					
					
					
					
					
//					logger.debug("【子任务队列】的任务数2：" + subTaskInfoQueue.size());
					//如果重试了50次或者超时了5分钟，那么子任务失败吧
					long reDoTime=new Date().getTime()-subtaskInfo.getLong("startDate");
					logger.debug("reDoTime:"+reDoTime+"。testnum:"+subtaskInfo.getInt("testNum"));
					if(subtaskInfo.getInt("testNum")>Integer.parseInt(MiddleConfig.getFailureSubTaskMaxRetryTimes()) || (reDoTime>Long.parseLong(MiddleConfig.getFailureSubTaskMaxRetryTimes())) ){
						logger.debug("【子任务队列消费】子任务超时失败："+subtaskInfo.getString("subTaskId"));
						logger.debug("【子任务队列消费】子任务超时失败，尝试次数为："+subtaskInfo.getInt("testNum"));
						
						AsClientContainer.subTaskInfoMap.remove(subtaskInfo.getString("subTaskId"));
						
						//通用处理完成或者失败的子任务
						Thread.sleep(1000);// 调节频率，过快容易撑死~~
						String isAsync=subtaskInfo.containsKey("isAsync")? subtaskInfo.getString("isAsync"):null;
						//1.【推失败了，如果是异步的，就发消息给服务端】
						if("async".equals(isAsync) || !Constants.TASK_TYPE_ONLINE.equals(subTaskType)){
//							AsClientContainer.sendTaskAsyncResult2Server(asyncServerCode,finishJason);
							//如果异步调用失败。
							logger.debug("【推送异步任务】超时了，发消息给服务器");
							JSONObject finishJason=new JSONObject();
							finishJason.put("code", "failure");
							finishJason.put("msg", "OFFLINE_RPC_FAIL中间层任务调用C端失败");
//							JSONObject finishData=new JSONObject();
//							finishData.put("taskId", subtaskInfo.getString("taskId"));
//							finishData.put("subTaskId", subtaskInfo.getString("subTaskId"));
//							finishData.put("remark", "重试了"+subtaskInfo.getInt("testNum")+"次,用时"+reDoTime+"ms");
							finishJason.put("data", subtaskInfo);//这里面有ip/port用于异步。
							
							
							//如果是线下的推送或者调用失败了，只持久化到本地，再重试。或者超时。不可以迅速返回失败的。
							sendTaskAsyncResult2Server(finishJason);
							return;

						}
						//2.【如果是线上任务推送失败，设置同步等待对象。】
						JSONObject finishJason=new JSONObject();
						finishJason.put("code", "failure");
						finishJason.put("msg", "中间层任务推送失败");
						JSONObject finishData=new JSONObject();
						finishData.put("taskId", subtaskInfo.getString("taskId"));
						finishData.put("subTaskId", subtaskInfo.getString("subTaskId"));
						finishData.put("remark", "重试了"+subtaskInfo.getInt("testNum")+"次,用时"+reDoTime+"ms");
						finishJason.put("data", finishData);
						PushFuture<CommonReturnData> responseFuture = AsClientContainer.syncKey.get(subtaskInfo.getString("subTaskId"));
						if(responseFuture!=null){
							CommonReturnData response=new CommonReturnData();
							response.setCode("failure");
							response.setMsg("任务超时失败");
							response.setData(finishData);
							responseFuture.setResponse(response);
							logger.debug("【推送任务任务】超时了，设置同步对象的返回值");
						}
						else{
							logger.debug("【推送任务任务】设置超时时，同步对象已经被移除。");
						}
					}
					else//如果是正常处理子任务
					{
						//如果非线上任务，就走外部接口（注入的实现类）发出去（实现类会持久化，再发的）
						logger.debug("subTaskType:"+subTaskType+"。offlineSendInterface:"+offlineSendInterface);
						if(!Constants.TASK_TYPE_ONLINE.equals(subTaskType)){
							try {
								if(offlineSendInterface!=null){
									offlineSendInterface.sendOfflineQuery(subtaskInfo);
								}else{
									logger.warn("【找不到外部（非线上子任务）调用的接口】");
									throw new SurveyException("找不到外部（非线上子任务）调用的接口实现类");
								}
								logger.info("【推送任务任务】成功推送非线上任务到C端！");
							} catch (SurveyException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								logger.warn("【推送任务任务】推送非线上任务到C端失败！");
								ReDoTask reDoTask = new ReDoTask(subtaskInfo);
								executor.submit(reDoTask);
							}
						} 
						else //如果是线上的，就推送出去。
						{
							SurveyClient surveyClient = AsServerContainer.getClientByUserRankAndClinetLever(0);
							// 如果找到策略的客户端
							if (surveyClient != null && surveyClient.getSessionId() != null) {
								// 开始推送
								String body = JsonUtils.toString(subtaskInfo);
								String clientSessionId = surveyClient.getSessionId();
								logger.info("【推送任务任务】推送目标sissionId:" + clientSessionId);
								boolean bln = ServerPushHandler.pushBySessionId(clientSessionId, "assignTaskToClient", body, new MiddlePushClientTaskCallback());
								logger.info("【推送任务任务】bln:" + bln);
								if (bln) {
									logger.info("【推送任务任务】成功！");
									// 推成功了，但一直不返回，也是个问题。不过同步对象会被移除的。

								} else {
									logger.warn("【推送任务任务】推送失败！");
									ReDoTask reDoTask = new ReDoTask(subtaskInfo);
									executor.submit(reDoTask);
								}
							} else {// 没有可用的客户端
								logger.info("【推送任务任务】bln:没有可用的客户端，直接失败返回。次数：" + subtaskInfo.getInt("testNum"));
								ReDoTask reDoTask = new ReDoTask(subtaskInfo);
								executor.submit(reDoTask);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	public void initTaskComsuptionThread() {
		Thread taskComsuptionThread = new Thread(taskConsumer);
		taskComsuptionThread.setName("TaskComsuptionThread");
		taskComsuptionThread.start();
		//处理超时
		executorTimeout.scheduleAtFixedRate(timeoutSubTask,10,10,TimeUnit.MINUTES);
	}
	
	/**
	 * 获取本机的Ip地址
	 * <P></P>
	 * @return
	 */
	private static String getIpAddress() {
		InetAddress address;
		try {
			address = InetAddress.getLocalHost();
			return address.getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * 异步返回服务器结果时，发通讯消息.
	 * @param asyncServerCode
	 * @param taskFinishInfo
	 */
	@Deprecated
	public static void sendTaskAsyncResult2Server(String asyncServerCode,JSONObject taskFinishInfo ) {
		JSONObject ser =AsClientContainer.REMORTSERVERS_MAP.get(asyncServerCode);
		Client client=ClientCenter.getAClient(ser.getString("ip"), ser.getInt("port"), ser.getString("appKey"),ser.getString("appSecret"));
		logger.info("【子任务完成上报】准备上报异步任务完成给："+ser.getString("ip"));
		if (client == null || client.getResult() == null || client.getResult().getStatus() != 200) {
			logger.warn("【子任务完成上报】初始化时连接服务端失败了。"+ser.getString("ip"));
		}
		else{
			com.sanyinggroup.corp.urocissa.client.model.MiddleMsg subTaskMsg = new com.sanyinggroup.corp.urocissa.client.model.MiddleMsg("subTaskFinishInfo", taskFinishInfo.toString());
			ResultObject res = client.sendMsg(subTaskMsg, new ReportSubtaskInfoFeedbackHandler());
			if (res.getStatus() != 200) {// 表示客户端发送的时候发送失败
				logger.info("【守护线程】上报状态FAILURE");
			}
			logger.info("【守护线程】上报状态OK");
		}
	}
	
	
	/**
	 * 返回的结果中有发出任务服务器信息，按这个异步通知
	 * *****未来查驾照的情况，只是C端补了驾驶证信息，这里收到，或者自己调用接口完成，再上报。或者再扔进队列处理。
	 * *****队列处理不仅判断任务类型，还要判断任务完整程度，完整了推下去子任务做，不完整的发C端补。
	 * @param taskFinishInfo
	 * @throws SurveyException 
	 */
	public void sendTaskAsyncResult2Server(JSONObject taskFinishInfo ) throws SurveyException {
		logger.debug("【上报异步结果】taskFinishInfo："+taskFinishInfo.toString());

		//1.存结果
		if(offlineSendInterface!=null){
			try {
				offlineSendInterface.saveOfflineQuery(taskFinishInfo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("【C端返回数据持久化接口保存数据失败】"+e.toString());
			}
		}else{
			logger.warn("【找不到外部（非线上子任务）调用的接口实现类】");
//			throw new SurveyException("找不到外部（非线上子任务）调用的接口实现类");
		}
//		finishJason.put("msg", "OFFLINE_RPC_FAIL中间层任务调用C端失败");
		if(taskFinishInfo.getString("msg").startsWith("OFFLINE_RPC_FAIL")){
			logger.warn("【（非线上子任务）调用C端RPC失败】，需要定时重试或者超时，不可迅速失败。");
			return ;
		}
		
//		String code = taskFinishInfo.getString("code");
//		String msg = taskFinishInfo.getString("msg");
//		String taskId = taskFinishInfo.getJSONObject("data").getString("taskId");
//		String subTaskId = taskFinishInfo.getJSONObject("data").getString("subTaskId");
//		String remark = (taskFinishInfo.getJSONObject("data").containsKey("remark"))? taskFinishInfo.getJSONObject("data").getString("remark"):"";
		String serverIp = (taskFinishInfo.getJSONObject("data").containsKey("serverIp"))? taskFinishInfo.getJSONObject("data").getString("serverIp"):"";
		String serverPort = (taskFinishInfo.getJSONObject("data").containsKey("serverPort"))? taskFinishInfo.getJSONObject("data").getString("serverPort"):"";
		
//		String serverIp=taskFinishInfo.containsKey("serverIp")?taskFinishInfo.getString("serverIp"):null;
//		String serverPort=taskFinishInfo.containsKey("serverPort")?taskFinishInfo.getString("serverPort"):null;
		if(StringUtils.isBlank(serverIp)|| StringUtils.isBlank(serverPort)){
			logger.warn("【上报异步结果】找不到结果信息中的服务器信息，无法异步反馈结果");
			throw new SurveyException("找不到结果信息中的服务器信息，无法异步反馈结果");
		}
		logger.debug("【上报异步结果】给："+serverIp+":"+serverPort);
		JSONObject ser =AsClientContainer.REMORTSERVERS_MAP.get(serverIp+"_"+serverPort);
		Client client=ClientCenter.getAClient(ser.getString("ip"), ser.getInt("port"), ser.getString("appKey"),ser.getString("appSecret"));
		logger.info("【子任务完成上报】准备上报异步任务完成给："+ser.getString("ip"));
		if (client == null || client.getResult() == null || client.getResult().getStatus() != 200) {
			logger.warn("【子任务完成上报】初始化时连接服务端失败了。"+ser.getString("ip"));
			throw new SurveyException("客户端client状态异常：("+ser.getString("ip")+")"+client);
		}
		else{
			MiddleMsg subTaskMsg = new MiddleMsg("subTaskFinishInfo", taskFinishInfo.toString());
			ResultObject res = client.sendMsg(subTaskMsg, new ReportSubtaskInfoFeedbackHandler());
			if (res.getStatus() != 200) {// 表示客户端发送的时候发送失败
				logger.info("【守护线程】上报状态FAILURE");
				throw new SurveyException("客户端client发送失败，状态码:"+res.getStatus());
			}
			logger.info("【守护线程】上报状态OK");
		}
	}
	
	/**
	 * 处理问题子任务的线程池
	 * @author liujun
	 *
	 */
	static class ReDoTask implements Runnable {
		private JSONObject subTaskInfo;

		public ReDoTask(JSONObject subTaskInfo) {
			this.subTaskInfo = subTaskInfo;
		}

		@Override
		public void run() {
			 logger.info("【等待后再次扔进队列处理】任务执行...");
			 subTaskInfo.put("testNum", subTaskInfo.getInt("testNum")+1);//失败次数+1
			try {
				Thread.sleep(5000);
				subTaskInfoQueue.put(subTaskInfo);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.warn("【等待后再次扔进队列处理】线程异常..."+e.toString());
			}// 调节频率，过快容易撑死~~
		}
	}
	
//	JSONArray dealTimeoutDbData()
	
	private class TimeoutSubTask implements Runnable {

		@Override
		public void run() {
			logger.info("【定时处理超时任务】任务执行...");
			if (offlineSendInterface != null) {
				logger.debug("【定时处理超时任务】的接口实现，可以进行...");
				JSONArray subArray = offlineSendInterface.dealTimeoutDbData();
				for (int i = 0; i < subArray.size(); i++) {
					// 遍历 jsonarray 数组，把每一个对象转成 json 对象
					JSONObject subTaskInfo = subArray.getJSONObject(i);
					logger.info("【定时处理超时任务】取出的任务请求为："+subTaskInfo.toString());
					subTaskInfo.put("testNum", subTaskInfo.getInt("testNum") + 1);// 失败次数+1
					String subTaskType = subTaskInfo.containsKey("subTaskType") ? subTaskInfo.getString("subTaskType") : null;
					if (!Constants.TASK_TYPE_ONLINE.equals(subTaskType)) {
						try {
							// AsClientContainer.sendTaskAsyncResult2Server(asyncServerCode,finishJason);
							// 如果异步调用失败。
							logger.debug("【推送异步任务】超时了，发消息给服务器");
							JSONObject finishJason = new JSONObject();
							finishJason.put("code", "failure");
							finishJason.put("msg", "中间层任务调用C端超时失败");
							// JSONObject finishData=new JSONObject();
							// finishData.put("taskId",
							// subtaskInfo.getString("taskId"));
							// finishData.put("subTaskId",
							// subtaskInfo.getString("subTaskId"));
							// finishData.put("remark",
							// "重试了"+subtaskInfo.getInt("testNum")+"次,用时"+reDoTime+"ms");
							finishJason.put("data", subTaskInfo);// 这里面有ip/port用于异步。

							// 如果是线下的推送或者调用失败了，只持久化到本地，再重试。或者超时。不可以迅速返回失败的。
							sendTaskAsyncResult2Server(finishJason);
							return;

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							logger.warn("【等待后再次扔进队列处理】线程异常..." + e.toString());
						}// 调节频率，过快容易撑死~~
					}
				}
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		//初始化
		logger.info("start...");
		//读取配置
		MiddleConfig.init("");
		REMORTSERVERS=JSONArray.fromObject(MiddleConfig.getServers());
		
		//作为客户端启动，连接多个服务
		AsClientContainer.getInstance();
		//启动守护线程显示容器内的信息，并测试执行默认的任务
		startDaemonThread();
		
		//作为服务端，启动监听
		AsServerContainer.getInstance();
		
	}
	
	/**
	 * 监控与业务心跳线程
	 */
	public static void startDaemonThread(){
		//设置守护线程，定时打出系统信息
		Thread daemon =new Thread(new Runnable() {
		    @Override
		    public void run() {
		        // TODO Auto-generated method stub
		    	try {
		    		for(;;){
		    			Thread.sleep(10000L);
		    			logger.debug("");logger.debug("");
		    			logger.debug("【守护线程-BEGIN】--------################################");
		    			
		    			//【正常业务数据心跳】(如果对不同的服务不同，再循环里处理)
		    			ClientRealData clientRealData=new ClientRealData();
		    			clientRealData.setClientCode(MiddleConfig.getClientCode());
		    			clientRealData.setAppkey(MiddleConfig.getAppKey());
//		    			clientRealData.setWeight(weight);
		    			clientRealData.setDescription(MiddleConfig.getClientDiscription());
//		    			clientRealData.setSiteIp(MiddleConfig.getServerIps());
//						clientRealData.setClientIp(MiddleConfig.getServerIps());
						clientRealData.setWeight(MiddleConfig.getWeight());
						clientRealData.setClientDescription(MiddleConfig.getClientDiscription());//这里可以设置实时动态信息
						try {
							String body = JsonUtils.toString(clientRealData);
							// 构建消息
							MiddleMsg msg = new MiddleMsg("getClientSystemInfo", body);
							
							for(int i=0;i<REMORTSERVERS.size();i++){
								JSONObject ser=(JSONObject) REMORTSERVERS.get(i);
								Client client=ClientCenter.getAClient(ser.getString("ip"), ser.getInt("port"), ser.getString("appKey"),ser.getString("appSecret"));
								logger.info("【守护线程】准备上报状态给："+ser.getString("ip"));
								if (client == null || client.getResult() == null || client.getResult().getStatus() != 200) {
									logger.warn("【守护线程】初始化时连接服务端失败了。"+ser.getString("ip"));
								}
								else{
									ResultObject res = client.sendMsg(msg, new ReportClientInfoFeedbackHandler());
									if (res.getStatus() != 200) {// 表示客户端发送的时候发送失败
										logger.info("【守护线程】上报状态FAILURE");
									}
									logger.info("【守护线程】上报状态OK");
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							logger.warn("【守护线程】业务心跳处理失败。"+e.toString());
						}
						
						
						//【输出系统信息】
						logger.info("【守护线程】----------------------------");
						logger.info("【守护线程】任务对列中的任务数:"+subTaskInfoQueue.size());
						logger.info("【守护线程】在线客户端数:----->>>>>----【"+AsServerContainer.onlineClientMap.size()+"】");
						logger.info("【守护线程】锁数:"+AsClientContainer.syncKey.size());
						logger.info("【守护线程】-----------------------------");
						logger.debug("【守护线程-END】--------################################");
		    			
						
						
						
		    			//【构造测试数据】
						if("true".equals(MiddleConfig.getUseTestDate())){
							JSONObject taskData = new JSONObject();
							taskData.put("taskId", "6666666666666666666666666");
							String subTaskId=UUID.randomUUID().toString().replaceAll("-", "");
							taskData.put("subTaskId", subTaskId);
							
							JSONObject queryJsonStr = new JSONObject();
							queryJsonStr.put("personIdCard","362401197507254012");
							queryJsonStr.put("personName","肖卿荣");
							queryJsonStr.put("personMobile","13916676701");
							taskData.put("queryJsonStr", queryJsonStr);
							
							
							taskData.put("testNum", 0);
							taskData.put("startDate", new Date().getTime());
							
							//1.扔到队列里处理
							subTaskInfoQueue.add(taskData);
							AsClientContainer.subTaskInfoMap.put(subTaskId, taskData);
							logger.info("【当前系统中已经接收的任务总数为】："+AsClientContainer.subTaskNum.incrementAndGet());
							//2.生成同步等待对象
							PushFuture<CommonReturnData> responseFuture = new SyncPushFuture(subTaskId);
							AsClientContainer.syncKey.put(subTaskId, responseFuture);
							
//		    				responseFuture=AsClientContainer.syncKey.get("aaaaaaaaa");
							logger.info("【任务时间--------------BEGIN】"+new Date().getTime());
							CommonReturnData commonReturnData=responseFuture.getResponse();
							logger.info("【任务时间-------------- END 】"+new Date().getTime());
							if(commonReturnData!=null){
								logger.info("【从同步对象中取值】RESUST:"+commonReturnData.getCode()+commonReturnData.getMsg());
							}
							else{
								logger.info("【从同步对象中取值】取不到值");
							}
							AsClientContainer.syncKey.remove(subTaskId);
						}
						
						

		    		}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		});
		daemon.setDaemon(true);
		daemon.setName("Biz Heartbeat Thread");
		daemon.start();
	}

}
