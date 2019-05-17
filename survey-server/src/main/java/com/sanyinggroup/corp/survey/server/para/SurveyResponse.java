package com.sanyinggroup.corp.survey.server.para;



/**
 *
 * 调查中间层通用响应结果对象
 * 
 */
public class SurveyResponse implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
	private String code;
	private String taskId;
	private String msg;
	private String sign;
	private String appkey;
	private String appSecret;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getAppkey() {
		return appkey;
	}
	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}
	public String getAppSecret() {
		return appSecret;
	}
	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}

	
	
	

}