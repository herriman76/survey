package com.sanyinggroup.corp.survey.middle.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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







import com.sanyinggroup.corp.survey.middle.Constants;
import com.sanyinggroup.corp.survey.middle.MiddleConfig;
import com.sanyinggroup.corp.survey.middle.handle.ClientStatusListener;
import com.sanyinggroup.corp.survey.middle.handle.RecvClientInfoHandler;
import com.sanyinggroup.corp.survey.middle.model.SurveyClient;
import com.sanyinggroup.corp.survey.middle.tools.RankAndLeverTreeMapSelect;
import com.sanyinggroup.corp.urocissa.server.ServerInit;
import com.sanyinggroup.corp.urocissa.server.api.handler.ServerPushHandler;
import com.sanyinggroup.corp.urocissa.server.api.info.ClientApp;
import com.sanyinggroup.corp.urocissa.server.api.service.MsgServiceHandlerRegister;

/**
 * 任务容器-管理背调任务与相关子任务
 * @author liujun
 * @date 2018年1月18日 上午10:33:01
 */
public class AsServerContainer {
	private static final Logger logger = Logger.getLogger(AsServerContainer.class);

	/** 实时总任务信息(taskid---TaskInfo(SubTaskInfoMap)) */
	public static volatile Map<String, SurveyClient> onlineClientMap=new ConcurrentHashMap<String,SurveyClient>();

	public boolean isMiddlewearStarted=false;
	
	
	/** 任务在内存中允许的最大存放数 */
//	private static Integer maxTaskMapSize=Integer.MAX_VALUE;


	/**
	 * 可接入的类型列表，目前一个中间层只支持一种类型的接口接入。（同一类型的app都一样，不同的不一样）
	 */	
	private static List<ClientApp> clientAppList=new ArrayList<ClientApp>();
	
//	static{
//		//设置有效的客户端
//
//	}
	
	/**
	 * 失败子任务重复处理次数、与超时处理的时间
	 */
	public static int maxReDealSubTaskTimes=20;
	/**
	 * 失败子任务重复处理次数、与超时处理的时间
	 */
	public static long maxTimeoutSubTaskTime=60000L;
	
	/**主任务超时时间*/
	public static long maxTimeoutTaskTime=150000L;

	/**
	 * 一个配置的client下的实时子任务信息(sessionId-List<SubTaskInfo>)
	 * 用sessionId方便应对底层的上下线变化。中间件的事件只能得到sessionId，没有ClientCode。
	 */
//	public volatile static Map<String, List<SubTaskInfo>> clientSubTaskInfoMap = new ConcurrentHashMap<String, List<SubTaskInfo>>();

	/**用户单例线程锁*/
	private static Boolean lockSigleton = true; 
	private static AsServerContainer asServerContainer;

	/** 子任务分派用线程池 */

	/** 监听器-子任务完成 */
//	public SubTaskListener subTaskListener = new SubTaskListener();

	/** 监听器-客户端上下线事件 */

	/** 任务池锁 */

	/** 任务处理线程 */
//	public TaskConsumer taskConsumer = new TaskConsumer();
	
	/**
	 * 外部持久化任务接口
	 */
//	public TaskPersistenceInterface taskPersistenceInterface;


	public static AsServerContainer getInstance(){
		if (asServerContainer == null) {
			synchronized (lockSigleton) {
				if (asServerContainer == null) {
					asServerContainer = new AsServerContainer();
					if(!StringUtils.isBlank(MiddleConfig.getAppKey()) && !StringUtils.isBlank(MiddleConfig.getAppSecret()) ){
						ClientApp eachClient=new ClientApp();//通讯层用的客户对象
						eachClient.setAppKey(MiddleConfig.getAppKey());
						eachClient.setAppSecret(MiddleConfig.getAppSecret());
						logger.info("根据配置生成有效的通讯Client:"+eachClient.getAppKey()+"|"+eachClient.getAppSecret());
						clientAppList.add(eachClient);
					}
					asServerContainer.initMiddlerWareStart();
				}
			}
		}
		return asServerContainer;
	}

