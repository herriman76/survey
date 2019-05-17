package com.sanyinggroup.corp.survey.server.container;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.sanyinggroup.corp.survey.server.Constants;
import com.sanyinggroup.corp.survey.server.handler.ClientStatusListener;
import com.sanyinggroup.corp.survey.server.handler.RecvClientInfoHandler;
import com.sanyinggroup.corp.survey.server.handler.RecvClientTaskHandler;
import com.sanyinggroup.corp.survey.server.model.SubTaskInfo;
import com.sanyinggroup.corp.survey.server.model.SurveyApp;
import com.sanyinggroup.corp.survey.server.model.SurveyClient;
import com.sanyinggroup.corp.survey.server.para.ClientRealData;
import com.sanyinggroup.corp.survey.server.para.SurveyResponse;
import com.sanyinggroup.corp.survey.server.tools.JsonUtils;
import com.sanyinggroup.corp.survey.server.tools.RankAndLeverTreeMapSelect;
import com.sanyinggroup.corp.survey.server.tools.WeightRandomSelect;
import com.sanyinggroup.corp.urocissa.core.util.SecretManagement;
import com.sanyinggroup.corp.urocissa.server.ServerConfig;
import com.sanyinggroup.corp.urocissa.server.ServerGlobal;
import com.sanyinggroup.corp.urocissa.server.ServerInit;
import com.sanyinggroup.corp.urocissa.server.api.info.ClientApp;
import com.sanyinggroup.corp.urocissa.server.api.model.MiddleMsg;
import com.sanyinggroup.corp.urocissa.server.api.service.MsgEvent;
import com.sanyinggroup.corp.urocissa.server.api.service.MsgServiceHandler;
import com.sanyinggroup.corp.urocissa.server.api.service.MsgServiceHandlerRegister;


/**
 * App容器-管理背调子任务应用的App
 * <P>描述：用于维护背调可用的App，每个App下配置的Client。以及维护当前在线的Client信息。</P>
 * @author liujun
 * @date 2018年1月18日 上午10:34:11
 */
public class AppContainer {
	
	private static final Logger logger =Logger.getLogger(AppContainer.class);

	private String                           serverIp;
	private String                           serverPort;
	
	/**用户单例线程锁*/
	private static Boolean lockSigleton = true; 
	private static AppContainer appContainer;
	
	/**配置的app，由于底层没有code，用appKey记录，所以监听底层的Client用AppKey确定。【appKey--(SurveyApp--SurveyClientList)】*/
	public static Map<String,SurveyApp> appHolder=new HashMap<String,SurveyApp>();
	
	/**配置的app，任务用Code记录（在任务处理中，因为子任务中使用AppCode指明使用的App类型，这是从产品那边配置的，不太可能用AppKey这个不是很清晰的码）
	 * 【appCode--(SurveyApp--SurveyClientList)】
	 */
	public static Map<String,SurveyApp> appCodeHolder=new HashMap<String,SurveyApp>();
	
	/**配置的Client（ClientCode--SurveyClient）*/
	public static Map<String,SurveyClient> clientHolder=new HashMap<String,SurveyClient>();
	
	/**可维护的Client在线列表。
	 * <key为appKey(代表一类客户端)，内部一组客户端，有并发控制<sessionId,client>>【appKey--(sessionId--onlineClient)】
	 */
	public static volatile Map<String, ConcurrentHashMap<String, SurveyClient>> onlineClientMap=new HashMap<String, ConcurrentHashMap<String,SurveyClient>>();
	
	
	/**根据配置的SurveyClient，生成ClientApp列表，仅用于启动中间件前给底层中间件传递*/
	private static List<ClientApp> clientAppList=new ArrayList<ClientApp>();
	
	private TaskContainer taskContainer;
	
	
	public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public boolean isMiddlewearStarted=false;
	
