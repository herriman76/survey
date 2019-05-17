package com.sanyinggroup.corp.survey.server;

import java.util.List;

import net.sf.json.JSONObject;

import com.sanyinggroup.corp.survey.server.model.SurveyApp;
import com.sanyinggroup.corp.survey.server.model.TaskInfo;

/**
 * <P>对外提供的任务处理接口，主要是Web层应用使用</P>
 * @author liujun
 * @date 2018年1月12日 下午5:10:02
 */
public interface TaskDealInterface {

	
	/**
	 * 根据请求参数与所选择的一组app产生任务并处理
	 * <P></P>
	 * @param paras
	 * @param appCode
	 */
	public void putTask2Pool(JSONObject paras) throws SurveyException;
	
	/**
	 * 根据请求参数与所选择的一组app产生任务并处理
	 * <P></P>
	 * @param paras
	 * @param appCode
	 */
	public void putTask2Pool(TaskInfo taskInfo) throws SurveyException;
}
