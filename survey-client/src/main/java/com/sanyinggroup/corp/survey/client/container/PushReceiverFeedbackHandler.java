package com.sanyinggroup.corp.survey.client.container;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.sanyinggroup.corp.survey.client.ApiInvorkerInterface;
import com.sanyinggroup.corp.survey.client.SurveyException;
import com.sanyinggroup.corp.survey.client.para.CommonReturnData;
import com.sanyinggroup.corp.survey.client.tools.JsonUtils;
import com.sanyinggroup.corp.urocissa.client.api.handler.PushReceiver;
import com.sanyinggroup.corp.urocissa.client.model.MiddleMsg;

/**
 * 处理服务器推送任务类
 * <P>客户端收到服务器推送的任务后，完成后，可以直接返回消息。 而消息的处理由这个类注册后进行发送，不用再另启一个异步消息发送了。</P>
 * 
 * @author liujun
 * @date 2018年1月22日 上午11:03:51
 */
public class PushReceiverFeedbackHandler implements PushReceiver {
	private static final Logger logger = Logger.getLogger(PushReceiverFeedbackHandler.class);

	ApiInvorkerInterface apiInvorkerInterface;

	public PushReceiverFeedbackHandler(ApiInvorkerInterface apiInvorkerInterface) {
		this.apiInvorkerInterface = apiInvorkerInterface;
	}

	/**
	 * 处理推送消息
	 */
	@Override
	public MiddleMsg handleReceivedMsg(MiddleMsg msg) {
		// TODO Auto-generated method stub

		String bodyString = msg.getBody() + "";
		logger.info("【客户端容器】处理服务器的推送子任务参数消息：" + msg);
		JSONObject taskData = JSONObject.fromObject(bodyString);
		try {
			//jason推送过来的数据，只是任务ID，子任务ID，和查询参数jason
			// SubTaskData
			// subTaskData=JsonUtils.toBean(bodyString,SubTaskData.class);
			CommonReturnData data = apiInvorkerInterface.dealSubTaskByApi(taskData);
			// return returnTaskMsg(data);//此版本直接返回，另不需要再发异步消息
			logger.info("【客户端容器】返回结果:" + JSONObject.fromObject(data).toString());
			msg.setBody(data);

			// return new MiddleMsg("subTaskFinishInfo",data);
			return msg;
		} catch (SurveyException e) {
			e.printStackTrace();
			CommonReturnData commonReturnData = new CommonReturnData();
			commonReturnData.setCode("failure");
			commonReturnData.setMsg(e.getMessage());
			commonReturnData.setData(taskData);
			msg.setBody(commonReturnData);
			return msg;
//			return new MiddleMsg("subTaskFinishInfo", commonReturnData);
		} catch (Exception e) {
			e.printStackTrace();
			CommonReturnData errorData = new CommonReturnData();
			errorData.setCode("failure");
			errorData.setMsg("子任务执行失败：e:" + e.getMessage());
			errorData.setData(taskData);
			msg.setBody(errorData);
			return msg;
//			return new MiddleMsg("subTaskFinishInfo", errorData);
		}
	}

}
