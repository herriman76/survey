package com.sanyinggroup.corp.survey.middle.handle;

import java.util.Date;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.sanyinggroup.corp.survey.middle.container.AsServerContainer;
import com.sanyinggroup.corp.survey.middle.model.ClientRealData;
import com.sanyinggroup.corp.survey.middle.model.SurveyClient;
import com.sanyinggroup.corp.survey.middle.model.SurveyResponse;
import com.sanyinggroup.corp.survey.middle.tools.JsonUtils;
import com.sanyinggroup.corp.urocissa.server.api.model.MiddleMsg;
import com.sanyinggroup.corp.urocissa.server.api.service.MsgEvent;
import com.sanyinggroup.corp.urocissa.server.api.service.MsgServiceHandler;


/**
 * 消息处理器：处理第三方应用上报的client信息
 * <P></P>
 * @author liujun
 * @date 2018年1月22日 下午2:04:28
 */
public class RecvClientInfoHandler implements MsgServiceHandler {
	private static final Logger logger =Logger.getLogger(RecvClientInfoHandler.class);
	
	/**
	 * 收到消息后，把消息中的调查客户信息，补充进调查客户端对象中。
	 * 调查客户端是在客户端上线事件中产生的，这里只补信息。
	 */
	@Override
	public MiddleMsg handleMsgEvent(MsgEvent dm, MiddleMsg msg) {
		// TODO Auto-generated method stub

		String body = msg.getBody() + "";
		String sessionId = msg.getHeader().getSessionID();
		String returnCode = "";
		logger.debug("【处理客户心跳】MsgBody：" + body);
		logger.debug("【处理客户心跳】sessionId：" + sessionId);
		// logger.debug("获取客户端注册信息:"+body);
		SurveyResponse td = new SurveyResponse();
		try {
			ClientRealData clientRealData = JsonUtils.toBean(body, ClientRealData.class);
			//将客户端的实时信息设置到在线客户端的属性中
//			Map<String, SurveyClient> onlineClientMap=(Map<String, SurveyClient>) AsServerContainer.onlineClientMap.get(clientRealData.getAppkey());
			SurveyClient surveyClient=AsServerContainer.onlineClientMap.get(sessionId);
			logger.debug("【处理客户心跳】-客户端【存在吗】？："+surveyClient!=null);
			if(surveyClient!=null){
//				logger.debug("【处理客户心跳】只有存在，才补充客户端信息sessionId|clientCode|appCode|appKey：" + surveyClient.getSessionId()+"|" + surveyClient.getClientCode() +"|" + surveyClient.getSurveyApp().getCode()+"|"+surveyClient.getSurveyApp().getAppKey());
				surveyClient.setClientCode(clientRealData.getClientCode());
				surveyClient.setUpdateTime(new Date());
				surveyClient.setWeight(clientRealData.getWeight() == null ? "60" : clientRealData.getWeight());
				logger.debug("【处理客户心跳】客户端新的信息："+surveyClient.toString());
			}
			returnCode = "success";
		} catch (Exception e) {
			e.printStackTrace();
			returnCode = "failure";
			logger.error("【处理客户心跳】消息失败！异常：" + e.toString());
		}
		td.setCode(returnCode);
		msg.setBody(td);
		logger.info("【处理客户心跳】返回给客户端数据：" + JSONObject.fromObject(msg).toString());
		return msg;

	}

}
