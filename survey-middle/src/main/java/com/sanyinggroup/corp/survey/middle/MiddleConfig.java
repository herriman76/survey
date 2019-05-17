package com.sanyinggroup.corp.survey.middle;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sanyinggroup.corp.urocissa.core.util.PropertyFileHandle;
import com.sanyinggroup.corp.urocissa.core.util.SecretManagement;
import com.sanyinggroup.corp.urocissa.server.api.info.ClientApp;
import com.sanyinggroup.corp.urocissa.server.api.model.MiddleMsg;
import com.sanyinggroup.corp.urocissa.server.util.MsgSignTool;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * 
 * <p>Package:com.sanyinggroup.communication.server</p> 
 * <p>Title:ServerConfig</p> 
 * <p>Description: 服务器端配置</p> 
 * @author lixiao
 * @date 2017年7月19日 下午6:11:15
 * @version 0.1
 */
public class MiddleConfig {
	private static final Logger logger =Logger.getLogger(MiddleConfig.class);
	
	private static String appKey = "";
	private static String appSecret = "";
//	private static String serverIps = "";
	private static String serverPort = "";
	private static String type = "";
	
	private static String weight = "";
	private static String clientCode = "";
	private static String clientDiscription = "";
	
	private static String servers = "";
	
	private static String useTestDate="false";
	
	
	private static String failureSubTaskMaxRetryTimes="2000000";
	private static String failureSubTaskMaxRetryMilliseconds="1200000";
	
	

	
	public static String getAppKey() {
		return appKey;
	}
	public static void setAppKey(String appKey) {
		MiddleConfig.appKey = appKey;
	}
	public static String getAppSecret() {
		return appSecret;
	}
	public static void setAppSecret(String appSecret) {
		MiddleConfig.appSecret = appSecret;
	}
//	public static String getServerIps() {
//		return serverIps;
//	}
//	public static void setServerIps(String serverIps) {
//		MiddleConfig.serverIps = serverIps;
//	}

	public static String getServerPort() {
		return serverPort;
	}
	public static void setServerPort(String serverPort) {
		MiddleConfig.serverPort = serverPort;
	}
	public static String getType() {
		return type;
	}
	public static void setType(String type) {
		MiddleConfig.type = type;
	}
	public static String getWeight() {
		return weight;
	}
	public static void setWeight(String weight) {
		MiddleConfig.weight = weight;
	}
	public static String getClientCode() {
		return clientCode;
	}
	public static void setClientCode(String clientCode) {
		MiddleConfig.clientCode = clientCode;
	}
	public static String getClientDiscription() {
		return clientDiscription;
	}
	public static void setClientDiscription(String clientDiscription) {
		MiddleConfig.clientDiscription = clientDiscription;
	}
		
	public static String getUseTestDate() {
		return useTestDate;
	}
	public static void setUseTestDate(String useTestDate) {
		MiddleConfig.useTestDate = useTestDate;
	}
	public static String getServers() {
		return servers;
	}
	public static void setServers(String servers) {
		MiddleConfig.servers = servers;
	}

	public static String getFailureSubTaskMaxRetryTimes() {
		return failureSubTaskMaxRetryTimes;
	}
	public static void setFailureSubTaskMaxRetryTimes(String failureSubTaskMaxRetryTimes) {
		MiddleConfig.failureSubTaskMaxRetryTimes = failureSubTaskMaxRetryTimes;
	}
	public static String getFailureSubTaskMaxRetryMilliseconds() {
		return failureSubTaskMaxRetryMilliseconds;
	}
	public static void setFailureSubTaskMaxRetryMilliseconds(String failureSubTaskMaxRetryMilliseconds) {
		MiddleConfig.failureSubTaskMaxRetryMilliseconds = failureSubTaskMaxRetryMilliseconds;
	}





	private static String IP = "127.0.0.1"; //监听ip 默认本机 127.0.0.1
	private static int PORT = 9166; // 监听端口 默认 9166
	//白名单
	private static List<String> whiteList = new ArrayList<String>(); 
	//黑名单
	private static List<String> blackList = new ArrayList<String>();
	//入站规则  0 不做检查 1：白名单  2 ：黑名单
	private static int inboundRule = 0;
	private  static boolean isInit = false; //是否已经初始化    如果手动调用每个set方法设置参数，请把这个值设为true
	/**
	 * Map<appkey,Map<appKey,ClientApp>>
	 */
	private  static Map<String,ClientApp> appKeys =  new HashMap<String,ClientApp>();
	
