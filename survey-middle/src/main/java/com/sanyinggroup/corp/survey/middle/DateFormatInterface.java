package com.sanyinggroup.corp.survey.middle;

import com.sanyinggroup.corp.survey.middle.model.CommonReturnData;

import net.sf.json.JSONObject;


/**
 * 对子应用产生的数据进行格式化
 * @author liujun
 *
 */
public interface DateFormatInterface {

	
	/**
	 * API接口返回查询数据后，通过这个接口类把厂商的JASON数据写入规范的数据库表中。
	 * 各类型API实现自己的类。
	 * <P></P>
	 * @param paras
	 * @param appCode
	 */
	public CommonReturnData formatJasonDbData(String taskId,String subTaskId) throws SurveyException;
	
}
