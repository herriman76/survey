package com.sanyinggroup.corp.survey.server.model;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.sanyinggroup.corp.survey.server.container.TaskContainer;
import com.sanyinggroup.corp.survey.server.tools.Utils;

import net.sf.json.JSONObject;

/**
 * 任务对象
 * @author liujun
 *
 */
public class TaskInfo {
    /**
     * 
     */
	private static final Logger logger = Logger.getLogger(TaskInfo.class);
	
    private static final long serialVersionUID = 1L;
    private String taskId;
    //被调查人
    private String requestICard;
    private String requestName;
    private String requestMobile;
    private String requestPosition;
    //调查者
    private String userId;
    private String userName;
    private String userPhone;
    private String companyId;
    private String companyName;
    
    
    private Date requestTime;
    private String taskName;
    private Integer totalTask;//任务总数
    private Integer onlineTask;//线上任务总数   这些有结果了就持久化了。
    private Integer pushTask;//推送任务数(都应该被推过，不管是不是推成功了)
    private Integer executeTask;//执行任务数（执行的子任务，不管是成功还是失败，还是超时）
    private Integer completeTask;//完成任务数（真正成功执行的任务）
    
    private Integer receivedAsyncTask;//异步收到的任务数（异步子任务回复收到的）
    
    
    private Map<String,SubTaskInfo> subTaskMap=new ConcurrentHashMap<String,SubTaskInfo>();//任务包含的子任务

    private String status;//001表示就绪 002执行中  003执行完成 004执行失败 005超时失败
    
    
    public TaskInfo(String idCard,String name){
    	this.requestICard=idCard;
    	this.requestName=name;
    }
    
    public TaskInfo(){
//    	this.taskId=Utils.getShortUUID();
    	this.totalTask=0;
    	this.onlineTask=0;
    	this.pushTask=0;
    	this.completeTask=0;
    	this.executeTask=0;
    	this.receivedAsyncTask=0;
    }
    
	@Override
	public boolean equals(Object obj) {
		// 判断是否是自身
		if (obj == this) {
			return true;
		}
		// 判断是否属于SurveyClient类型
		if (!(obj instanceof TaskInfo)) {
			return false;
		}
		TaskInfo taskInfo = (TaskInfo) obj;

		return taskInfo.taskId.equals(this.taskId);//
	}
    
    //synchronized
    public static void modifyTaskInfoPush(TaskInfo taskInfo, boolean isOk){
//    	this.executeTask=this.executeTask.intValue()+1;
    	logger.debug("【主任务对象】分派更新状态");
    	if(isOk){
    		taskInfo.pushTask=taskInfo.pushTask.intValue()+1;
    	}
    }
    
    /**
     * 修改内存主任务状态
     * 如果执行成功，执行数与完成数都+1
     * 否则，只是执行数+1
     * <P></P>
     * @param taskInfo
     * @param isOk
     */
    public static void modifyTaskInfoFinish(TaskInfo taskInfo,boolean isOk){
//    	this.executeTask=this.executeTask.intValue()+1;
    	logger.debug("【主任务对象】完成更新状态");
    	if(isOk){
    		taskInfo.completeTask=taskInfo.completeTask.intValue()+1;
    	}
    	taskInfo.executeTask=taskInfo.executeTask.intValue()+1;
    }


	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getRequestICard() {
		return requestICard;
	}

	public void setRequestICard(String requestICard) {
		this.requestICard = requestICard;
	}


	public String getRequestName() {
		return requestName;
	}

	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}

	public Date getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Integer getTotalTask() {
		return totalTask;
	}

	public void setTotalTask(Integer totalTask) {
		this.totalTask = totalTask;
	}
	
	

	public Integer getOnlineTask() {
		return onlineTask;
	}

	public void setOnlineTask(Integer onlineTask) {
		this.onlineTask = onlineTask;
	}

	public Integer getPushTask() {
		return pushTask;
	}

	public void setPushTask(Integer pushTask) {
		this.pushTask = pushTask;
	}

	public Integer getCompleteTask() {
		return completeTask;
	}

	public void setCompleteTask(Integer completeTask) {
		this.completeTask = completeTask;
	}

	public Integer getExecuteTask() {
		return executeTask;
	}

	public void setExecuteTask(Integer executeTask) {
		this.executeTask = executeTask;
	}

	public Map<String, SubTaskInfo> getSubTaskMap() {
		return subTaskMap;
	}

	public void setSubTaskMap(Map<String, SubTaskInfo> subTaskMap) {
		this.subTaskMap = subTaskMap;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	

	public Integer getReceivedAsyncTask() {
		return receivedAsyncTask;
	}

	public void setReceivedAsyncTask(Integer receivedAsyncTask) {
		this.receivedAsyncTask = receivedAsyncTask;
	}

	public String getRequestMobile() {
		return requestMobile;
	}

	public void setRequestMobile(String requestMobile) {
		this.requestMobile = requestMobile;
	}

	public String getRequestPosition() {
		return requestPosition;
	}

	public void setRequestPosition(String requestPosition) {
		this.requestPosition = requestPosition;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	
	

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	@Override
	public String toString(){
		return this.taskId+"|"+this.requestName+"|"+this.requestICard+"||"+this.getSubTaskMap().size()+"|"+this.getTotalTask()+"|"+this.getExecuteTask()+"|"+this.getCompleteTask();
	}
    
    
}
