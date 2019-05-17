package com.sanyinggroup.corp.survey.middle.model;

import java.util.Date;

/*
 * 第三方接口应用客户端对象
 */
public class SurveyClient {
	
    private static final long serialVersionUID = 1L;
    //基础属性
    private String clientCode;
//    private String appKey;//属于什么App,以appKey为外键
    private String type;
//    private String appScret;
    
    //业务心跳得到的属性
    private String name;
    private String weight;
    private String provider;
    
    //通讯组件得到的属性
    private String clientIp;
    private String clientPort;
    private String sessionId;
    private String channelId;
    //其它属性
    private int taskCount;
    private String status;
    private String description;
    private String createPerson;
    private Date createTime;
    private String updatePerson;
    private Date updateTime;
    
    public SurveyClient() {
    }
    
    public SurveyClient(String clientCode,String name,String weight,String type) {
    	this.clientCode=clientCode;
    	this.name=name;
    	this.weight=weight;
    	this.type=type;
    }
    
    //
    public SurveyClient(String clientIp,String clientPort,String sessionId,String channelId,String type) {
    	this.clientIp=clientIp;
    	this.clientPort=clientPort;
    	this.sessionId=sessionId;
    	this.channelId=channelId;
    	this.type=type;
    }
    
	@Override
	public boolean equals(Object obj) {
		//判断是否是自身
		if(obj == this){
			return true;
		}
	    //判断是否属于SurveyClient类型
		if(!(obj instanceof SurveyClient)){
			return false;
		}
		SurveyClient surveyClient = (SurveyClient)obj;
		
		return surveyClient.sessionId.equals(this.sessionId);//在管理在线客户端时，相同的sessionId认为是同一个。
	}
	

	public String getClientCode() {
		return clientCode;
	}

	public void setClientCode(String clientCode) {
		this.clientCode = clientCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public int getTaskCount() {
		return taskCount;
	}

	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	

	public String getSurveyApp() {
		return type;
	}

	public void setSurveyApp(String type) {
		this.type = type;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getClientPort() {
		return clientPort;
	}

	public void setClientPort(String clientPort) {
		this.clientPort = clientPort;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	@Override
	public String toString(){
		return "【channelId，sessionId，clientIp，name,clientCode,weight】"+this.channelId+"|"+this.sessionId+"|"+this.clientIp+"|"+this.name+"|"+this.clientCode+"|"+this.weight;
	}
	
	
}