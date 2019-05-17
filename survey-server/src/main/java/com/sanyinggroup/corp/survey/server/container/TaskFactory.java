package com.sanyinggroup.corp.survey.server.container;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.sanyinggroup.corp.survey.server.Constants;
import com.sanyinggroup.corp.survey.server.SurveyException;
import com.sanyinggroup.corp.survey.server.model.SubTaskInfo;
import com.sanyinggroup.corp.survey.server.model.SurveyApp;
import com.sanyinggroup.corp.survey.server.model.TaskInfo;

/**
 * <P>任务工厂，根据背调请求参数，生成主任务(包含子任务)对象</P>
 * 
 * @author liujun
 * @date 2018年1月22日 下午2:18:51
 */
public class TaskFactory {

	private static final Logger logger = Logger.getLogger(TaskFactory.class);

	/**
	 * 根据请求参数与appCode，生成主任务（包含子任务)
	 * <P>只是内存中生成，未来可引入外部接口类，实现持久化或者其它什么化</P>
	 * 
	 * @param paras
	 * @param appCode
	 * @return
	 */
	public static TaskInfo creatTaskInfo(JSONObject jsonParam) throws SurveyException {
		// 生成主任务
		try {
			// JSONObject jsonParam=JSONObject.fromObject(paras);
			TaskInfo taskInfo = new TaskInfo();
			taskInfo.setTaskId(jsonParam.getString("taskId"));
			taskInfo.setTaskName("调查"+jsonParam.getString("investigateId"));
			//被调查人
			taskInfo.setRequestName(jsonParam.getString("personName"));
			taskInfo.setRequestICard(jsonParam.getString("personIdCard"));
			taskInfo.setRequestMobile(jsonParam.getString("personMobile"));
			//调查者
			taskInfo.setUserId(jsonParam.getString("userId"));
			taskInfo.setUserName(jsonParam.getString("userName"));
			taskInfo.setUserPhone(jsonParam.getString("userPhone"));
			taskInfo.setCompanyId(jsonParam.getString("companyId"));
			taskInfo.setCompanyName(jsonParam.getString("companyName"));
			taskInfo.setRequestTime(new Date());//设置生成时间，超时用。
			Map<String, SubTaskInfo> subTaskMap = new HashMap<String, SubTaskInfo>();
			
			int onlineSubTaskNum=0;
			//循环任务类别，只要---->总类别中有，才产生子任务。原来用的是code，新版本用appkey.兼容
			boolean isAppkeyUsed=jsonParam.containsKey("isAppkeyUsed");
			logger.debug("[构建子任务true为新版]isAppkeyUsed:"+isAppkeyUsed);
			Iterator<Entry<String, SurveyApp>> iter = isAppkeyUsed?AppContainer.appHolder.entrySet().iterator():AppContainer.appCodeHolder.entrySet().iterator();
//			Iterator<Entry<String, SurveyApp>> iter = AppContainer.appCodeHolder.entrySet().iterator();
//			Iterator<Entry<String, SurveyApp>> iter = AppContainer.appHolder.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, SurveyApp> entry = (Map.Entry<String, SurveyApp>) iter.next();
				//二级jason中的数据.抽取一部分出来产生子任务对象，本身作为整体也是子任务对象的请求属性。
				logger.debug("[构建子任务]key:"+entry.getKey());
				JSONObject jsonEach = jsonParam.getJSONObject(entry.getKey());
				logger.debug("[构建子任务]jsonEach:"+jsonEach);
				if (jsonEach != null && !"null".equals(jsonEach.toString()) && !"".equals(jsonEach.toString())) {
					SubTaskInfo subTaskInfo = new SubTaskInfo();
					subTaskInfo.setSubTaskId(jsonEach.getString("subTaskId"));
					//不再一个个列出来条件了，可以不通用，可以不一样。
					String appKey=jsonEach.containsKey("subProductAppkey")?jsonEach.getString("subProductAppkey"):"";

					subTaskInfo.setAppCode(entry.getKey());
					subTaskInfo.setAppKey(appKey);
					subTaskInfo.setName(jsonEach.getString("subProductName"));
					//各个任务的参数可能不一样，后面当jason对象传给第三方执行。(兼容版本，做个判断)
//					subTaskInfo.setParaObj(jsonEach.containsKey("subProductRequestPara")?jsonEach.getJSONObject("subProductRequestPara"):jsonEach);
					//////------------设备统一的子任务查询条件--------------//////////////////
					subTaskInfo.setParaObj(jsonEach);
					
					subTaskInfo.setTaskInfo(taskInfo);
					subTaskInfo.setCreateTime(new Date());
					subTaskInfo.setSubTaskType(jsonEach.getString("subProductType"));
					//有可能是从库中重新加载上来的数据，库里来的，不走这里。
					//提交过来的，入库init，内存中不设置状态
//					if(jsonEach.containsKey("subTaskStatus")) subTaskInfo.setStatus(jsonEach.getString("subTaskStatus"));

					//统计线上的总数，线上都有结果了就进行持久化。
//					subTaskInfo.setSubTaskType(jsonEach.getString("subProductType"));
					if(Constants.TASK_TYPE_ONLINE.equals(jsonEach.getString("subProductType"))) onlineSubTaskNum++;
					subTaskMap.put(subTaskInfo.getSubTaskId(), subTaskInfo);
				}
			}
			taskInfo.setTotalTask(subTaskMap.size());//子任务总数
			taskInfo.setOnlineTask(onlineSubTaskNum);//线上子任务总数
			logger.debug("[构建子任务]子任务总数:"+subTaskMap.size());
			logger.debug("[构建总任务]新生成任务:"+taskInfo.toString());
			logger.info("【任务工厂】新生成任务：" + JSONObject.fromObject(taskInfo));
			taskInfo.setSubTaskMap(subTaskMap);
			return taskInfo;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new SurveyException("生成任务失败",e);
		}
	}

}
