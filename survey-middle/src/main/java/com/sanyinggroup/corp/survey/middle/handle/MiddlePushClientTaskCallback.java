package com.sanyinggroup.corp.survey.middle.handle;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.sanyinggroup.corp.survey.middle.container.AsClientContainer;
import com.sanyinggroup.corp.survey.middle.model.CommonReturnData;
import com.sanyinggroup.corp.survey.middle.model.PushFuture;
import com.sanyinggroup.corp.survey.middle.model.SurveyResponse;
import com.sanyinggroup.corp.survey.middle.tools.JsonUtils;
import com.sanyinggroup.corp.urocissa.client.init.Client;
import com.sanyinggroup.corp.urocissa.client.init.ClientCenter;
import com.sanyinggroup.corp.urocissa.client.model.ResultObject;
import com.sanyinggroup.corp.urocissa.server.api.handler.ServerPushCallback;
import com.sanyinggroup.corp.urocissa.server.api.model.MiddleMsg;

/**
 * 任务消息处理器-推送方式
 * <P>以推送直接回调的方式处理任务</P>
 * @author liujun
 * @date 2018年1月23日 上午10:18:37
 */
public class MiddlePushClientTaskCallback implements ServerPushCallback {

	private static final Logger logger = Logger.getLogger(MiddlePushClientTaskCallback.class);
	
	@Override
	public void callback(MiddleMsg msg) {
		// TODO Auto-generated method stub
		String body = msg.getBody() + "";
		logger.debug("【推送回调得到Client子任务返回结果】API返回子任务结果:" + body);
		SurveyResponse td = new SurveyResponse();
		try {
			JSONObject finishJason = JsonUtils.toJSONObject(body);
			// String identification = tdobject.getString("identification");
			// String returnCode = tdobject.getString("returnCode");
			// String returnMessage = tdobject.getString("returnMessage");//
			String code = finishJason.getString("code");
			String msgInfo = finishJason.getString("msg");
			String subTaskId = finishJason.getJSONObject("data").getString("subTaskId");
			String taskId = finishJason.getJSONObject("data").getString("taskId");
//			String clientCode = finishJason.getString("clientCode");
			
			//从任务池中移除
			JSONObject taskData =AsClientContainer.subTaskInfoMap.remove(subTaskId);
//			String isAsync=taskData.getString("isAsync");
//			String asyncServerCode=taskData.getString("asyncServerCode");
			String isAsync=taskData.containsKey("isAsync")? taskData.getString("isAsync"):null;
			String asyncServerCode=taskData.containsKey("asyncServerCode")? taskData.getString("asyncServerCode"):null;

			//如果是异步任务，发消息给服务器，告诉完成情况
			//最后的实现上：非线上的任务，都走外部接口，目前通过DUBBO调用C端。线上的才用原中间件推，所以这里只会收到线上的。是需要设置同步等待对象的。
			if("async".equals(isAsync)){
				AsClientContainer.getInstance().sendTaskAsyncResult2Server(finishJason);
			}
			else{//如果是同步任务，结果写入同步等待对象中
				CommonReturnData response=new CommonReturnData();
				
				//写入前，要对最终的结果进行JASON到DB的结构化转换，如果转出错，就是原数据JASON不对。
				try {
					if(AsClientContainer.getInstance().dateFormatInterface!=null){
						AsClientContainer.getInstance().dateFormatInterface.formatJasonDbData(taskId,subTaskId);
					}
					else{
						logger.info("【未配置数据转换工具接口类】");
					}
					response.setCode(code);
					response.setMsg(msgInfo);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					response.setCode("failure");
					response.setMsg("接口数据JSAN标准化转换失败");
				}
				response.setData(finishJason.getJSONObject("data"));
				logger.info("【修改子任务的完成状态，并移除】子任务完成状态！code："+code);
				PushFuture<CommonReturnData> pushFuture=AsClientContainer.syncKey.get(subTaskId);
				
				//将从客户端返回的结果写入到同步对象中
				if(pushFuture!=null) pushFuture.setResponse(response);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("【推送回调】背调中心回调处理任务完成消息失败！异常：" + e.toString());
		}
	}

	
}
