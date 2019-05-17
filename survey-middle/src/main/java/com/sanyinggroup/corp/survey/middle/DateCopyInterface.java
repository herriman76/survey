package com.sanyinggroup.corp.survey.middle;

import com.sanyinggroup.corp.survey.middle.model.CommonReturnData;

import net.sf.json.JSONObject;

/**
 * 对合格的历史数据进行复制
 * @author liujun
 *
 */
public interface DateCopyInterface {

	
	/**
	 * 根据请求的任务数据，查询历史数据，如果有效就复制一份，不用再查询API了。
	 * <P></P>
	 * @param paras
	 * @param appCode
	 */
	public CommonReturnData copyHistoryData(JSONObject taskData,int validDays) throws SurveyException;
	
}