	  /**
     * 启动背调中心的通讯中间件服务
     * <P></P>
     */
    public void initMiddlerWareStart(){
		logger.info("【背调中心】注册客户心跳消息与任务完成消息的回调处理");
		if(clientAppList==null || clientAppList.size()==0){
			logger.info("【背调中心】作为服务端，没有配置可用的客户端");
			return;
		}
		try {
			// 获取应用client基本信息
			MsgServiceHandlerRegister register = MsgServiceHandlerRegister.getRegister();
			//注册事件处理类
			MsgServiceHandlerRegister.setEventHandlerClass(ClientStatusListener.class);
			logger.info("1.【背调中间层】注册客户事件处理类成功");
			//注册消息处理类
			register.addMsgServiceHandler("getClientSystemInfo",RecvClientInfoHandler.class);
			logger.info("2.【背调中间层】注册客户心跳消息回调处理成功");
//			register.addMsgServiceHandler("subTaskFinishInfo", RecvClientTaskHandler.class);
			logger.info("3.【背调中间层】注册客户任务完成消息回调处理成功");

			new Thread(new Runnable(){
			    @Override
			    public void run() {
			        // TODO Auto-generated method stub
			    	try {
			    		isMiddlewearStarted=true;
			    		ServerInit.init(Integer.parseInt(MiddleConfig.getServerPort()), clientAppList);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						isMiddlewearStarted=false;
					}
			    }
			}).start();
			logger.info("【背调中间层】启动消息服务成功！端口：" + MiddleConfig.getServerPort());
		} catch (Exception e) {
			isMiddlewearStarted=false;
			e.printStackTrace();
			logger.error("【背调中间层】启动消息服务失败！异常：" + e.toString());
		}
	
    }
    
    

    /**
     * 可用执行的客户端进行选择的策略对象
     * @param userRank
     * @return
     */
	public static SurveyClient getClientByUserRankAndClinetLever(int userRank) {
		// 从appCode得到appKey，从而找到可用的在线客户端.让前端根据appKey来分子任务不可靠
		int onlineClientNum=onlineClientMap.size();
		logger.info("【策略2】surveyApp(code|在线客户端数):"+""+"|" + onlineClientNum);
		// 用于权重随机的参数对象
//		logger.info("【策略】可选用客户端的数:" + onlineClientNum);
		Map<String, Integer> canUseClient = new HashMap<String, Integer>();//用于treemap排序。
		List<SurveyClient> sameLeverClientList = new LinkedList<SurveyClient>();//用于同级别客户端存放
		if (onlineClientNum == 0) {
			logger.info("【策略2】备选客户端为0，返回!");
			return null;
		}else {
			Iterator iter = onlineClientMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, SurveyClient> entry = (Map.Entry<String, SurveyClient>) iter.next();
				logger.debug("【SurveyClient --INFO】"+entry.getValue().toString());
				Integer eachWeight = new Integer(entry.getValue().getWeight() == null ? Constants.CLIENT_BASE_LEVER+"" : entry.getValue().getWeight());
				canUseClient.put(entry.getKey(), eachWeight);
				logger.info("【策略2】循环可选用客户端详细信息(sessionId|weight):" + entry.getKey() + "|" + eachWeight+ "|" + entry.getValue());
				if(onlineClientNum==1) return entry.getValue();
			}
		}
		
		//找出可用的客户端，产生一个map，再构建treemap，用策略得到一个值。
		logger.info("【策略2】准备筛选的客户端个数："+canUseClient.size());//canUseClient不含有重复的，所以得到选择的客户端值，还要再处理多个同值的情况。
		RankAndLeverTreeMapSelect rankAndLeverTreeMapSelect = new RankAndLeverTreeMapSelect(canUseClient,0);
		Integer chooseValue = rankAndLeverTreeMapSelect.chooseValue();
		logger.info("【策略2】选择出的客户端lever："+chooseValue);
		if(chooseValue==null) return null;
		for(SurveyClient surveyClient:onlineClientMap.values()){
			if(surveyClient.getWeight()==null) surveyClient.setWeight(Constants.CLIENT_BASE_LEVER+"");
			if(chooseValue.intValue()==new Integer(surveyClient.getWeight()).intValue()) sameLeverClientList.add(surveyClient);
		}
		if(sameLeverClientList.size()==1) return sameLeverClientList.get(0);
		//如果同一个值的客户端有多个，再排序，取任务最少的一个。
		Collections.sort(sameLeverClientList,new Comparator<SurveyClient>() {
			@Override
			public int compare(SurveyClient surveyClient1, SurveyClient surveyClient2) {
				//以下如果改变顺序则调换一下参数位置
				return surveyClient1.getTaskCount()-(surveyClient2.getTaskCount());
			}
			
		});
		SurveyClient surveyClient=sameLeverClientList.get(0);
		sameLeverClientList.clear();
		return surveyClient;
	}

}