	/**
	 * 配置测试用的app与client的字典表数据
	 * @return 
	 */
	public static void setDefaultAppClient(){
		logger.info("----------第一组配置-----------");
		SurveyApp surveyApp=new SurveyApp("loan","贷款信息","5732f9a0bb7348258cfe53fa0160162d","eeeeewerw34rwer","这个是获取贷款信息");
		SurveyClient surveyClient=new SurveyClient("loan_kaola","贷款_考拉","80",surveyApp);
//		SurveyClient surveyClient2=new SurveyClient("loan_wanda","贷款_万达","50","供一般客户使用");
//		Map<String,SurveyClient> clientHolder =new HashMap<String,SurveyClient>();
		List<SurveyClient> surveyClientList=new ArrayList<SurveyClient>();
		surveyClientList.add(surveyClient);
		surveyApp.setClientList(surveyClientList);//App配置的客户端
		
		appHolder.put(surveyApp.getAppKey(),surveyApp);
		appCodeHolder.put(surveyApp.getCode(),surveyApp);
		//初始化几个容器
		clientHolder.put(surveyClient.getClientCode(), surveyClient);
		TaskContainer.clientSubTaskInfoMap.put(surveyClient.getClientCode(), new ArrayList<SubTaskInfo>());
		onlineClientMap.put(surveyApp.getAppKey(), new ConcurrentHashMap<String, SurveyClient>());
		
		logger.info("----------第二组配置-----------");
		
		surveyApp=new SurveyApp("dishonest","失信信息","3f00c0cb49ea4f90a78a06f16e27c218","ssssdfsdfsdf","无");
		surveyClient=new SurveyClient("dishonest_kaola","失信_考拉","80",surveyApp);
//		surveyClient2=new SurveyClient("dishonest_wanda","失信_万达","50","供一般客户使用");
//		clientHolder =new HashMap<String,SurveyClient>();
		surveyClientList=new ArrayList<SurveyClient>();
		surveyClientList.add(surveyClient);
		surveyApp.setClientList(surveyClientList);
		appHolder.put(surveyApp.getAppKey(),surveyApp );
		appCodeHolder.put(surveyApp.getCode(),surveyApp);
		//初始化几个容器
		clientHolder.put(surveyClient.getClientCode(), surveyClient);
		TaskContainer.clientSubTaskInfoMap.put(surveyClient.getClientCode(), new ArrayList<SubTaskInfo>());
		onlineClientMap.put(surveyApp.getAppKey(), new ConcurrentHashMap<String, SurveyClient>());
		
		logger.info("----------通讯层对象产生-----------");
		//根据配置的Client，产生底层通讯用的ClientApp
    	Iterator iter = appHolder.entrySet().iterator(); 
    	while (iter.hasNext()){ 
    	    Map.Entry<String, SurveyApp> entry = (Map.Entry<String, SurveyApp>) iter.next(); 
    	    surveyApp=entry.getValue();
    	    ClientApp eachClient=new ClientApp();//通讯层用的客户对象
    	    eachClient.setAppKey(surveyApp.getAppKey());
    	    eachClient.setAppSecret(surveyApp.getAppScret());
    	    logger.info("根据App配置生成通讯Client:"+surveyApp.getAppKey()+"|"+surveyApp.getAppScret());
    	    clientAppList.add(eachClient);
    	} 
    	logger.info("----------默认配置完成-----------");
	}
	
	/**
	 * <P>注册app种类列表与所属的client列表,并设置给通讯中间件使用</P>
	 * 
	 * @param surveyAppList
	 */
	public static void registeAppClientInfo(List<SurveyApp> surveyAppList){
		if(surveyAppList==null || surveyAppList.size()==0){
			logger.info("----------无传入配置项，使用默认配置-----------");
			setDefaultAppClient();
		}else{
			for(SurveyApp surveyApp:surveyAppList){
				//通讯层用的客户对象,一个app只有一个，实际中上报后sessionId不同
				ClientApp eachClient=new ClientApp();
				eachClient.setAppKey(surveyApp.getAppKey());
				eachClient.setAppSecret(surveyApp.getAppScret());
				logger.info("根据App配置生成通讯Client:"+surveyApp.getAppKey()+"|"+surveyApp.getAppScret());
				clientAppList.add(eachClient);
				//配置的app记录
				appHolder.put(surveyApp.getAppKey(),surveyApp);
				appCodeHolder.put(surveyApp.getCode(),surveyApp);
				for(SurveyClient surveyClient:surveyApp.getClientList()){
					//配置的客户端记录
					clientHolder.put(surveyClient.getClientCode(), surveyClient);
					//配置客户端下的子任务记录
					TaskContainer.clientSubTaskInfoMap.put(surveyClient.getClientCode(), new ArrayList<SubTaskInfo>());
					//App下的动态客户端记录---//应该放在外层循环，待完善。
					onlineClientMap.put(surveyApp.getAppKey(), new ConcurrentHashMap<String, SurveyClient>());
				}
			}
		}
	}
	
