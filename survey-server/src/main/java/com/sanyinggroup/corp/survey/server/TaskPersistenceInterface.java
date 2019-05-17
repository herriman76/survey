package com.sanyinggroup.corp.survey.server;

import java.util.List;

import net.sf.json.JSONObject;

import com.sanyinggroup.corp.survey.server.model.SubTaskInfo;
import com.sanyinggroup.corp.survey.server.model.SurveyApp;
import com.sanyinggroup.corp.survey.server.model.TaskInfo;

/**
 * <P>对外提供的任务处理接口，目前主要是Web层的应用使用，需要外部提供持久化的实现类</P>
 * @author liujun
 * @date 2018年1月12日 下午5:10:02
 */
public interface TaskPersistenceInterface {

	
	/**
	 * 任务变更入库接口，外部类实现具体操作
	 * <P></P>
	 * @param paras
	 * @param appCode
	 */
	public void modifyTask2DB(TaskInfo taskInfo) throws SurveyException;
	
	/**
	 * 只有子任务时，子任务的变更入库
	 * <P></P>
	 * @param subTaskInfo
	 * @throws SurveyException
	 */
	public void modifySubTask2DB(SubTaskInfo subTaskInfo) throws SurveyException;

	
	/**
	 * V2.0中线下任务完成时，由于内存中不可能长时间放线下的任务，所以这个结果直接入库
	 * @param finishJason
	 * @throws SurveyException
	 */
	void modifySubTask2DB(JSONObject finishJason) throws SurveyException;
}
