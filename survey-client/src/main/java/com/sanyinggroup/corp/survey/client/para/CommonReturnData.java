package com.sanyinggroup.corp.survey.client.para;



/**
 *
 * 背调中间层通用返回值对象
 * 
 */
public class CommonReturnData implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private String code;
	private String msg;
	private Object data;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}

	

}