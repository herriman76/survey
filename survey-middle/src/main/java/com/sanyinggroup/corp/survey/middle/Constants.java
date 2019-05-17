package com.sanyinggroup.corp.survey.middle;

/**
 * 背调中间层常量类
 * @author liujun
 *
 */
public class Constants {
	
	/**任务状态-001表示就绪*/
    public static final String TASK_INIT = "001"; //001表示就绪
    /**任务状态-002表示启动*/
    public static final String TASK_DOING = "002"; 
    /**任务状态-003表示正常完成*/
    public static final String TASK_DONE = "003"; 
    /**任务状态-004表示完成失败*/
    public static final String TASK_FAILURE = "004"; 
    /**任务状态-005表示超时失败*/
    public static final String TASK_TIMEOUT = "005"; 
    
    
    public static final int CLIENT_BASE_LEVER = 60; 
    
    //00强授权，01补充授权，02无感
    public static final String TASK_TYPE_ONLINE = "L";//02无感.线上
    public static final String TASK_TYPE_PERSON = "P";//01补充授权.人工
    public static final String TASK_TYPE_CRAWLER = "C";//00强授权.爬虫
    
}
