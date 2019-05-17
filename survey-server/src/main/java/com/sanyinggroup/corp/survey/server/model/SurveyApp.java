package com.sanyinggroup.corp.survey.server.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sanyinggroup.corp.urocissa.server.api.info.ClientApp;

/*
 * 实现某一种调查业务的App对象
 */
public class SurveyApp {

	
    private static final long serialVersionUID = 1L;
   
    
    private String code;
    private String name;
    private String appKey;
    private String appScret;
    
    private List<SurveyClient> clientList;//appKey是key.因为底层没有code，只有appKey，所以只能这样对应。
    
    private String description;
    private String createPerson;
    private Date createTime;
    
    public SurveyApp(String code,String name,String appKey,String appScret,String description){
    	this.code=code;
    	this.name=name;
    	this.appKey=appKey;
    	this.appScret=appScret;
    	this.description=description;
    }
   
    public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
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




	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getAppScret() {
		return appScret;
	}

	public void setAppScret(String appScret) {
		this.appScret = appScret;
	}



	public List<SurveyClient> getClientList() {
		return clientList;
	}

	public void setClientList(List<SurveyClient> clientList) {
		this.clientList = clientList;
	}

	public SurveyApp() {
    }
	
	@Override
	public String toString(){
		return this.name+"|"+this.appKey+"|"+this.appScret+"|"+this.description+"|"+this.createPerson;
	}
}
