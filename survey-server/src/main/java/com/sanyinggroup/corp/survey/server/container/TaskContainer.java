package com.sanyinggroup.corp.survey.server.container;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.sanyinggroup.corp.survey.server.Constants;
import com.sanyinggroup.corp.survey.server.SubTaskFinishInterface;
import com.sanyinggroup.corp.survey.server.SurveyException;
import com.sanyinggroup.corp.survey.server.TaskPersistenceInterface;
import com.sanyinggroup.corp.survey.server.handler.ClientStatusListener;
import com.sanyinggroup.corp.survey.server.handler.ServerPushTaskCallback;
import com.sanyinggroup.corp.survey.server.model.SubTaskData;
import com.sanyinggroup.corp.survey.server.model.SubTaskInfo;
import com.sanyinggroup.corp.survey.server.model.SurveyApp;
import com.sanyinggroup.corp.survey.server.model.SurveyClient;
import com.sanyinggroup.corp.survey.server.model.TaskInfo;
import com.sanyinggroup.corp.survey.server.tools.JsonUtils;
import com.sanyinggroup.corp.urocissa.server.api.handler.ServerPushHandler;

/**
 * 任务容器-管理背调任务与相关子任务
 * @author liujun
 * @date 2018年1月18日 上午10:33:01
 */
public class TaskContainer {
	private static final Logger logger = Logger.getLogger(TaskContainer.class);

	/** 实时总任务信息(taskid---TaskInfo(SubTaskInfoMap)) */
	public volatile static Map<String, TaskInfo> taskInfoMap = new ConcurrentHashMap<String, TaskInfo>();

	/** 任务在内存中允许的最大存放数 */
	private static Integer maxTaskMapSize=Integer.MAX_VALUE;
	/**
	 * 实时子任务信息（subtaskid---SubTaskInfo）
	 * 目的：子任务完成后，根据subTaskId从这里快速拿到对应的子任务。从上面的主任务不方便找。
	 * 不需要了，子任务中带有主任务ID，所以还是先拿主任务，再取子任务处理。
	 */
//	public volatile static Map<String, SubTaskInfo> subTaskInfoMap = new ConcurrentHashMap<String, SubTaskInfo>();
	
	/**使用阻塞队列，放置所有要处理的失败子任务.失败的任务先会再回线程池，之后超时会触发返回*/
	BlockingQueue<SubTaskInfo> subTaskInfoQueue = new LinkedBlockingQueue<SubTaskInfo>();
	
	
	/**任务超时是否自动处理，此超时不是推送客端尝试多次，而是等待子任务完成*/
	public boolean autoDealTimeoutSubtask = false;
	
	/**
	 * 子任务推送的最多尝试次数
	 */
	public static int maxRePushSubTaskTimes=5;
	/**
	 * 等待子任务完成的超时的时间
	 */
//	public static long maxTimeoutSubTaskDealTime=24*60*60000L;
	
	/**线上子任务超时时间*/
	public static long maxTimeoutOnlineSubTaskTime=60*1000L;
	
	
	/**主任务超时时间*/
	public static long maxTimeoutTaskTime=2*24*60*60*1000L;

	/**
	 * 一个配置的client下的实时子任务信息(sessionId-List<SubTaskInfo>)
	 * 用sessionId方便应对底层的上下线变化。中间件的事件只能得到sessionId，没有ClientCode。
	 */
	public volatile static Map<String, List<SubTaskInfo>> clientSubTaskInfoMap = new ConcurrentHashMap<String, List<SubTaskInfo>>();

	/** app,client配置容器类 */
	private AppContainer appContainer;

	/** 子任务分派用线程池 */
	private static ExecutorService executor = Executors.newCachedThreadPool();

	/** 监听器-子任务完成 */
	public SubTaskListener subTaskListener = new SubTaskListener();

	/** 监听器-客户端上下线事件 */
	public ClientStatusListener clientStatusListener = new ClientStatusListener();

	/** 任务池锁 */
	private Object TaskLock = new Object();

	/** 任务处理线程 */
	public SubTaskRedo subTaskRedo = new SubTaskRedo();
	
	/**
	 * 外部持久化任务接口
	 */
	public TaskPersistenceInterface taskPersistenceInterface;

