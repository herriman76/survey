package com.sanyinggroup.corp.survey.server.handler;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.sanyinggroup.corp.survey.server.container.ContainerInit;
import com.sanyinggroup.corp.survey.server.container.TaskContainer;
import com.sanyinggroup.corp.survey.server.para.SurveyResponse;
import com.sanyinggroup.corp.survey.server.tools.JsonUtils;
import com.sanyinggroup.corp.urocissa.server.api.handler.ServerPushCallback;
import com.sanyinggroup.corp.urocissa.server.api.model.MiddleMsg;

/**
 * 任务消息处理器-推送方式
 * <P>以推送直接回调的方式处理任务</P>
 * @author liujun
 * @date 2018年1月23日 上午10:18:37
 */
public class ServerPushTaskCallback implements ServerPushCallback {

	private static final Logger logger = Logger.getLogger(RecvClientTaskHandler.class);
	
	@Override
	public void callback(MiddleMsg msg) {
		// TODO Auto-generated method stub
		String body = msg.getBody() + "";
		String code = "";
		logger.debug("【推送回调得到Client子任务返回结果】API返回子任务结果:" + body);
		SurveyResponse td = new SurveyResponse();
		try {
			JSONObject tdobject = JsonUtils.toJSONObject(body);
			// String identification = tdobject.getString("identification");
			// String subTaskId = tdobject.getString("subTaskId");
			// String returnCode = tdobject.getString("returnCode");
			// String returnMessage = tdobject.getString("returnMessage");//
			// 子任务失败原因
			ContainerInit.getInstance().taskContainer.subTaskListener.onSubTaskFinished(tdobject);
			code = "success";
		} catch (Exception e) {
			e.printStackTrace();
			code = "failure";
			logger.error("【推送回调】背调中心回调处理任务完成消息失败！异常：" + e.toString());
		}
	}

	
}
