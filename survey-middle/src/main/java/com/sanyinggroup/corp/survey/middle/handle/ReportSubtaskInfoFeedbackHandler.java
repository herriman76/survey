package com.sanyinggroup.corp.survey.middle.handle;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.sanyinggroup.corp.survey.middle.tools.JsonUtils;
import com.sanyinggroup.corp.urocissa.client.api.handler.MsgHandler;
import com.sanyinggroup.corp.urocissa.client.model.MiddleMsg;

/**
 * 上报任务完成状态后，服务器返回消息的处理
 * <P></P>
 * @author liujun
 * @date 2018年1月22日 下午2:39:25
 */
public class ReportSubtaskInfoFeedbackHandler implements MsgHandler {

	private static final Logger logger = Logger.getLogger(ReportSubtaskInfoFeedbackHandler.class);

	/**
	 * 收到服务器反馈后，仅做展示，表明上报的任务服务端收到了
	 */
	@Override
	public void callback(MiddleMsg msg) {
		// TODO Auto-generated method stub
		 // 回调处理
		logger.info("【上报完成任务后回调】收到服务器信息："+msg);
		if (msg.getHeader().getStatus() == 200) {// 表示返回成功
			try {
				String body = msg.getBody() + "";
				if (body != null && !"null".equals(body)) {
					JSONObject tdobject = JsonUtils.toJSONObject(body);
					String reCode = tdobject.getString("code");
					if ("success".equals(reCode)) {
						logger.info("【上报完成任务后回调】成功传递子任务完成信息");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			logger.error("【上报完成任务后回调】服务器返回接收情况失败");
		}
	}

}
