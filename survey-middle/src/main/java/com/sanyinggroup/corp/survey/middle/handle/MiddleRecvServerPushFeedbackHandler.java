package com.sanyinggroup.corp.survey.middle.handle;

import java.util.Date;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.sanyinggroup.corp.survey.middle.Constants;
import com.sanyinggroup.corp.survey.middle.SurveyException;
import com.sanyinggroup.corp.survey.middle.container.AsClientContainer;
import com.sanyinggroup.corp.survey.middle.model.CommonReturnData;
import com.sanyinggroup.corp.survey.middle.model.PushFuture;
import com.sanyinggroup.corp.survey.middle.model.SyncPushFuture;
import com.sanyinggroup.corp.urocissa.client.ClientInit;
import com.sanyinggroup.corp.urocissa.client.api.future.SendFuture;
import com.sanyinggroup.corp.urocissa.client.api.future.SyncResponseMap;
import com.sanyinggroup.corp.urocissa.client.api.future.SyncSendFuture;
import com.sanyinggroup.corp.urocissa.client.api.handler.MsgHandler;
import com.sanyinggroup.corp.urocissa.client.api.handler.PushReceiver;
import com.sanyinggroup.corp.urocissa.client.model.MiddleMsg;

/**
 * 接收服务器任务后，进行处理
 * 1.任务存入
 * 2.产生同步对象
 * 3.获取值后返回给服务器
 * 
 * @author liujun
 * @date 2018年1月22日 上午11:03:51
 */
public class MiddleRecvServerPushFeedbackHandler implements PushReceiver {
	private static final Logger logger = Logger.getLogger(MiddleRecvServerPushFeedbackHandler.class);


	public MiddleRecvServerPushFeedbackHandler() {
	}

	/**
	 * 处理推送消息
	 */
	@Override
	public MiddleMsg handleReceivedMsg(MiddleMsg msg) {
		// TODO Auto-generated method stub

		String bodyString = msg.getBody() + "";
		logger.info("【客户端容器】处理服务器的推送子任务消息msg：" + msg);
		JSONObject taskData = JSONObject.fromObject(bodyString);
		try {
			String taskId=taskData.getString("taskId");
			String subTaskId=taskData.getString("subTaskId");
			String subTaskType=taskData.containsKey("subTaskType")?taskData.getString("subTaskType"):null;
			String queryStr=taskData.getString("queryJsonStr");
			logger.debug("【查询条件】queryStr:"+queryStr);
			System.out.println("【查询条件】queryStr:"+queryStr);
			
			//【是否异步请求及异步请求的服务器code(必须是配置项)，两个都要有。】
			//【！！！！！！！！！如果是同步的，就产生同步等待对象。如果是异步的，就完成回调中给服务发送结果。】
			String isAsync=taskData.containsKey("isAsync")? taskData.getString("isAsync"):null;
			String asyncServerCode=taskData.containsKey("asyncServerCode")? taskData.getString("asyncServerCode"):null;
			//上面两个不要了，用【任务类型】来区分是不是要异步，用【ip：port】来确定异步返回给谁。
			
			//【是否异步请求及异步请求的服务器code(必须是配置项)，两个都要有。否则不知道回调时告诉谁。如同叫我办事，没留电话号码，我办好无法通知】
			long timeoutSet=60*1000l;
//			int timeOutSeconds=taskData.getInt("timeOutSeconds");
			if(taskData.containsKey("timeOutSeconds")) timeoutSet=taskData.getInt("timeOutSeconds")*1000l;
			logger.info("【当前任务中的特殊配置为】isAsync|asyncServerCode|timeoutSet："+isAsync+"|"+asyncServerCode+"|"+timeoutSet);

//			String personIdCard=JSONObject.fromObject(queryStr).getString("personIdCard");
//			String personName=JSONObject.fromObject(queryStr).getString("personName");
//			String personMobile=JSONObject.fromObject(queryStr).getString("personMobile");
			
			taskData.put("testNum", 0);
			taskData.put("startDate", System.currentTimeMillis());
			//1.把请求记入内存（包括对列与监控MAP）
			AsClientContainer.subTaskInfoQueue.add(taskData);
			AsClientContainer.subTaskInfoMap.put(subTaskId, taskData);
			logger.info("【当前系统中已经接收的任务总数为】："+AsClientContainer.subTaskNum.incrementAndGet());
			
			//如果接收到的是异步任务，就直接返回结果，表示收到了。目前非线上就是这样。
			if("async".equals(isAsync)||!Constants.TASK_TYPE_ONLINE.equals(subTaskType)){
				CommonReturnData commonReturnData=new CommonReturnData();
				commonReturnData.setCode("received");//即通知服务端可能持久化数据了
				commonReturnData.setMsg("收到异步查询请求成功");
				commonReturnData.setData(taskData);
				logger.info("【异步或者是非线上任务】反馈中心任务已经code=received");
				msg.setBody(commonReturnData);
			}
			else{//如果收到的是线上无感的同步任务，就产生同步等待对象
				// 2.生成同步等待对象
				logger.info("【同步对象等待】timeoutSet(s):"+timeoutSet);
				PushFuture<CommonReturnData> responseFuture =new SyncPushFuture(timeoutSet,subTaskId);
				AsClientContainer.syncKey.put(subTaskId, responseFuture);
//				PushFuture<CommonReturnData> responseFuture = new SyncPushFuture(subTaskId);
				AsClientContainer.syncKey.put(subTaskId, responseFuture);
				//3.获取同步返回值
				CommonReturnData commonReturnData=responseFuture.getResponse();
				//正常情况下，监听客户端完成时，会写入结果。另外分配失败时会写入结果（没客户端或者推广失败）
				if(commonReturnData==null) {
					commonReturnData=new CommonReturnData();
					commonReturnData.setCode("failure");
					commonReturnData.setMsg("超时时间，还找不到同步对象中的值，返回失败");
					commonReturnData.setData(taskData);
					logger.warn("【从同步对象中取值】超时取不到值");
				}
				logger.info("【从同步对象中取值】RESUST:"+commonReturnData.getCode()+commonReturnData.getMsg());
				//4.移除同步对象
				logger.info("【客户端容器now】:"+AsClientContainer.syncKey.size());
				AsClientContainer.syncKey.remove(subTaskId);
				logger.info("【客户端容器-1】:"+AsClientContainer.syncKey.size());
				
				logger.info("【客户端容器】上报结果:" + JSONObject.fromObject(commonReturnData).toString());
				msg.setBody(commonReturnData);				
			}
			// return new MiddleMsg("subTaskFinishInfo",data);
			return msg;
		} catch (Exception e) {
			e.printStackTrace();
			CommonReturnData errorData = new CommonReturnData();
			errorData.setCode("failure");
			errorData.setMsg("子任务执行失败：e:" + e.getMessage());
			errorData.setData(taskData);
			msg.setBody(errorData);
			return msg;
		}
	}

}