	public static String getIP() {
		return IP;
	}
	/**
	 * <p>Title:setIP</p> 
	 * <p>Description: 设置服务器ip</p> 
	 * @date 2017年7月19日 下午6:16:48
	 * @version 
	 * @return void
	 * @param ip
	 */
	public static void setIP(String ip) {
		IP = ip==null?"127.0.0.1":ip;
	}
	public static int getPORT() {
		return PORT;
	}
	/**
	 * <p>Title:setPORT</p> 
	 * <p>Description: 设置启动端口</p> 
	 * @date 2017年7月19日 下午6:20:29
	 * @version 
	 * @return void
	 * @param port
	 */
	public static void setPORT(int port) {
		PORT =  port<=0?9166:port;
	}
	/**
	 * <p>Title:getWhiteList</p> 
	 * <p>Description: 获取当前白名单列表</p> 
	 * @date 2017年8月2日 上午9:52:50
	 * @version 
	 * @return List<String>
	 * @return
	 */
	public static List<String> getWhiteList() {
		return whiteList ==null?new ArrayList<String>() : whiteList;
	}
	/**
	 * <p>Title:setWhiteList</p> 
	 * <p>Description: 设置白名单 </p> 
	 * @date 2017年7月19日 下午6:20:53
	 * @version 
	 * @return void
	 * @param whiteList
	 */
	public static void setWhiteList(List<String> whiteList) {
		MiddleConfig.whiteList = whiteList;
	}
	/**
	 * <p>Title:addWhiteList</p> 
	 * <p>Description: 增加白名列表</p> 
	 * @date 2017年8月2日 上午9:54:57
	 * @version 
	 * @return List<String>
	 * @param whiteList
	 * @return
	 */
	public static List<String> addWhiteList(List<String> whiteList) {
		MiddleConfig.whiteList.addAll(whiteList);
		return MiddleConfig.whiteList;
	}
	/**
	 * <p>Title:addWhite</p> 
	 * <p>Description: 增加白名单 </p> 
	 * @date 2017年8月2日 上午9:55:56
	 * @version 
	 * @return List<String>
	 * @param white
	 * @return
	 */
	public static List<String> addWhite(String white) {
		MiddleConfig.whiteList.add(white);
		return MiddleConfig.whiteList;
	}
	
	/**
	 * <p>Title:getBlackList</p> 
	 * <p>Description: 获取黑名单列表</p> 
	 * @date 2017年8月2日 上午9:59:00
	 * @version 
	 * @return List<String>
	 * @return
	 */
	public static List<String> getBlackList() {
		return blackList==null?new ArrayList<String>() : blackList;
	}
	/**
	 * <p>Title:setBlackList</p> 
	 * <p>Description: 设置黑名单</p> 
	 * @date 2017年8月2日 上午9:56:18
	 * @version 
	 * @return void
	 * @param blackList
	 */
	public static void setBlackList(List<String> blackList) {
		MiddleConfig.blackList = blackList;
	}
	/**
	 * 
	 * <p>Title:addBlackList</p> 
	 * <p>Description: 增加黑名单列表</p> 
	 * @date 2017年8月2日 上午10:01:52
	 * @version 
	 * @return List<String>
	 * @param blackList
	 * @return
	 */
	public static List<String> addBlackList(List<String> blackList) {
		MiddleConfig.blackList.addAll(blackList);
		return blackList;
	}
	/**
	 * <p>Title:addBlack</p> 
	 * <p>Description: 增加黑名单</p> 
	 * @date 2017年8月2日 上午10:02:11
	 * @version 
	 * @return void
	 * @param black
	 */
	public static void addBlack(String black) {
		MiddleConfig.blackList.add(black);
	}
	/**
	 * <p>Title:getInboundRule</p> 
	 * <p>Description: 入站规则  0 不做检查 1：白名单  2 ：黑名单</p> 
	 * @date 2017年8月2日 上午9:48:19
	 * @version 
	 * @return int
	 */
	public static int getInboundRule() {
		return inboundRule;
	}
	/**
	 * 
	 * <p>Title:setInboundRule</p> 
	 * <p>Description: 入站规则  0  不做检查 1：白名单  2 ：黑名单</p> 
	 * @date 2017年8月2日 上午9:48:03
	 * @version 
	 * @return void
	 * @param inboundRule
	 */
	public static void setInboundRule(int inboundRule) {
		MiddleConfig.inboundRule = inboundRule;
	}
	
