package com.sanyinggroup.corp.survey.client.container;

import org.apache.log4j.Logger;

import com.sanyinggroup.corp.urocissa.client.event.AbstractEventHandler;
import com.sanyinggroup.corp.urocissa.client.event.ServerInfo;
import com.sanyinggroup.corp.urocissa.client.init.Client;
import com.sanyinggroup.corp.urocissa.client.model.MiddleMsg;
import com.sanyinggroup.corp.urocissa.client.model.ResultObject;

/**
 * 监听客户端通讯中间件的状态变化信息
 * <P></P>
 * @author liujun
 * @date 2018年1月22日 下午2:25:24
 */
public class ClientStatusListener extends AbstractEventHandler {
	
	private static final Logger logger = Logger.getLogger(ClientStatusListener.class);
	
	/**
	 * 登录成功后，设置连接正常，通知心跳
	 */
	@Override
	public void loginSuccess(ResultObject res) {
		// TODO Auto-generated method stub
		super.loginSuccess(res);
		logger.debug("【登录成功事件】"+res.getStatusMsg());
		ClientContainer clientContainer=ClientContainer.getInstance();
		clientContainer.setConnected(true);
		Client client=clientContainer.getClient();
		if(client!=null) clientContainer.notifyHeartBeat();
	}

	/**
	 * 登录失败，设置连接为false
	 */
	@Override
	public void loginError(ResultObject res) {
		// TODO Auto-generated method stub
		super.loginError(res);
		logger.debug("【登录失败事件】"+res.getStatusMsg());
		ClientContainer clientContainer=ClientContainer.getInstance();
		clientContainer.setConnected(false);
	}

	/**
	 * 断开连接，设置连接为false
	 */
	@Override
	public void disconnected(ServerInfo res) {
		// TODO Auto-generated method stub
		super.disconnected(res);
		logger.debug("【断开连接事件】"+res.getAppKey());
		ClientContainer clientContainer=ClientContainer.getInstance();
		clientContainer.setConnected(false);
	}

	/**
	 * 连接失败，设置连接为false
	 */
	@Override
	public void connectFail(ResultObject res) {
		// TODO Auto-generated method stub
		super.connectFail(res);
//		logger.debug("【连接失败事件】"+res.getStatusMsg());
		ClientContainer clientContainer=ClientContainer.getInstance();
		clientContainer.setConnected(false);
	}

	/**
	 * 发送消息失败事件
	 */
	@Override
	public void msgSendFail(MiddleMsg msg, ResultObject res) {
		// TODO Auto-generated method stub
		super.msgSendFail(msg, res);
//		ClientContainer clientContainer=ClientContainer.getInstance();
//		clientContainer.notifyHeartBeat();
	}

}
