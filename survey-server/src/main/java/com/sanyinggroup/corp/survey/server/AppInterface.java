package com.sanyinggroup.corp.survey.server;

import java.util.List;

import com.sanyinggroup.corp.survey.server.model.SurveyApp;

/**
 * <P>对外提供的获取App的接口</P>
 * @author liujun
 * @date 2018年1月12日 下午5:10:02
 */
@Deprecated
public interface AppInterface {

	
	/**
	 * 获取当前所有的App
	 * <P></P>
	 * @return
	 */
	public List<SurveyApp> appList();
}
