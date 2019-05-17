package com.sanyinggroup.corp.survey.client.container;

import io.netty.buffer.ByteBuf;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.sanyinggroup.corp.survey.client.ApiInvorkerInterface;
import com.sanyinggroup.corp.survey.client.SurveyException;
import com.sanyinggroup.corp.survey.client.para.ClientRealData;
import com.sanyinggroup.corp.survey.client.para.CommonReturnData;
import com.sanyinggroup.corp.survey.client.para.SubTaskData;
import com.sanyinggroup.corp.survey.client.tools.JsonUtils;
import com.sanyinggroup.corp.urocissa.client.api.handler.MsgHandler;
import com.sanyinggroup.corp.urocissa.client.api.handler.PushReceiver;
import com.sanyinggroup.corp.urocissa.client.api.handler.PushReceiverCenter;
import com.sanyinggroup.corp.urocissa.client.event.AbstractEventHandler;
import com.sanyinggroup.corp.urocissa.client.init.Client;
import com.sanyinggroup.corp.urocissa.client.init.ClientCenter;
import com.sanyinggroup.corp.urocissa.client.model.MiddleMsg;
import com.sanyinggroup.corp.urocissa.client.model.ResultObject;

/**
 * 背调中间层容器-管理通讯层并处理背调任务
 * @author liujun
 *
 */
public class ClientContainer {

	private static final Logger logger = Logger.getLogger(ClientContainer.class);

	public static final long THEARTBEAT_INTERVAL = 3 * 1000L;
	/**appKey-同类的客户端相同*/
	private String appkey;
	/**appSec-同类的客户端相同*/
	private String appSecret;
	/**客户端标识Code*/
	private String clientCode;
	/**所连接服务器IP*/
	private String serverIP;
	/**此客户端的权重*/
	private String weight;
	/**通讯层客户端*/
	Client client = null;
	/**子任务处理接口对象*/
	ApiInvorkerInterface apiInvorkerInterface;
	/**监听器-监听通讯层客户端状态*/
	ClientStatusListener clientStatusListener;
	/**客户端上报状态传输对象*/
	ClientRealData clientRealData;
	/**客户端是否连接状态标识*/
	private boolean isConnected = false;
	private Object lock=new Object();
//	ScheduledExecutorService service = Executors.newScheduledThreadPool(1);//不需要线程池，只要一个循环的守护线程
	/**用户单例线程锁*/
	private static Boolean lockSigleton = true;
	/**用户单例对象*/
	private static ClientContainer clientContainer;


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
	public static ClientContainer init(String serverIP,String clientCode,String appkey,String appSecret,String weight,String description,ApiInvorkerInterface apiInvorkerInterface) {
		logger.info("【客户端容器】启动中...");
		if (clientContainer == null) {
			synchronized (lockSigleton) {
				if (clientContainer == null) {
					clientContainer = new ClientContainer(serverIP,clientCode,appkey,appSecret,weight,description,apiInvorkerInterface);
				}
			}
			clientContainer.registSubTaskDealClass();
			clientContainer.notifyHeartBeat();
			clientContainer.startThread();
		}
		logger.info("【客户端容器】启动完成...");
		return clientContainer;
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
	private ClientContainer(String serverId,String clientCode,String appkey,String appSecret,String weight,String description,ApiInvorkerInterface apiInvorkerInterface){
		this.serverIP=serverId;
		this.appkey=appkey;
		this.appSecret=appSecret;
		clientRealData=new ClientRealData();
		clientRealData.setClientCode(clientCode);
		clientRealData.setAppkey(appkey);
		clientRealData.setWeight(weight);
		clientRealData.setDescription(description);
		clientRealData.setSiteIp(serverId);
		this.apiInvorkerInterface = apiInvorkerInterface==null?new DemoApiService():apiInvorkerInterface;
		clientStatusListener = new ClientStatusListener();
	}
	
	/**
	 * 获取客户端容器单例对象
	 * @return
	 */
	public static ClientContainer getInstance(){
		return clientContainer;
	}
	
	public String getServerIP() {
		return serverIP;
	}


	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}


