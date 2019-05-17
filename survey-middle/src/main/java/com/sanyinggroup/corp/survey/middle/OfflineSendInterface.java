package com.sanyinggroup.corp.survey.middle;

import com.sanyinggroup.corp.survey.middle.model.CommonReturnData;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public interface OfflineSendInterface {

	
	/**
	 * 根据请求的任务数据，调用外部的接口，实现把任务分配给C端，如果正常不用返回数据，异常抛出即可
	 * CommonReturnData 是以后异常不够用时，再用。
	 * <P></P>
	 * @param paras
	 * @param appCode
	 */
	public CommonReturnData sendOfflineQuery(JSONObject taskData) throws SurveyException;
	
	
	public CommonReturnData saveOfflineQuery(JSONObject finishData) throws SurveyException;
	
	public JSONArray dealTimeoutDbData();
	
}
