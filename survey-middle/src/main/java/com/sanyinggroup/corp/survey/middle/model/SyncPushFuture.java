package com.sanyinggroup.corp.survey.middle.model;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.sf.json.JSONObject;

/**
 * <p>Package:com.sanyinggroup.corp.urocissa.client.api.future</p> 
 * <p>Title:SyncSendFuture</p> 
 * <p>Description: </p> 
 * @author lixiao
 * @date 2017年8月1日 下午4:11:02
 * @version
 */
public class SyncPushFuture implements PushFuture<CommonReturnData>{
	
	private CountDownLatch latch = new CountDownLatch(1);
    private final long begin = System.currentTimeMillis();
    private long timeout;
    private CommonReturnData response;
    private final String subTaskId;
    private boolean writeResult;
    private Throwable cause;
    private boolean isTimeout = false;
    
    public SyncPushFuture(String subTaskId) {
		super();
		this.subTaskId = subTaskId;
		timeout  = 60*1000L; // 默认5秒
	}
	
	public SyncPushFuture( long timeout, String subTaskId) {
		super();
		this.timeout = timeout;
		this.subTaskId = subTaskId;
		this.writeResult = true;
        this.isTimeout = false;
	}
    
	/**
	 * @return the subTaskId
	 */
	public String getRequestId() {
		return subTaskId;
	}
	/**
	 * @return the cause
	 */
	public Throwable getCause() {
		return cause;
	}
	/**
	 * @param isTimeout the isTimeout to set
	 */
	public void setTimeout(boolean isTimeout) {
		this.isTimeout = isTimeout;
	}
	
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	public CommonReturnData get() throws InterruptedException, ExecutionException {
		latch.await();
		return  response;
	}

	@Override
	public CommonReturnData get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		if (latch.await(timeout, unit)) {
            return response;
        }
		return null;
	}

	@Override
	public Throwable cause() {
		return cause;
	}

	@Override
	public void setCause(Throwable cause) {
		this.cause = cause;
	}

	@Override
	public boolean isWriteSuccess() {
		return writeResult;
	}

	@Override
	public void setWriteResult(boolean result) {
		this.writeResult = result;
	}

	@Override
	public String subTaskId() {
		return subTaskId;
	}

	@Override
	public CommonReturnData getResponse() {
		try {
			latch.await(timeout,TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return response;
	}

	@Override
	public void setResponse(CommonReturnData response) {
		this.response = response;
		latch.countDown();
	}

	@Override
	public boolean isTimeout() {
		 if (isTimeout) {
	            return isTimeout;
	        }
	     return System.currentTimeMillis() - begin > timeout;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SyncSendFuture [begin=" + begin
				+ ", timeout=" + timeout + ", response=" + response
				+ ", subTaskId=" + subTaskId + ", writeResult=" + writeResult
				+ ", cause=" + cause + ", isTimeout=" + isTimeout + "]";
	}
	
}
