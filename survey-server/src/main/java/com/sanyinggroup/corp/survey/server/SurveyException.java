package com.sanyinggroup.corp.survey.server;


/**
 * <P>自定义调查模块的通用异常</P>
 * @author liujun
 * @date 2018年1月24日 下午5:35:29
 */
public class SurveyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SurveyException() {
		super();
	}

	public SurveyException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SurveyException(String message) {
		super(message);
	}

}
