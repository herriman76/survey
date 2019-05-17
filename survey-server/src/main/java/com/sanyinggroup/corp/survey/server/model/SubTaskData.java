package com.sanyinggroup.corp.survey.server.model;

/**
 * 子任务信息传输对象
 * @author liujun
 *
 */
public class SubTaskData {

    private static final long serialVersionUID = 1L;

	private String taskId; //任务ID
	private String subTaskId; //子任务ID
	//查询人
	private String userJsonStr;//查询人信息
	private String userId;//用户唯一标识符userId
	//被查人
	private String queryJsonStr;//查询条件
	
	private String subTaskType;
	 //返回
	private	String returnCode;
	private String returnMessage;
	
	//异步请求时，客户端要按这个找到对应的服务，发结果消息的。v2.0
	private String serverIp;
	private String serverPort;
	
	public SubTaskData(){

	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getSubTaskId() {
		return subTaskId;
	}

	public void setSubTaskId(String subTaskId) {
		this.subTaskId = subTaskId;
	}

	public String getUserJsonStr() {
		return userJsonStr;
	}

	public void setUserJsonStr(String userJsonStr) {
		this.userJsonStr = userJsonStr;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getQueryJsonStr() {
		return queryJsonStr;
	}

	public void setQueryJsonStr(String queryJsonStr) {
		this.queryJsonStr = queryJsonStr;
	}

	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public String getReturnMessage() {
		return returnMessage;
	}

	public void setReturnMessage(String returnMessage) {
		this.returnMessage = returnMessage;
	}

	public String getSubTaskType() {
		return subTaskType;
	}

	public void setSubTaskType(String subTaskType) {
		this.subTaskType = subTaskType;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}
	
	
	
}