package com.sanyinggroup.corp.survey.client;

import java.util.List;

import net.sf.json.JSONObject;

import com.sanyinggroup.corp.survey.client.para.CommonReturnData;
import com.sanyinggroup.corp.survey.client.para.SubTaskData;


/**
 * 调用第三方功能的接口,请各个第三方接口应用实现些接口
 * <P></P>
 * @author liujun
 * @date 2018年1月12日 下午5:10:02
 */
public interface ApiInvorkerInterface {

	
	/**
	 * 根据请求参数与所选择的一组app产生任务并处理
	 * <P></P>
	 * @param paras
	 * @param appCode
	 */
	public CommonReturnData dealSubTaskByApi(JSONObject taskData) throws SurveyException;
}
