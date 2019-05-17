package com.sanyinggroup.corp.survey.server.handler;

import java.util.Date;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.sanyinggroup.corp.survey.server.container.AppContainer;
import com.sanyinggroup.corp.survey.server.container.ContainerInit;
import com.sanyinggroup.corp.survey.server.container.TaskContainer;
import com.sanyinggroup.corp.survey.server.model.SurveyClient;
import com.sanyinggroup.corp.survey.server.para.ClientRealData;
import com.sanyinggroup.corp.survey.server.para.SurveyResponse;
import com.sanyinggroup.corp.survey.server.tools.JsonUtils;
import com.sanyinggroup.corp.urocissa.server.api.model.MiddleMsg;
import com.sanyinggroup.corp.urocissa.server.api.service.MsgEvent;
import com.sanyinggroup.corp.urocissa.server.api.service.MsgServiceHandler;

/**
 * <P>任务消息处理器-标准异步消息</P>
 * 推送后，以异步消息处理方式，接收客户反馈任务完成
 * 当第三方应用完成任务后，会发消息来上报完成情况。
 * 这里收到信息后，通知容器去处理。
 * @author liujun
 * @date 2018年1月22日 下午2:08:59
 */
public class RecvClientTaskHandler implements MsgServiceHandler {
	private static final Logger logger = Logger.getLogger(RecvClientTaskHandler.class);

	/**
	 * 从消息中取出完成情况，通知容器处理。
	 */
	@Override
	public MiddleMsg handleMsgEvent(MsgEvent dm, MiddleMsg msg) {
		// TODO Auto-generated method stub
		String body = msg.getBody() + "";
		String code = "";
		logger.debug("【得到Client子任务返回结果】API返回子任务结果:" + body);
		SurveyResponse td = new SurveyResponse();
		try {
			JSONObject tdObject = JsonUtils.toJSONObject(body);
			// 子任务失败原因
			ContainerInit.getInstance().taskContainer.subTaskListener.onSubTaskFinished(tdObject);
			code = "success";
		} catch (Exception e) {
			e.printStackTrace();
			code = "failure";
			logger.error("【得到Client子任务返回结果】背调中心消息处理任务完成消息失败！异常：" + e.toString());
		}
		td.setCode(code);
		msg.setBody(td);
		return msg;

	}

}