	/**
	 * <P>
	 * 根据提交请求，生成主任务子任务，放入登记的map，并放入子任务队列
	 * </P>
	 * 
	 * @param paras
	 * @param appCode
	 * @throws SurveyException 
	 */
	public boolean createAndPutTaskPool (TaskInfo taskInfo) throws SurveyException {
		boolean createResult=false;
//		TaskInfo taskInfo = TaskFactory.creatTaskInfo(paras);
		logger.debug("【主任务Map添加】当前总数:" + taskInfoMap.size());
//		logger.debug("【子任务队列添加】当前总数:" + subTaskInfoQueue.size());
		if (taskInfoMap.size() >= maxTaskMapSize) {
			logger.debug("【任务登记MAP】已满！");
			throw new SurveyException("任务登记已经满");
//			return false;
		} else {
			taskInfoMap.put(taskInfo.getTaskId(), taskInfo);
			Iterator<Map.Entry<String,SubTaskInfo>> iter =taskInfo.getSubTaskMap().entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, SubTaskInfo> entry = (Map.Entry<String, SubTaskInfo>) iter.next();
//				subTaskInfoMap.put(entry.getKey(), entry.getValue());
				try {
					startExecutorCompletionService(entry.getValue());
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new SurveyException("任务提交线程池失败",e);
				}
			}
			logger.debug("【主任务池添加】当前总数+1后:" + taskInfoMap.size());
//			logger.debug("【子任务队列添加】当前总数+1后:" + subTaskInfoQueue.size());
			//阻塞队列不需要另加锁
//			synchronized (TaskLock) {
//				TaskLock.notifyAll();
//			}
			createResult = true;
		}
		logger.debug("【任务登记MAP】情况如下：");
		Iterator<Map.Entry<String,TaskInfo>> iter =taskInfoMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, TaskInfo> entry = (Map.Entry<String, TaskInfo>) iter.next();
			logger.debug("【任务登记MAP】主任务id|(总|推|执|完)："+entry.getValue().getTaskId()+"|"+entry.getValue().getTotalTask()+"|"+entry.getValue().getPushTask()+"|"+entry.getValue().getExecuteTask()+"|"+entry.getValue().getCompleteTask() );
		}
		return createResult;
	}


	/**
	 * 手工界面管理子任务时，可对超时的任务置为失败
	 * @param finishJason
	 */
	public void setPushFailureSubTaskFinish(String taskId,String subTaskId) {
		// 根据子任务完成情况，修改子任务的状态，以及主任务的状态
		logger.info("【修改子任务的完成状态】subTaskId："+subTaskId);
		
		try {
			String code = "failure";//finishJason.getString("code");
			String msg = "手工置超时任务失败";//finishJason.getString("msg");
//			String subTaskId = finishJason.getJSONObject("data").getString("subTaskId");
//			String taskId = finishJason.getJSONObject("data").getString("taskId");
//			String clientCode = finishJason.getString("clientCode");
			
			JSONObject finishJason = new JSONObject();
			finishJason.put("code", "failure");
			finishJason.put("msg", "线下子任务人工失败");
			
			JSONObject finishData = new JSONObject();
			finishData.put("taskId", taskId);
			finishData.put("subTaskId", subTaskId);
			finishData.put("remark", "设置失败时间" + System.currentTimeMillis());
			finishJason.put("data", finishData);
//			logger.debug("【子任务队列消费】：【" + subtaskInfo.getSubTaskId() + "】重试了" + subtaskInfo.getExeErrorCount() + "次,用时" + reDoTime + "ms");
			//通用处理完成或者失败的子任务。推送试过了也就当子任务结束了。
			updateSubTaskStatus(finishJason);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("【修改子任务的状态，并移除】出错："+e.toString());
		}
	}
	
	/**
	 * 手工界面管理子任务时，可对超时的任务置重新运行
	 * @param finishJason
	 */
	public void setPushFailureSubTaskRedo(String taskId,String subTaskId) {
		// 根据子任务完成情况，修改子任务的状态，以及主任务的状态
		logger.info("【重置子任务的状态，再次推送】返回："+subTaskId.toString());
		
		try {
//			String code = "failure";//finishJason.getString("code");
//			String msg = "手工置超时任务失败";//finishJason.getString("msg");
//			String subTaskId = finishJason.getJSONObject("data").getString("subTaskId");
//			String taskId = finishJason.getJSONObject("data").getString("taskId");
//			String clientCode = finishJason.getString("clientCode");
			TaskInfo taskInfo=taskInfoMap.get(taskId);
			if(taskInfo==null){
				logger.info("【重置子任务的状态，再次推送】完成太晚了被超时先处理了，内存中已经没有这个主任务了！taskId："+taskId);
				return;
			}
			SubTaskInfo subTaskInfo = taskInfo.getSubTaskMap().get(subTaskId);
			subTaskInfo.setCreateTime(new Date());
			subTaskInfo.setExeErrorCount(0);
			subTaskInfo.setStatus(null);
			startExecutorCompletionService(subTaskInfo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("【重置子任务的状态，再次推送】出错："+e.toString());
		}
	}
	
	
	/**
	 * 手工界面管理任务时，可对主任务进行人工失败
	 * @param finishJason
	 */
	public void setTaskFailure(String taskId) {
		// 根据子任务完成情况，修改子任务的状态，以及主任务的状态
		logger.info("【修改任务的完成状态,手工移除】taskId：" + taskId.toString());
		TaskInfo taskInfo = taskInfoMap.remove(taskId);// 从任务记录中移除
		if(taskInfo==null) {
			logger.info("【修改任务的完成状态,手工移除】任务池子中找不到这个任务了："+taskId);
			return;
		}
		taskInfo.setStatus(Constants.TASK_FAILURE);
		//主任务入库
		if (taskPersistenceInterface != null) {
			try {
				taskPersistenceInterface.modifyTask2DB(taskInfo);
				logger.debug("【修改任务的完成状态,手工移除】持久化任务OK：" + taskId);
			} catch (SurveyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("【修改任务的完成状态,手工移除】持久化任务出错id：" + taskId);
			}// 主任务入库
		}
		//子任务入库
		Iterator<Map.Entry<String,SubTaskInfo>> iter =taskInfo.getSubTaskMap().entrySet().iterator();
		Date nowDate=new Date();
		while (iter.hasNext()) {
			Map.Entry<String, SubTaskInfo> entry = (Map.Entry<String, SubTaskInfo>) iter.next();
			entry.getValue().setUpdateTime(nowDate);
			entry.getValue().setExeErrorCount(maxRePushSubTaskTimes+1);
			entry.getValue().setStatus(Constants.TASK_FAILURE);
			entry.getValue().setDescription("人工设置主任务失败");
			try {
				if (taskPersistenceInterface != null) taskPersistenceInterface.modifySubTask2DB(entry.getValue());
			} catch (SurveyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.info("【修改任务的完成状态,手工移除】有完成后移除。总任务【移除后】有：" + taskInfoMap.size());
	}
	

	/**
	 * <P>启动一个子任务队列消费线程，用于把子任务扔进线程池中。
	 *（新的任务是直接入池。因为线程池中出了问题的子任务也需要先进一个队列等待，再扔进线程池处理。）
	 * </P>
	 */
	public void initTaskComsuptionThread() {
		Thread taskComsuptionThread = new Thread(subTaskRedo);
		taskComsuptionThread.setName("TaskComsuptionThread");
		taskComsuptionThread.setDaemon(true);
		taskComsuptionThread.start();
	}

	public AppContainer getAppContainer() {
		return appContainer;
	}

	public void setAppContainer(AppContainer appContainer) {
		this.appContainer = appContainer;
	}
	

	public boolean isAutoDealTimeoutSubtask() {
		return autoDealTimeoutSubtask;
	}


	public void setAutoDealTimeoutSubtask(boolean autoDealTimeoutSubtask) {
		this.autoDealTimeoutSubtask = autoDealTimeoutSubtask;
	}


	public TaskPersistenceInterface getTaskPersistenceInterface() {
		return taskPersistenceInterface;
	}


	public void setTaskPersistenceInterface(TaskPersistenceInterface taskPersistenceInterface) {
		this.taskPersistenceInterface = taskPersistenceInterface;
	}


	/**
	 * 更新在线客户端
	 * @param clientCode
	 */
	@Deprecated
	public void updateClientOnline(String clientCode) {
		appContainer.updateClientOnline(clientCode);
		// if(!isAdd) clientSubTaskInfoMap.remove("");
	}

	/**
	 * <P>更新所有分配过此客户端的子任务的目标客户端为空，并重新放入线程池执行</P>
	 * @param clientCode
	 */
	public synchronized void updateSubTaskInfoByOffline(String clientSessionId) {
		// appContainer.updateClientOnline(clientCode);
		List<SubTaskInfo> subTaskInfoList = clientSubTaskInfoMap.get(clientSessionId);
		logger.info("【重置下线客户端的任务】当前客户端下共有任务数：" + subTaskInfoList.size());
		//清除掉客户端中子任务中的配置的客户端
		for (SubTaskInfo subTaskInfo : subTaskInfoList) {
			subTaskInfo.setSurveyClient(null);
			try {
				startExecutorCompletionService(subTaskInfo);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//清除掉客户端里的所有子任务
		clientSubTaskInfoMap.remove(clientSessionId);
		//subTaskInfoList.clear();
	}

	/**
	 * <P>
	 * 根据监听的子任务完成结果，更新任务的状态，都完成后有可能从总任务表中移除，并且调用外部接口，进行持久化操作。
	 * </P>
	 * 
	 * @param finishJason
	 */
	public void updateSubTaskStatus(JSONObject finishJason) {
		// 根据子任务完成情况，修改子任务的状态，以及主任务的状态
		logger.info("【修改子任务的完成状态】："+finishJason.toString());
		
		try {
			String code = finishJason.getString("code");
			String msg = finishJason.getString("msg");
			String subTaskId = finishJason.getJSONObject("data").getString("subTaskId");
			String taskId = finishJason.getJSONObject("data").getString("taskId");
			String remark = (finishJason.getJSONObject("data").containsKey("remark"))? finishJason.getJSONObject("data").getString("remark"):"";
//			String clientCode = finishJason.getString("clientCode");
			TaskInfo taskInfo=taskInfoMap.get(taskId);
			if(taskInfo==null){
				logger.info("【修改子任务的完成状态】内存中已经没有这个主任务了！taskId："+taskId);
				//如果需要，找不到主任务了，还可以直接入库
				//如果非线上任务，有可能内存中没有了，因为非线上，内存中存在的时间太长了，占用比较大
				//是否都完成，以及上报都在接口中实现
				if(taskPersistenceInterface!=null) taskPersistenceInterface.modifySubTask2DB(finishJason);//子任务入库
				return;
			}
			SubTaskInfo subTaskInfo = taskInfo.getSubTaskMap().get(subTaskId);
			

			
//			if(Constants.TASK_TYPE_ONLINE.equals(subTaskInfo.getSubTaskType())){
//				logger.info("【修改子任务的完成状态】线上子任务不能人工处理！subTaskInfo.getSubTaskType()："+subTaskInfo.getSubTaskType());
//				//如果需要，找不到主任务了，还可以直接入库
//				return;
//			}
			//1.【收到】
			if("received".equals(code)) {
				logger.info("【修改子任务的完成状态，并移除】子任务为异步的，对方已经收到！code："+code);
				subTaskInfo.setStatus(Constants.TASK_DOING);
				subTaskInfo.setDescription("子应用已经收到子任务");
				synchronized (taskInfo) {
					taskInfo.setReceivedAsyncTask(taskInfo.getReceivedAsyncTask()+1);
				}
				//这里啥也不做。因为是异步的，只是对方已经收到了。
				return;
			}
			if(Constants.TASK_FAILURE.equals(subTaskInfo.getStatus()) ||  Constants.TASK_DONE.equals(subTaskInfo.getStatus())  ){
				//这里啥也不做。可能总任务检测时设置了结果
				return;
			}
			subTaskInfo.setSurveyClient(null);
//			subTaskInfo.setUpdateTime(new Date());//数据持久化时再设置
			//2.【不成功】 默认成功。成功时...(一定是执行且成功的)
			if(!"success".equals(code)) {
				logger.info("【修改子任务的完成状态，并移除】子任务推送或者执行出错了！返回code："+code);
				subTaskInfo.setStatus(Constants.TASK_FAILURE);
				subTaskInfo.setDescription(msg+remark);//中文失败与原因
				//这里是否加入出错队列再处理？还是先入库，以后从库中加载呢？都可以，目前先入。如果推送失败的，已经推过多次了，如果执行失败的，先不再推送了。
//				return;
			}//3.【成功】
			else{
				logger.info("【修改子任务的完成状态，并移除】子任务执行成功！code："+code);
				String hasData = (finishJason.getJSONObject("data").containsKey("hasData"))? finishJason.getJSONObject("data").getString("hasData"):"";
				Integer dataCount =0;
				try {
					dataCount = (finishJason.getJSONObject("data").containsKey("dataCount"))? finishJason.getJSONObject("data").getInt("dataCount"):0;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				subTaskInfo.setHasData(hasData);
				subTaskInfo.setDataCount(dataCount);
				subTaskInfo.setStatus(Constants.TASK_DONE);
			}

			
			if(taskPersistenceInterface!=null) taskPersistenceInterface.modifySubTask2DB(subTaskInfo);//子任务入库
			synchronized (taskInfo) {//计算完成数
				//执行数据+1（包括推失败的，对方收到的异步的），完成数要看成功才成。
				TaskInfo.modifyTaskInfoFinish(taskInfo, "success".equals(code));
//				logger.info("【修改子任务的完成状态，并移除】总任务【移除前】有："+taskInfoMap.size());
//				logger.info("【修改主任务的状态，并可能移除】当前主任务的-总|执|完|线：" + taskInfo.getTotalTask() + "|" + taskInfo.getExecuteTask()+ "|" + taskInfo.getCompleteTask()+ "|" + taskInfo.getOnlineTask());
			}
			
			
			
			
			//del--->当执行数与线上数一样时。线上都完成了。就持久化，但不移除。当与总数一样时，持久化并移除。
			//都执行了就移除，并持久化。但如果不全是线上的，置的状态不一样
			if (taskInfo.getExecuteTask().intValue() == taskInfo.getTotalTask().intValue()) {
				//【移除的情况：】当线上数与总数一样的时候，全完成了。就从内存中移除，并且调外部接口类进行持久化。线下子任务没有超时机制，可能一直接没反馈，由主任务总超时处理。
//				if (taskInfo.getOnlineTask().intValue() == taskInfo.getTotalTask().intValue()  || taskInfo.getExecuteTask().intValue() == taskInfo.getTotalTask().intValue() ) {
				//如果没有异步的任务
				if (taskInfo.getReceivedAsyncTask()==0 ) {
					//主任务状态为：
					boolean isAllSubtaskOk=taskInfo.getCompleteTask().intValue()==taskInfo.getExecuteTask();
					taskInfo.setStatus(isAllSubtaskOk?Constants.TASK_DONE:Constants.TASK_FAILURE);
//					if(taskInfo.getCompleteTask().intValue() == taskInfo.getExecuteTask().intValue()) taskInfo.setStatus(Constants.TASK_FAILURE);
//					taskInfoMap.remove(taskInfo.getTaskId());// 从任务记录中移除
//					if(taskPersistenceInterface!=null) taskPersistenceInterface.modifyTask2DB(taskInfo);//主任务入库
					logger.info("【修子任务的完成状态，全部完成并移除】移除任务id：" + taskInfo.getTaskId());
				}else{
					//【持久化】都移除，线下不适合在内存中长时间放。
					taskInfo.setStatus(Constants.TASK_DOING);
					logger.info("【修子任务的完成状态，只持久化线上部分】任务id：" + taskInfo.getTaskId()+"。此时收到线下任务回复数为："+taskInfo.getReceivedAsyncTask());
				}
				taskInfoMap.remove(taskInfo.getTaskId());// 从任务记录中移除(线下的不适合长时间放在内存中)
				logger.info("【修改任务的完成状态，全部完成并移除】有完成后移除。总任务【移除后】有：" + taskInfoMap.size());
				if(taskPersistenceInterface!=null) taskPersistenceInterface.modifyTask2DB(taskInfo);//主任务入库
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("【修改子任务的状态，并移除】出错："+e.toString());
		}
	}
	
	
	/**
	 * <P>守护线程中定时会清理超时的主任务。(线上子任务等待结果超时在队列里处理，线下超时不处理，由主任务超时处理)</P>
	 */
	public void dealTimeoutTask(TaskInfo taskInfo){
		//检查一下，万一主任务执行了好久了，也就超时出错吧.不会执行这里，因为子任务出错比较快，不会再调用到这里。
		logger.debug("【修改任务的完成状态，超时而移除】有完成后移除：" + taskInfo.getRequestTime());
		Date nowDate=new Date();
		long reDoTime=nowDate.getTime()-taskInfo.getRequestTime().getTime();
		if(taskInfo.getRequestTime()!=null && (reDoTime>maxTimeoutOnlineSubTaskTime)){
			//如果配置手工处理，就不处理超时的主任务。子任务也不处理。
//			if(!isAutoDealTimeoutSubtask()){
//				logger.info("【修改任务的完成状态，超时而移除】主任务超时，但配置由手工处理：" + taskInfo.getTaskId());
//				return;
//			}
			logger.info("【修改任务的完成状态，超时而移除】主任务超时，自动处理：" + taskInfo.getTaskId());
			if(taskInfo.getTotalTask().intValue()==taskInfo.getOnlineTask().intValue()){
				taskInfo.setStatus(Constants.TASK_TIMEOUT);
				taskInfoMap.remove(taskInfo.getTaskId());// 从任务记录中移除
				if(taskPersistenceInterface!=null){
					try {
						taskPersistenceInterface.modifyTask2DB(taskInfo);
					} catch (SurveyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.info("【修改任务的完成状态，超时而移除】持久化任务id：" + taskInfo.getTaskId());
					}//主任务入库
				}
			}else{
				//只让线上的子任务超时
				Iterator<Map.Entry<String,SubTaskInfo>> iter =taskInfo.getSubTaskMap().entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, SubTaskInfo> entry = (Map.Entry<String, SubTaskInfo>) iter.next();
					// subTaskInfoMap.put(entry.getKey(), entry.getValue());
					SubTaskInfo subTaskInfo = entry.getValue();
					subTaskInfo.setStatus(Constants.TASK_TIMEOUT);
					subTaskInfo.setUpdateTime(nowDate);
					subTaskInfo.setDescription("线上子任务没收到反馈，超时");
					
					JSONObject finishJason = new JSONObject();
					finishJason.put("code", "failure");
					finishJason.put("msg", "线上子任务等待结果超时了");
					
					JSONObject finishData = new JSONObject();
					finishData.put("taskId", subTaskInfo.getTaskInfo().getTaskId());
					finishData.put("subTaskId", subTaskInfo.getSubTaskId());
					finishData.put("remark", "等待用时" + reDoTime + "ms");
					finishJason.put("data", finishData);
					
					updateSubTaskStatus(finishJason);
//					if(taskPersistenceInterface!=null){
//						try {
//							taskPersistenceInterface.modifySubTask2DB(finishJason);
//						} catch (SurveyException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//							logger.info("【修改任务的完成状态，超时而移除】持久化任务id：" + taskInfo.getTaskId());
//						}//主任务入库
//					}
				}
			}

			logger.info("【修改任务的完成状态，超时而移除】有完成后移除。总任务【移除后】有：" + taskInfoMap.size());
		}
	}

	
	/**
	 * <P>用于处理阻塞对列里的子任务，再扔进线程池。</P>
	 * 1.反复处理再分配的子任务，一定次数后超时。主要是原通讯没返回sessionId，监听移除有问题，所以多试几次。
	 * 每次试如果不能会真正移除不在线的客户端。
	 * 2.处理线上子任务，已经推送出去了，状态发生变化。反复检测是不是超时没有收到反馈。有反馈的会设置子任务状态，就不再入队了。
	 * @author liujun
	 * @date 2018年1月22日 上午9:59:10
	 */
	private class SubTaskRedo implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					logger.debug("【子任务队列消费】---------------BEGIN------------------");
					// 因为这个队列中都是出问题的子任务，所以要等待一下处理。
					Thread.sleep(2000);// 调节频率，过快容易撑死~~
					// logger.debug("【子任务队列】的任务数1：" + taskNum);
					SubTaskInfo subtaskInfo = subTaskInfoQueue.take();
					logger.debug("【子任务队列消费】取出子任务Id：" + subtaskInfo.getSubTaskId());
					// logger.debug("【子任务队列】的任务数2：" + subTaskInfoQueue.size());
					// 如果外部主任务设置了状态，子任务就不处理，丢弃。
//					if (subtaskInfo.getTaskInfo().getStatus() != null) {
//						logger.debug("【子任务队列消费】主任务已经设置状态，子任务不再处理了。" + subtaskInfo.getTaskInfo().getStatus());
//					} else {
						long reDoTime = System.currentTimeMillis() - subtaskInfo.getCreateTime().getTime();
						//如果是推送成功的线上任务，进行超时处理
						if("success".equals(subtaskInfo.getAsignStatus())){
							if (reDoTime > maxTimeoutOnlineSubTaskTime) {
								logger.debug("【子任务队列消费】此线上子任务已经超时（ms）。"+reDoTime);
								// 自动处理，作为失败子任务返回
								JSONObject finishJason = new JSONObject();
								finishJason.put("code", "failure");
								finishJason.put("msg", "线上子任务等待结果超时");
								
								JSONObject finishData = new JSONObject();
								finishData.put("taskId", subtaskInfo.getTaskInfo().getTaskId());
								finishData.put("subTaskId", subtaskInfo.getSubTaskId());
								finishData.put("remark", "等待用时" + reDoTime + "ms");
								finishJason.put("data", finishData);
//								logger.debug("【子任务队列消费】：【" + subtaskInfo.getSubTaskId() + "】重试了" + subtaskInfo.getExeErrorCount() + "次,用时" + reDoTime + "ms");
								//通用处理完成或者失败的子任务。推送试过了也就当子任务结束了。
								updateSubTaskStatus(finishJason);
							}
						}
						else{
							//没有推送成的，看是否超过次数，可能继续推送
							logger.debug("【当前子任务推送重试】子任务id[" + subtaskInfo.getSubTaskId() + "]重试了" + subtaskInfo.getExeErrorCount() + "次,用时" + reDoTime + "ms");
							// 目前只控制时间，超时后。如果不自动处理，就不再加入队列中，不再处理了，需要人工来处理了。
							if (subtaskInfo.getExeErrorCount() > maxRePushSubTaskTimes) {
								logger.debug("【子任务队列消费】自动处理begin");
								// 自动处理，作为失败子任务返回
								JSONObject finishJason = new JSONObject();
								finishJason.put("code", "failure");
								finishJason.put("msg", "任务推送失败");
								JSONObject finishData = new JSONObject();
								finishData.put("taskId", subtaskInfo.getTaskInfo().getTaskId());
								finishData.put("subTaskId", subtaskInfo.getSubTaskId());
								finishData.put("remark", "重试了" + subtaskInfo.getExeErrorCount() + "次,用时" + reDoTime + "ms");
								finishJason.put("data", finishData);
								logger.debug("【子任务队列消费】：【" + subtaskInfo.getSubTaskId() + "】重试了" + subtaskInfo.getExeErrorCount() + "次,用时" + reDoTime + "ms");
								//通用处理完成或者失败的子任务。推送试过了也就当子任务结束了。
								updateSubTaskStatus(finishJason);
							} else {
								logger.debug("【子任务队列消费】重新回线程池处理----------END---------");
								startExecutorCompletionService(subtaskInfo);
							}
						}
//					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.debug("【子任务队列消费】异常：-----------------------------" + e.toString());
				}
			}

		}
	}

	/**
	 * <P>将子任务设置一个可用的客户端后，通过线程池执行发送</P>
	 * 
	 * @param subTaskInfo
	 * @param taskContainer
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void startExecutorCompletionService(SubTaskInfo subTaskInfo) throws InterruptedException, ExecutionException {

		if (StringUtils.isBlank(subTaskInfo.getAppCode()) && StringUtils.isBlank(subTaskInfo.getAppKey())) {
			logger.warn("【分派预处理】子任务未设置AppCode或者AppKey，子任务Id：" + subTaskInfo.getSubTaskId());
			return;
		}
		if (!StringUtils.isBlank(subTaskInfo.getStatus())) {
			//已经有状态的，都是从库里加载的，不处理了，只放池子里。等线下的回调，或者人工处理，或者超时了。
			logger.warn("【分派预处理】子任务已经有状态，不再分配推 。子任务Id：" + subTaskInfo.getSubTaskId()+",状态："+subTaskInfo.getStatus());
//			if(Constants.TASK_TYPE_ONLINE.equals(subTaskInfo.getSubTaskType()))
//				subTaskInfoQueue.put(subTaskInfo);//如果成功的，并且是线上的，进行超时处理。
			return;
		}
		// 从在线客户端表中取一个
//		SurveyClient surveyClient = AppContainer.getWeightRandomClient(subTaskInfo.getAppCode());
		SurveyClient surveyClient =null;
		if(StringUtils.isBlank(subTaskInfo.getAppKey())){
			logger.debug("【老版本-按AppCode分配】-----------旧版getAppCode--"+subTaskInfo.getAppCode());
			surveyClient=AppContainer.getClientByUserRankAndClinetLever(subTaskInfo.getAppCode(),0);
		}else
		{
			logger.debug("【新版本-按Appkey分配】-----------新版getAppKey--"+subTaskInfo.getAppKey());
			surveyClient=AppContainer.getClientByUserRankAndClinetLeverByAppkey(subTaskInfo.getAppKey(),0);
		}
		if (surveyClient == null || surveyClient.getClientCode() == null) {// 后面表示没有业务心跳补充属性
//			logger.debug("");
			logger.warn("【分派预处理】暂无可用的客户端");
			logger.debug("");
			subTaskInfo.setExeErrorCount(subTaskInfo.getExeErrorCount()+1);
//			subTaskInfoQueue.put(subTaskInfo);//没客户处理，则回炉
			
			logger.debug("【分派预处理】原来回炉再次尝试，现在直接返回任务推送失败，再推无意义");
			// 自动处理，作为失败子任务返回
			JSONObject finishJason = new JSONObject();
			finishJason.put("code", "failure");
			finishJason.put("msg", "任务推送失败");
			JSONObject finishData = new JSONObject();
			finishData.put("taskId", subTaskInfo.getTaskInfo().getTaskId());
			finishData.put("subTaskId", subTaskInfo.getSubTaskId());
			finishData.put("remark", "暂无可用的客户端");
			finishJason.put("data", finishData);
			logger.debug("【子任务队列消费】：【暂无可用的客户端");
			//通用处理完成或者失败的子任务。推送试过了也就当子任务结束了。
			updateSubTaskStatus(finishJason);
			
			
			return;
		} 
		
		//设置执行子任务的在线客户端,之前有设置过其它的，也会被替换成当前的
		String sessionId=surveyClient.getSessionId();
		logger.debug("【分派预处理】策略选出的客户端sessionId为：" +sessionId );
		subTaskInfo.setSurveyClient(surveyClient);
		subTaskInfo.setClientCode(surveyClient.getClientCode());//标识最后一次使用的客户端

		//一个在线客户端下所有的任务中加入此任务。用于客户端下线后，更新下面的子任务
		 List<SubTaskInfo> subTaskInfoList =clientSubTaskInfoMap.get(sessionId);
		 if(subTaskInfoList==null) subTaskInfoList=new ArrayList<SubTaskInfo>();
		 subTaskInfoList.add(subTaskInfo);

		// 交线程池执行分派子任务
		Future<String> future = executor.submit(new AssignTask(subTaskInfo));
		String exeResult = "";
		try {
			exeResult = future.get(15L, TimeUnit.SECONDS);
		} catch (TimeoutException|ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.warn("【分派预处理】出错e:" + e.getMessage());
			exeResult = "failure";
		} finally {
			logger.info("【分派预处理】子任务分派结果为：" + exeResult);
			boolean isSubTaskSendOk = "success".equals(exeResult);
			//推送后的维护
			clientSubTaskInfoMap.get(sessionId).remove(subTaskInfo);//从当前客户端下移除子任务
			subTaskInfo.setSurveyClient(null);//子任务清除客户端
			
			
			if(!isSubTaskSendOk){
				logger.info("【分派预处理】子任务分派不成功，重新回队列subTaskId：" + subTaskInfo.getSubTaskId());
				subTaskInfo.setExeErrorCount(subTaskInfo.getExeErrorCount()+1);
				subTaskInfoQueue.put(subTaskInfo);//不成功，则回炉
			}else{
				subTaskInfo.setAsignStatus("success");//表示分配成功，等结果了
				if(Constants.TASK_TYPE_ONLINE.equals(subTaskInfo.getSubTaskType()))
				subTaskInfoQueue.put(subTaskInfo);//如果成功的，并且是线上的，进行超时处理。
			}
			
			
			//对主任务进行状态标识
			TaskInfo taskInfo = subTaskInfo.getTaskInfo();
			synchronized (taskInfo) {
				TaskInfo.modifyTaskInfoPush(taskInfo, isSubTaskSendOk);
			}
		}
	}

	/**
	 * <P>线程池执行发送任务</P>
	 * 
	 * @author liujun
	 * @date 2018年1月15日 下午5:36:48
	 */
	static class AssignTask implements Callable<String> {
		private SubTaskInfo subTaskInfo;

		public AssignTask(SubTaskInfo subTaskInfo) {
			this.subTaskInfo = subTaskInfo;
		}

		@Override
		public String call() throws Exception {
			// Thread.sleep(3000);
			 logger.info("【推送线程任务】任务执行...");
			// if(true) return "success";//测试

//			String body = subTaskInfo.getParaObj().toString();
			//推送的子任务，只包括总任务ID，子任务ID，其它都是一个json中。
			SubTaskData subTaskData=new SubTaskData();
			subTaskData.setTaskId(subTaskInfo.getTaskInfo().getTaskId());
			subTaskData.setSubTaskId(subTaskInfo.getSubTaskId());
			subTaskData.setSubTaskType(subTaskInfo.getSubTaskType());
			subTaskData.setQueryJsonStr(subTaskInfo.getParaObj().toString());
			//这两个用于异步任务时，客户端按里面的IP,PORT发结果消息上来。
			//不管是什么，都加上这个。如果多个中间层，那要按这个回复信息。
			subTaskData.setServerIp(ContainerInit.getInstance().appContainer.getServerIp());
			subTaskData.setServerPort(ContainerInit.getInstance().appContainer.getServerPort());
			
			String body =(JsonUtils.toString(subTaskData));
			
			logger.debug("----------------【推送子任务】--------------------body："+body);
			// String appCode = subTaskInfo.getAppCode();

			if (subTaskInfo.getSurveyClient() == null) {
				logger.info("【推送线程任务】任务未设置执行客户端:" + subTaskInfo.getDescription());
				return "failure";
			}
			String clientSessionId=subTaskInfo.getSurveyClient().getSessionId();
			logger.info("【推送任务任务】推送目标sissionId:" + clientSessionId);
			boolean bln = ServerPushHandler.pushBySessionId(subTaskInfo.getSurveyClient().getSessionId(), "assignTaskToClient", body, new ServerPushTaskCallback());
			logger.info("【推送任务任务】bln:"+bln);
			// boolean bln =
			// ServerPushHandler.pushByAppKey(subTaskInfo.getSurveyClient().getClientCode(),
			// "assignTaskToClient", body);// 消息推送，推送所有服务器
			
			//不管成功不成功，去除子任务与动态客户端的关联（成功就不要关联了，不成功也应该换其它的客户端了）
//			List thisClientSubTaskList=clientSubTaskInfoMap.get(clientSessionId);
//			if(thisClientSubTaskList==null) thisClientSubTaskList=new ArrayList<SubTaskInfo>();
//			logger.info("【推送任务推送】当前session下的子任务数:" + thisClientSubTaskList.size());
//			if (thisClientSubTaskList.size() > 0) {
//				thisClientSubTaskList.remove(subTaskInfo);
//				subTaskInfo.setSurveyClient(null);
//			}
//			Map<String, List<SubTaskInfo>> 
			
			//注意：【在返回值的furturn中处理移除或者再入队的操作】
			
			if (bln) {
				logger.info("【推送任务推送】子任务成功");
				subTaskInfo.setAsignStatus("success");
				return "success";
			} else {// 推送失败
				logger.error("【推送任务推送】子任务推送失败：" + subTaskInfo.getSubTaskId());
				subTaskInfo.setAsignStatus("failure");

				//按说客户端下线可以事件中移除，但偶尔出现没有移除，所以在这里将无法推送的，
				//将这个不可用的channel的的客户端移除
				//注意：【在返回值的furturn中处理移除或者再入队的操作】
				Map<String, SurveyClient> surveyClientMap=AppContainer.onlineClientMap.get(subTaskInfo.getSurveyClient().getSurveyApp().getAppKey());
				if(surveyClientMap.containsKey(subTaskInfo.getSurveyClient().getSessionId())){
					logger.info("【推送失败移除客户端】sessionId:"+subTaskInfo.getSurveyClient().getSessionId());
					surveyClientMap.remove(subTaskInfo.getSurveyClient().getSessionId());// 根据通讯客户端，移除里面的调查客户端对象
				}
				return "failure";
			}
		}
	}

	/**
	 * 子任务被远程第三方应用完成后，监听器类处理状态状态
	 * <P>
	 * </P>
	 * 
	 * @author liujun
	 * @date 2018年1月15日 下午6:28:24
	 */
	public class SubTaskListener implements SubTaskFinishInterface {
		@Override
		public void onSubTaskFinished(JSONObject finishJason) {
			// TODO Auto-generated method stub
			logger.info("子任务监听，准备修改任务状态");
			updateSubTaskStatus(finishJason);
		}
	}

}
