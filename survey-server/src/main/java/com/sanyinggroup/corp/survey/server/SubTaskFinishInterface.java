package com.sanyinggroup.corp.survey.server;

import java.util.List;

import net.sf.json.JSONObject;

import com.sanyinggroup.corp.survey.server.model.SurveyClient;

/**
 * 子任务完成接口类
 * @author liujun
 *
 */
public interface SubTaskFinishInterface {
	
	public void onSubTaskFinished(JSONObject finishJason);

}