	public String getAppkey() {
		return appkey;
	}

	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}

	public String getClientCode() {
		return clientCode;
	}

	public void setClientCode(String clientCode) {
		this.clientCode = clientCode;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}
	

	public boolean isConnected() {
		return isConnected;
	}


	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}


	public Client getClient() {
		return client;
	}


	public void setClient(Client client) {
		this.client = client;
	}


	/**
	 * 启动客户端的状态上报心跳
	 */
	public synchronized void notifyHeartBeat() {
		if (client == null || client.getResult() == null || client.getResult().getStatus() != 200) {
			try {
				client = ClientCenter.getAClient(this.serverIP, 9166, this.appkey, this.appSecret,clientStatusListener.getClass());
				logger.error("【客户端容器】client："+client.getResult().getStatus());
				if (client.getResult().getStatus() == 200) {
					isConnected = true;
					synchronized (lock) {
						lock.notify();
					}
				} else {
					logger.error("【客户端容器】服务器连接失败");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			synchronized (lock) {
				lock.notify();
			}
		}

	}

	/**
	 * 守护线程定时上报客户端信息
	 * <P></P>
	 */
	private void startThread() {
		
		//设置守护线程，定时打出系统信息
		Thread daemon =new Thread(new Runnable(){
		    @Override
		    public void run() {
		        // TODO Auto-generated method stub
		    		for(;;){
		    			logger.debug("-----------------【守护线程-BEGIN】-----------------");
//						logger.info("【客户端容器】业务心跳开始");
						try {
							Thread.sleep(15000L);
							synchronized (lock) {
								if (!isConnected) {
									logger.info("【守护线程】业务心跳线程阻塞等待中...");
									lock.wait();
									logger.info("【守护线程】业务心跳线程唤醒");
								}
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						clientRealData.setClientIp(getIpAddress());
						clientRealData.setWeight(weight==null?"80":weight);
//						clientRealData.setClientDescription("获取法院涉案信息");//这里可以设置实时动态信息
						try {
							String body = JsonUtils.toString(clientRealData);
							// 构建消息
							MiddleMsg msg = new MiddleMsg("getClientSystemInfo", body);
							if (isConnected) {// 表示连接成功
								// 发送消息
								ResultObject res = client.sendMsg(msg, new ReportClientInfoFeedbackHandler());
								if (res.getStatus() != 200) {// 表示客户端发送的时候发送失败
									logger.info("【守护线程】上报状态失败");
								}
							} else {
								logger.info("【守护线程】连接失败:" + client.getResult().getStatusMsg());
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
		    			
		    			logger.debug("-----------------【守护线程-END】-----------------");
		    		}
		    }
		});
		daemon.setDaemon(true);
		daemon.start();
	}
		



	/**
	 * 注册一个接收服务器推送信息处理的类
	 * 处理后的消息会自动发回给服务器
	 * <P></P>
	 */
	public void registSubTaskDealClass() {
		try {
			PushReceiverCenter.registReceiver("assignTaskToClient", new PushReceiverFeedbackHandler(apiInvorkerInterface));
			//回头补充接收推送后的直接回复消息。
//			PushReceiverCenter.registReceiver(actionName, receiver)	
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 获取本机的Ip地址
	 * <P></P>
	 * @return
	 */
	private String getIpAddress() {
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
	 * 演示第三方应用的调用类
	 * <P></P>
	 * @author liujun
	 * @date 2018年1月22日 下午2:28:20
	 */
	public class DemoApiService implements ApiInvorkerInterface {

		@Override
		public CommonReturnData dealSubTaskByApi(JSONObject taskData) throws SurveyException {
			// TODO Auto-generated method stub
			logger.info("【第三方调用】请求参数:" + taskData.toString());
			CommonReturnData commonReturnData=new CommonReturnData();
			commonReturnData.setCode("success");
			commonReturnData.setMsg("第三方执行成功");
			commonReturnData.setData(taskData);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new SurveyException("第三方执行失败",e);
//				commonReturnData.setCode("error");
//				commonReturnData.setMsg("第三方执行失败");
//				return commonReturnData;
			}
			logger.info("【第三方调用】返回结果:" + JSONObject.fromObject(commonReturnData).toString());
			return commonReturnData;
		}
		
	}
	
	/**
	 * 测试客户端
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ClientContainer.init("192.168.117.35","Client001", "5732f9a0bb7348258cfe53fa0160162d","eeeeewerw34rwer", "80", "测试客户端", null);
		System.out.println("-------------------------------------------------");

	}

}