	/**
	 * 生成App容器的单例对象
	 * <P></P>
	 * @param taskContainer
	 * @return
	 */
	public static AppContainer init(TaskContainer taskContainer){
		if (appContainer == null) {
			synchronized (lockSigleton) {
				if (appContainer == null) {
					appContainer = new AppContainer(taskContainer);
				}
			}
		}
		return appContainer;
	}
	
	/**
	 * 构造函数，将App配置容器与Task任务容器关联
	 */
    private AppContainer(TaskContainer taskContainer){
    	this.taskContainer=taskContainer;
    	this.taskContainer.setAppContainer(this);
    	logger.info("appHolder size():"+appHolder.size());
    }
    
    /**
     * 设置启动通讯的IP与端口,IP暂不使用
     * <P></P>
     * @param ip 
     * @param port
     */
    public void setIPPort(String ip,String port){
        this.serverIp=ip;
        this.serverPort=port;
    }
    
    
    
    
    public String getServerIp() {
		return serverIp;
	}

	public String getServerPort() {
		return serverPort;
	}

	/**
     * 根据权重，从可用的客户端中选择出最终使用的客户端
     * <P></P>
     * @param appCode
     * @return
     */
	public static SurveyClient getWeightRandomClient(String appCode) {
		// 从appCode得到appKey，从而找到可用的在线客户端.让前端根据appKey来分子任务不可靠
		SurveyApp surveyApp = appCodeHolder.get(appCode);
		Map<String, SurveyClient> onlineSurveyClientMap = onlineClientMap.get(surveyApp.getAppKey());
		int onlineClientNum=onlineSurveyClientMap.size();
		logger.info("【策略】surveyApp(code|在线客户端数):"+appCode+"|" + onlineClientNum);
		// 用于权重随机的参数对象
//		logger.info("【策略】可选用客户端的数:" + onlineClientNum);
		Map<String, Integer> canUseClient = new HashMap<String, Integer>();
		if (onlineClientNum == 0) {
			logger.info("【策略】备选客户端为0，返回!");
			return null;
		}else {
			Iterator iter = onlineSurveyClientMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, SurveyClient> entry = (Map.Entry<String, SurveyClient>) iter.next();
				Integer eachWeight = new Integer(entry.getValue().getWeight() == null ? Constants.CLIENT_BASE_LEVER+"" : entry.getValue().getWeight());
				canUseClient.put(entry.getKey(), eachWeight);
				logger.info("【策略】循环可选用客户端详细信息(sessionId|weight):" + entry.getKey() + "|" + eachWeight);
				if(onlineClientNum==1) return entry.getValue();
			}
		}
		logger.info("【策略】准备筛选的客户端个数："+canUseClient.size());
		WeightRandomSelect weightRandom = new WeightRandomSelect(canUseClient);
		String randomKey = (String) weightRandom.random();
		return onlineSurveyClientMap.get(randomKey);
		
	}
    
	/**
     * 根据客户端，从可用的客户端中选择出最终使用的客户端
     * <P></P>
     * @param appCode
     * @return
     */
	public static SurveyClient getClientByUserRankAndClinetLever(String appCode,int userRank) {
		// 从appCode得到appKey，从而找到可用的在线客户端.让前端根据appKey来分子任务不可靠
		SurveyApp surveyApp = appCodeHolder.get(appCode);
		logger.debug("【策略2】未配置客户端，返回!appKey:"+appCode);
		return getClientByUserRankAndClinetLeverByAppkey(surveyApp.getAppKey(),userRank);
	}
	public static SurveyClient getClientByUserRankAndClinetLeverByAppkey(String appKey,int userRank) {
		// 从appCode得到appKey，从而找到可用的在线客户端.让前端根据appKey来分子任务不可靠
//		SurveyApp surveyApp = appCodeHolder.get(appCode);
		Map<String, SurveyClient> onlineSurveyClientMap = onlineClientMap.get(appKey);
		if(onlineSurveyClientMap==null){
			logger.info("【策略2】未配置客户端，返回!appKey:"+appKey);
			return null;
		}
		
		int onlineClientNum=onlineSurveyClientMap.size();
		logger.info("【策略2】surveyApp(code|在线客户端数):"+appKey+"|" + onlineClientNum);
		// 用于权重随机的参数对象
//		logger.info("【策略】可选用客户端的数:" + onlineClientNum);
		Map<String, Integer> canUseClient = new HashMap<String, Integer>();//用于treemap排序。
		List<SurveyClient> sameLeverClientList = new LinkedList<SurveyClient>();//用于同级别客户端存放
		if (onlineClientNum == 0) {
			logger.info("【策略2】备选客户端为0，返回!");
			return null;
		}else {
			Iterator iter = onlineSurveyClientMap.entrySet().iterator();
			while (iter.hasNext()) {
				
				Map.Entry<String, SurveyClient> entry = (Map.Entry<String, SurveyClient>) iter.next();
				Integer eachWeight = new Integer(entry.getValue().getWeight() == null ? Constants.CLIENT_BASE_LEVER+"" : entry.getValue().getWeight());
				canUseClient.put(entry.getKey(), eachWeight);
				logger.info("【策略2】循环可选用客户端详细信息(sessionId|weight):" + entry.getKey() + "|" + eachWeight);
				if(onlineClientNum==1) return entry.getValue();
			}
		}
		
		//找出可用的客户端，产生一个map，再构建treemap，用策略得到一个值。
		logger.info("【策略2】准备筛选的客户端个数："+canUseClient.size());//canUseClient不含有重复的，所以得到选择的客户端值，还要再处理多个同值的情况。
		RankAndLeverTreeMapSelect rankAndLeverTreeMapSelect = new RankAndLeverTreeMapSelect(canUseClient,0);
		Integer chooseValue = rankAndLeverTreeMapSelect.chooseValue();
		logger.info("【策略2】选择出的客户端lever："+chooseValue);
		if(chooseValue==null) return null;
		for(SurveyClient surveyClient:onlineSurveyClientMap.values()){
			if(surveyClient.getWeight() == null) surveyClient.setWeight(Constants.CLIENT_BASE_LEVER+"");
			logger.info("【策略2】当前比对的surveyClient.getWeight(null=60)："+(surveyClient.getWeight()));
			//if(StringUtils.isBlank(surveyClient.getWeight())) continue;//没有就是60分
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
		
		//如果找到的客户端不能用，就递归找一个，同时移除这个客户端
		SecretManagement m = ServerGlobal.sessionWithAppKeys.get(surveyClient.getSessionId());
		if(m!=null  && m.getChannel()!=null && m.getChannel().isWritable()){
			return surveyClient;
		}
		else{
			Map<String, SurveyClient> surveyClientMap=AppContainer.onlineClientMap.get(appKey);
			if(surveyClientMap.containsKey(surveyClient.getSessionId())){
				logger.info("【策略。推送失败移除客户端再递归获取】sessionId:"+surveyClient.getSessionId());
				surveyClientMap.remove(surveyClient.getSessionId());// 根据通讯客户端，移除里面的调查客户端对象
			}
			return getClientByUserRankAndClinetLeverByAppkey(appKey,userRank);
		}
	}
	
    /**
     * 更新在线客户端
     * @param clientCode
     */
	@Deprecated
    public void updateClientOnline(String clientCode){
    	
    }
    
    /**
     * 启动背调中心的通讯中间件服务
     * <P></P>
     */
    public void initMiddlerWareStart(){
		logger.info("【背调中心】注册客户心跳消息与任务完成消息的回调处理");
		try {
			// 获取应用client基本信息
			MsgServiceHandlerRegister register = MsgServiceHandlerRegister.getRegister();
			//注册事件处理类
			MsgServiceHandlerRegister.setEventHandlerClass(ClientStatusListener.class);
			logger.info("1.【背调中心】注册客户事件处理类成功");
			//注册消息处理类
			register.addMsgServiceHandler("getClientSystemInfo",RecvClientInfoHandler.class);
			logger.info("2.【背调中心】注册客户心跳消息回调处理成功");
			
			register.addMsgServiceHandler("subTaskFinishInfo", RecvClientTaskHandler.class);
			logger.info("3.【背调中心】注册客户任务完成消息回调处理成功");

			new Thread(new Runnable(){
			    @Override
			    public void run() {
			        // TODO Auto-generated method stub
			    	try {
			    		isMiddlewearStarted=true;
			    		ServerInit.init(StringUtils.isEmpty(serverPort)?9166:Integer.parseInt(serverPort), clientAppList);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						isMiddlewearStarted=false;
					}
			    }
			}).start();
			logger.info("【背调中心】启动消息服务成功！端口：" + serverPort);
		} catch (Exception e) {
			isMiddlewearStarted=false;
			e.printStackTrace();
			logger.error("【背调中心】启动消息服务失败！异常：" + e.toString());
		}
	
    }
	
}
