package com.sanyinggroup.corp.survey.client.para;

/**
 * 接收到子任务请求信息传输对象
 * @author liujun
 *
 */
public class SubTaskData {

    private static final long serialVersionUID = 1L;
    /**所属任务ID*/
	private String taskId; //任务ID
	/**子任务ID*/
	private String subTaskId; //子任务ID
	/**查询人的信息json*/
	private String userJsonStr;//查询人信息
	/**查询人的ID*/
	private String userId;//用户唯一标识符userId
	/**背调子任务查询条件jason*/
	private String queryJsonStr;//查询条件
	/**返回CODE*/
	private	String returnCode;
	/**返回信息*/
	private String returnMessage;
	
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
	
	
	
}