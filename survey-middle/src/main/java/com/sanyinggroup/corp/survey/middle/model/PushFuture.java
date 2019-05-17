package com.sanyinggroup.corp.survey.middle.model;

import java.util.concurrent.Future;
/**
 * 
 * <p>Package:com.sanyinggroup.corp.urocissa.client.api.future</p> 
 * <p>Title:SendFuture</p> 
 * <p>Description: 同步消息处理器 </p> 
 * @author lixiao
 * @date 2017年8月11日 下午3:53:35
 * @since 1.0.0
 * @param <T>
 */
public interface PushFuture<T> extends Future<T>{
	
	Throwable cause();

    void setCause(Throwable cause);

    boolean isWriteSuccess();

    void setWriteResult(boolean result);

    String subTaskId();

    T getResponse();

    void setResponse(T response);

    boolean isTimeout();
}