	public static Map<String, ClientApp> getAppKeys() {
		return appKeys;
	}
	public static void setAppKeys(Map<String, ClientApp> appKeys) {
		MiddleConfig.appKeys = appKeys;
	}
	/**
	 * 
	 * <p>Title:initWithConfig</p> 
	 * <p>Description: 根据配置文件初始化 </p> 
	 * @date 2017年7月20日 上午11:30:08
	 * @version 
	 * @return void
	 * @param configPath
	 * @throws IOException
	 */
	public  static void  initWithConfig(String configPath) throws IOException{
		Map<String,String> map = PropertyFileHandle.read(configPath);
		//System.out.println("启动配置信息：\n"+map);
		logger.info("启动配置信息：\n"+map);
		
//		appKey=54645rw2354325a
//				appSecret=ababbaaa
//				serverIps=192.168.117.35
//				serverPorts=9166
//				type=LINF
//				weight=75
//				clientCode=loan_middle
//				clientDiscription="\u8D37\u6B3E\u4FE1\u606F-\u4E2D\u95F4\u5C42"
		if(map.get("appKey")!=null && !("").equals(map.get("appKey"))){
			appKey=map.get("appKey");
		}
		if(map.get("appSecret")!=null && !("").equals(map.get("appSecret"))){
			appSecret=map.get("appSecret");
		}
//		if(map.get("serverIps")!=null && !("").equals(map.get("serverIps"))){
//			serverIps=map.get("serverIps");
//		}
		if(map.get("serverPort")!=null && !("").equals(map.get("serverPort"))){
			serverPort=map.get("serverPort");
		}
		if(map.get("type")!=null && !("").equals(map.get("type"))){
			type=map.get("type");
		}
		
		if(map.get("weight")!=null && !("").equals(map.get("weight"))){
			weight=map.get("weight");
		}
		if(map.get("clientCode")!=null && !("").equals(map.get("clientCode"))){
			clientCode=map.get("clientCode");
		}
		if(map.get("clientDiscription")!=null && !("").equals(map.get("clientDiscription"))){
			clientDiscription=map.get("clientDiscription");
		}
		if(map.get("servers")!=null && !("").equals(map.get("servers"))){
			servers=map.get("servers");
		}
		if(map.get("useTestDate")!=null && !("").equals(map.get("useTestDate"))){
			useTestDate=map.get("useTestDate");
		}
		
		if(map.get("failureSubTaskMaxRetryTimes")!=null && !("").equals(map.get("failureSubTaskMaxRetryTimes"))){
			failureSubTaskMaxRetryTimes=map.get("failureSubTaskMaxRetryTimes");
		}
		if(map.get("failureSubTaskMaxRetryMilliseconds")!=null && !("").equals(map.get("failureSubTaskMaxRetryMilliseconds"))){
			failureSubTaskMaxRetryMilliseconds=map.get("failureSubTaskMaxRetryMilliseconds");
		}
		
		logger.info("【启动配置信息】appKey---------:"+map.get("servers")+"[useTestDate]"+useTestDate);
		
		
		//设置appkey
		if(map.get("appKeys")!=null && !("").equals(map.get("appKeys"))){
			logger.info("【启动配置信息】appKeys:"+map.get("appKeys"));
			JSONArray json = JSONArray.fromObject(map.get("appKeys")); // 首先把字符串转成 JSONArray  对象
			if(json.size()>0){
			  for(int i=0;i<json.size();i++){
			    ClientApp clientApp = (ClientApp) JSONObject.toBean(json.getJSONObject(i), ClientApp.class);   
			    //System.out.println(clientApp) ;  // 日志输出
			    appKeys.put(clientApp.getAppKey(), clientApp);
			  }
			}
		}
		
		
		
		//设置白名单
		logger.info("【启动配置信息】whiteList:"+map.get("whiteList"));
		if(map.get("whiteList")!=null && !("").equals(map.get("whiteList"))){
			String[] str = map.get("whiteList").split(";");
			if(str!=null && str.length>0){
				whiteList.addAll(Arrays.asList(str));
			}
		}
		//设置黑名单
		logger.info("【启动配置信息】blackList:"+map.get("blackList"));
		if(map.get("blackList")!=null && !("").equals(map.get("blackList"))){
			String[] str = map.get("blackList").split(";");
			if(str!=null && str.length>0){
				blackList.addAll(Arrays.asList(str));
			}
		}
		//设置端口
		logger.info("【启动配置信息】port:"+map.get("port"));
		if(map.get("port")!=null && !("").equals(map.get("port"))){
			PORT = Integer.parseInt(map.get("port"));
		}
		//设置入站规则
		if(map.get("inboundRule")!=null && !("").equals(map.get("inboundRule"))){
			inboundRule = Integer.parseInt(map.get("inboundRule"))<=0?0:Integer.parseInt(map.get("inboundRule"))>3?3:Integer.parseInt(map.get("inboundRule"));
		}
		
	}

