package com.sanyinggroup.corp.survey.middle.handle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sanyinggroup.corp.survey.middle.MiddleConfig;
import com.sanyinggroup.corp.survey.middle.container.AsServerContainer;
import com.sanyinggroup.corp.survey.middle.model.SurveyClient;
import com.sanyinggroup.corp.urocissa.server.api.info.ClientApp;
import com.sanyinggroup.corp.urocissa.server.event.AbstractEventHandler;
import com.sanyinggroup.corp.urocissa.server.event.EventInfo;

/**
 * 
 * <P>
 * 第三方应用客户端appClient在线状态监听器,监听底层通讯中间件的事件
 * </P>
 * 
 * @author liujun
 * @date 2018年1月22日 下午1:53:49
 */
public class ClientStatusListener extends AbstractEventHandler {

	private static final Logger logger = Logger.getLogger(ClientStatusListener.class);

	/**
	 * 登录成功 根据得到的通讯客户端，生成一个调查客户端，记录在app（应用类别）表中。
	 * 同时，给这个调查客户端，生成空的子任务表，用于记录分配给它的子任务。
	 */
	@Override
	public void loginSuccess(EventInfo res) {
		// TODO Auto-generated method stub
		super.loginSuccess(res);
		logger.info("【中间件状态监听】新上线客户端的AppKey：" + res.getAppinfo().getAppKey());
//		ContainerInit containerInit = ContainerInit.getInstance();
//		if (containerInit != null) {
			ClientApp clientApp = res.getAppinfo();
			Map<String, SurveyClient> surveyClientMap = AsServerContainer.onlineClientMap;
			// 1.新建一个调查客户端，以sessionId为key,记录在app表下面。
			// surveyClient中一部分来源上监听，另一部分信息要来源于业务心跳。
			SurveyClient surveyClient = new SurveyClient(clientApp.getIp(), "", clientApp.getSessionId(), clientApp.getChannelId(),MiddleConfig.getType());
			surveyClientMap.put(clientApp.getSessionId(), surveyClient);
			// 2.新建一个准备放置客户端下的子任务。
//			TaskContainer.clientSubTaskInfoMap.put(clientApp.getSessionId(), new ArrayList<SubTaskInfo>());// 新建此客户端下的子任务容器
			logger.info("【中间件状态监听】此APP当前客户端总数：" + surveyClientMap.size());
//		}
	}

	/**
	 * 断开连接事件
	 */
	@Override
	public void disconnected(EventInfo res) {
		// TODO Auto-generated method stub
		super.disconnected(res);
		logger.info("【中间件状态监听】新离线客户端AppKey：" + res.getAppinfo().getAppKey());
		logger.info("【中间件状态监听】新离线客户端SessionId：" + res.getAppinfo().getSessionId());
//		ContainerInit containerInit = ContainerInit.getInstance();
//		if (containerInit != null) {
			ClientApp clientApp = res.getAppinfo();// 得到通讯客户端对象
			if (!StringUtils.isBlank(clientApp.getAppKey()) ) {
				// 1.维护app下的在线客户端数据
				Map<String, SurveyClient> surveyClientMap = AsServerContainer.onlineClientMap;
				logger.info("【中间件状态监听】此APP离线客户端变更前数：" + surveyClientMap.size());
				logger.info("【中间件状态监听】此APP离线的sesssionId：" + clientApp.getSessionId());
				
				// 调试内容，因为有出现没移除，所以打印出信息
				logger.debug("【下线前客户端情况】--调试内容Begin---");
				Iterator<Entry<String, SurveyClient>> iter = surveyClientMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, SurveyClient> entry = (Map.Entry<String, SurveyClient>) iter.next();
					logger.debug("【监听】--循环Begin---");
					logger.debug("【监听】在线SurveyClient.getSessionId：" + entry.getValue().getSessionId());
					logger.debug("【监听】在线SurveyClient.getChannelId：" + entry.getValue().getChannelId());
					logger.debug("【监听】在线SurveyClient.getClientCode：" + entry.getValue().getClientCode());
					logger.debug("【监听】在线SurveyClient.getClientCode：" + entry.getValue().toString());
					logger.debug("【监听】--循--环End---");
				}
				logger.debug("【下线前客户端】--调试内容End---");
				
				if (clientApp.getSessionId() != null) {
					if (surveyClientMap != null) {
						surveyClientMap.remove(clientApp.getSessionId());// 根据通讯客户端，移除里面的调查客户端对象
					}
					// 2.维护在线客户端下的子任务数据
					// 通知容器维护动态客户端下的子任务map。
//					containerInit.taskContainer.updateSubTaskInfoByOffline(clientApp.getSessionId());
				}
				logger.info("【中间件状态监听1】此APP上线客户端变更后数：" + surveyClientMap.size());
			} else if(!StringUtils.isBlank(clientApp.getSessionId())){
				// 如果只有SessionId的话
				logger.info("【中间件状态监听】此APP下线事件只有SessionId：" + clientApp.getSessionId());
//				Iterator<Entry<String, SurveyClient>> iter = AsServerContainer.onlineClientMap.iterator();
//				while (iter.hasNext()) {
//					Map.Entry<String, ConcurrentHashMap<String, SurveyClient>> entry = (Map.Entry<String, ConcurrentHashMap<String, SurveyClient>>) iter.next();
//					if(entry.getValue()!=null) entry.getValue().remove(clientApp.getSessionId());
//				}
//				containerInit.taskContainer.updateSubTaskInfoByOffline(clientApp.getSessionId());
				AsServerContainer.onlineClientMap.remove(clientApp.getSessionId());
				logger.info("【中间件状态监听2】此APP下线客户端变更后数：" + AsServerContainer.onlineClientMap.size());
			}
//		}
	}

	/**
	 * 服务端关闭事件处理
	 */
	@Override
	public void serverClose() {
		// TODO Auto-generated method stub
		logger.info("【中间件状态监听】服务器关闭事件");
		AsServerContainer.onlineClientMap.clear();
		AsServerContainer.getInstance().isMiddlewearStarted = false;
	}

}
