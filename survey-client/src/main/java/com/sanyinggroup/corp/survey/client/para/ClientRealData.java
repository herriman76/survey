package com.sanyinggroup.corp.survey.client.para;

/**
 * 客户端状态实时信息传输对象
 * @author liujun
 *
 */
public class ClientRealData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String clientCode;
	private String appkey;
	private String appSecret;
	private String siteIp;
	private Integer requestCount;
	private Integer requestSuccessCount;
	private String siteNumber;
	private String description;
	private String status;
	private String postTime;// 传递时间
	private String appCode;// 用来区分是公安不良信息PSUI、CLIG法院诉讼、学历学籍ACRD 、贷款信息 LINF、职业资格VCQN
	// 机器信息
	private String freeSpace;
	private String totalSpace;
	private String usableSpace;
	private String systemMemory;
	private String maxMemory;
	private String freeMemory;
	private String weight;

	// 介绍信息
	private String clientIp;
	private String clientDescription;

	public ClientRealData(String clientCode,String appkey,String weight){
		this.clientCode=clientCode;
		this.appkey=appkey;
		this.weight=weight;
	}
	
	public ClientRealData(){

	}
	
	
	public String getClientCode() {
		return clientCode;
	}

	public void setClientCode(String clientCode) {
		this.clientCode = clientCode;
	}

	public String getAppkey() {
		return appkey;
	}

	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}

	public String getSiteIp() {
		return siteIp;
	}

	public void setSiteIp(String siteIp) {
		this.siteIp = siteIp;
	}

	public Integer getRequestCount() {
		return requestCount;
	}

	public void setRequestCount(Integer requestCount) {
		this.requestCount = requestCount;
	}

	public Integer getRequestSuccessCount() {
		return requestSuccessCount;
	}

	public void setRequestSuccessCount(Integer requestSuccessCount) {
		this.requestSuccessCount = requestSuccessCount;
	}

	public String getSiteNumber() {
		return siteNumber;
	}

	public void setSiteNumber(String siteNumber) {
		this.siteNumber = siteNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(String freeSpace) {
		this.freeSpace = freeSpace;
	}

	public String getTotalSpace() {
		return totalSpace;
	}

	public void setTotalSpace(String totalSpace) {
		this.totalSpace = totalSpace;
	}

	public String getUsableSpace() {
		return usableSpace;
	}

	public void setUsableSpace(String usableSpace) {
		this.usableSpace = usableSpace;
	}

	public String getSystemMemory() {
		return systemMemory;
	}

	public void setSystemMemory(String systemMemory) {
		this.systemMemory = systemMemory;
	}

	public String getMaxMemory() {
		return maxMemory;
	}

	public void setMaxMemory(String maxMemory) {
		this.maxMemory = maxMemory;
	}

	public String getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(String freeMemory) {
		this.freeMemory = freeMemory;
	}

	

	public String getAppCode() {
		return appCode;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	public String getPostTime() {
		return postTime;
	}

	public void setPostTime(String postTime) {
		this.postTime = postTime;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getClientDescription() {
		return clientDescription;
	}

	public void setClientDescription(String clientDescription) {
		this.clientDescription = clientDescription;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}
	
	
}