	/**
	 * <p>Title:init</p> 
	 * <p>Description: 默认初始化，加载 config.properties</p> 
	 * @date 2017年8月2日 下午1:48:58
	 * @version 
	 * @return void
	 * @param configPaht  properties文件路径
	 * @throws IOException
	 */
	public static void  init(String configPaht) throws IOException{
		if(configPaht==null || ("").equals(configPaht));
			configPaht = "middle.properties";
		initWithConfig(configPaht);
		isInit = true;
	}
	/**
	 * 
	 * <p>Title:init</p> 
	 * <p>Description: 初始化启动参数</p> 
	 * @date 2017年8月2日 上午10:16:03
	 * @version 
	 * @param ip 监听ip 默认本机 127.0.0.1
	 * @param port  默认 9166
	 * @param whiteList //白名单
	 * @param blackList
	 * @param inboundRule 入站规则  0  不做检查 1：白名单过滤  2 ：黑名单过滤
	 * @param appKeys @see Map<appkey,ClientApp>
	 */
	@Deprecated
	public static void  init(String ip,int port , List<String> whiteList,List<String> blackList,int inboundRule,Map<String,ClientApp> appKeys) {
		IP = ip==null?"127.0.0.1":ip;
		PORT = port<=0?9166:port;
		if(whiteList!=null && whiteList.size()>0){
			MiddleConfig.whiteList = whiteList;
			//ServerConfig.whiteList.addAll(whiteList);
		}else{
			MiddleConfig.whiteList.clear();
		}
		if(blackList!=null && blackList.size()>0){
			MiddleConfig.blackList = blackList;
		}else{
			MiddleConfig.blackList.clear();
		}
		MiddleConfig.inboundRule = inboundRule;
		if(appKeys!=null){
			MiddleConfig.appKeys = appKeys;
		}
		isInit = true;
	}
	/**
	 * 
	 * <p>Title:init</p> 
	 * <p>Description: 初始化启动参数</p> 
	 * @date 2017年8月2日 上午10:16:03
	 * @since 2.0.0 
	 * @param ip 监听ip 默认本机 127.0.0.1
	 * @param port  默认 9166
	 * @param whiteList //白名单
	 * @param blackList
	 * @param inboundRule 入站规则  0  不做检查 1：白名单过滤  2 ：黑名单过滤
	 * @param apps @see ClientApp[]
	 */
	public static void  init(String ip,int port , List<String> whiteList,List<String> blackList,int inboundRule,ClientApp...apps) {
		IP = ip==null?"127.0.0.1":ip;
		PORT = port<=0?9166:port;
		if(whiteList!=null && whiteList.size()>0){
			MiddleConfig.whiteList = whiteList;
			//ServerConfig.whiteList.addAll(whiteList);
		}else{
			MiddleConfig.whiteList.clear();
		}
		if(blackList!=null && blackList.size()>0){
			MiddleConfig.blackList = blackList;
		}else{
			MiddleConfig.blackList.clear();
		}
		MiddleConfig.inboundRule = inboundRule;
		if(apps!=null && apps.length>0){
			Map<String,ClientApp> appKeys = new HashMap<String,ClientApp>();
			for(ClientApp app:apps) {
				appKeys.put(app.getAppKey(), app);
			}
			MiddleConfig.appKeys = appKeys;
		}
		isInit = true;
	}
	/**
	 * 
	 * <p>Title:init</p> 
	 * <p>Description: </p> 
	 * @date 2017年8月2日 下午1:41:07
	 * @version 
	 * @return void
	 * @param ip 监听ip 默认本机 127.0.0.1
	 * @param port 默认 9166
	 * @param appKeys @see Map<appkey,ClientApp>
	 */
	@Deprecated
	public static void init(String ip,int port,Map<String,ClientApp> appKeys){
		init(ip, port, null, null, 0,appKeys);
	}
	/**
	 * 
	 * <p>Title:init</p> 
	 * <p>Description: </p> 
	 * @date 2017年8月2日 下午1:41:07
	 * @since 2.0.0
	 * @return void
	 * @param ip 监听ip 默认本机 127.0.0.1
	 * @param port 默认 9166
	 * @param apps @see ClientApp[]
	 */
	public static void init(String ip,int port,ClientApp...apps){
		init(ip, port, null, null, 0,apps);
	}
	/**
	 * @return the isInit
	 */
	public static boolean isInit() {
		return isInit;
	}
	/**
	 * @param isInit 如果手动调用每个set方法设置参数，请调用这个方法把参数设为true
	 */
	public static void setInit(boolean isInit) {
		MiddleConfig.isInit = isInit;
	}
	/**
	 * 
	 * <p>Title:checkIp</p> 
	 * <p>Description: ip 检查 是否允许连接</p> 
	 * <p>根据入站规则检查{@see  inboundRule}</p>
	 * @date 2017年7月20日 下午1:26:23
	 * @version 
	 * @return boolean true：允许| false:不允许
	 * @param ip
	 * @return
	 */
	public static boolean checkIp(String ip){
		boolean isOK = true;
		if(MiddleConfig.getInboundRule()!=0){ // 0 不做ip校验
			isOK = MiddleConfig.getInboundRule()==1?false:true; //1：白名单校验 2： 黑名单校验
			if(isOK){ 
				//黑名单
				for (String WIP : MiddleConfig.getBlackList()) {
					if (WIP.equals(ip)) {
						isOK = false;
						break;
					}
				}
			}else{
				for (String WIP : MiddleConfig.getWhiteList()) {
					if (WIP.equals(ip)) {
						isOK = true;
						break;
					}
				}
			}
		}
		return isOK;
	}
	/**
	 * <p>Title:checkAppKey</p> 
	 * <p>Description: 检查 appkey是否合法</p> 
	 * @date 2017年7月20日 下午1:41:00
	 * @version 
	 * @return boolean
	 * @param appKey
	 * @return
	 */
	public static boolean checkAppKey(String appkey){
		boolean bln = false;
		if(null != appkey && !("").equals(appkey)){
			ClientApp clientApp = MiddleConfig.getAppKeys().get(appkey);
			if(clientApp !=null){
				bln = clientApp.getStatus()>=0?true:false;
			}
		}
		return bln;
	}
	/**
	 * <p>Title:checkSign</p> 
	 * <p>Description: 登录签名认证，此时只根据appSecret去判断签名是否正确</p> 
	 * @date 2017年8月30日 下午3:35:32
	 * @return boolean
	 * @param appkey
	 * @param msg
	 * @return
	 * @since
	 */
	public static boolean loginCheckSign(String appkey,MiddleMsg msg){
		boolean bln = false;
		if(null != appkey && !("").equals(appkey)){
			ClientApp clientApp = MiddleConfig.getAppKeys().get(appkey);
			if(clientApp !=null){
				bln = MsgSignTool.verifySign(clientApp.getAppSecret(), msg);
			}
		}
		return bln ;
	}

	
	
}
