package com.sanyinggroup.corp.survey.server.model;

import java.util.Date;

import com.sanyinggroup.corp.survey.server.tools.Utils;

import net.sf.json.JSONObject;
/**
 * 子任务对象
 * @author liujun
 *
 */
public class SubTaskInfo {

	private static final long serialVersionUID = 1L;
	private String subTaskId;
	private TaskInfo taskInfo;
	private String appCode;// 是针对哪个类型生成的任务，如学籍学历，犯罪记录等
	private String appKey;// 是针对哪个类型生成的任务，如学籍学历，犯罪记录等.新版本，按肖老师意见，用这个。但这个key太长也没足够的意义。
	private JSONObject paraObj;// 请求参数
	private String status;// 任务是否完成 success:failure
	private String asignStatus;// 判断任务推送结果 success:failure
	private String name;
	private String idCard;
	private String description;
	private String createPerson;
	private Date createTime;
	private String updatePerson;
	private Date updateTime;
	private SurveyClient surveyClient;// 执行任务的客户端
	private String clientCode;//客户端code，因为上面的执行客户端surveyClient只有sessionID，会被移除，这里保留记录客户端code
	private Integer exeErrorCount;//失败次数
	
	private String subTaskType;
	
	private String hasData;
	private Integer dataCount;

	public SubTaskInfo(TaskInfo taskInfo, JSONObject paras, String appCode) {
		this.subTaskId = Utils.getShortUUID();
		this.taskInfo = taskInfo;
		this.paraObj = paras;
		this.appCode = appCode;
	}

	public SubTaskInfo() {
//		this.subTaskId = Utils.getShortUUID();
		this.exeErrorCount=0;
	}

	@Override
	public boolean equals(Object obj) {
		// 判断是否是自身
		if (obj == this) {
			return true;
		}
		// 判断是否属于SurveyClient类型
		if (!(obj instanceof SubTaskInfo)) {
			return false;
		}
		SubTaskInfo subTaskInfo = (SubTaskInfo) obj;

		return subTaskInfo.subTaskId.equals(this.subTaskId);//
	}

	public String getSubTaskId() {
		return subTaskId;
	}

	public void setSubTaskId(String subTaskId) {
		this.subTaskId = subTaskId;
	}

	public TaskInfo getTaskInfo() {
		return taskInfo;
	}

	public void setTaskInfo(TaskInfo taskInfo) {
		this.taskInfo = taskInfo;
	}

	public String getAppCode() {
		return appCode;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}

	public JSONObject getParaObj() {
		return paraObj;
	}

	public void setParaObj(JSONObject paraObj) {
		this.paraObj = paraObj;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	

	public String getAsignStatus() {
		return asignStatus;
	}

	public void setAsignStatus(String asignStatus) {
		this.asignStatus = asignStatus;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCreatePerson() {
		return createPerson;
	}

	public void setCreatePerson(String createPerson) {
		this.createPerson = createPerson;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUpdatePerson() {
		return updatePerson;
	}

	public void setUpdatePerson(String updatePerson) {
		this.updatePerson = updatePerson;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public SurveyClient getSurveyClient() {
		return surveyClient;
	}

	public void setSurveyClient(SurveyClient surveyClient) {
		this.surveyClient = surveyClient;
	}
	
	
	
	public String getSubTaskType() {
		return subTaskType;
	}

	public void setSubTaskType(String subTaskType) {
		this.subTaskType = subTaskType;
	}

	public String getClientCode() {
		return clientCode;
	}

	public void setClientCode(String clientCode) {
		this.clientCode = clientCode;
	}

	public Integer getExeErrorCount() {
		return exeErrorCount;
	}

	public void setExeErrorCount(Integer exeErrorCount) {
		this.exeErrorCount = exeErrorCount;
	}
	
	

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	
	

	public String getHasData() {
		return hasData;
	}

	public void setHasData(String hasData) {
		this.hasData = hasData;
	}

	

	public Integer getDataCount() {
		return dataCount;
	}

	public void setDataCount(Integer dataCount) {
		this.dataCount = dataCount;
	}

	@Override
	public String toString(){
		return this.getTaskInfo().getTaskId()+"|"+this.getSubTaskId()+"|"+this.getAppCode()+"|"+this.status+"|"+this.clientCode+"|"+this.exeErrorCount;
	}

}
