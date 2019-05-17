package com.sanyinggroup.corp.survey.server;

import java.util.List;

import com.sanyinggroup.corp.survey.server.model.SurveyClient;

/**
 * <P>对外提供的客户端信息接口</P>
 * @author liujun
 * @date 2018年1月12日 下午5:08:54
 */
@Deprecated
public interface ClientInterface {

	/**
	 * 获取所有的客户端
	 * <P></P>
	 * @return
	 */
	public List<SurveyClient> clientList();
	
	
	
}
