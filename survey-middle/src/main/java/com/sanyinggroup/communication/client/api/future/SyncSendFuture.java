// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SyncSendFuture.java

package com.sanyinggroup.communication.client.api.future;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.sanyinggroup.corp.urocissa.client.api.future.SendFuture;
import com.sanyinggroup.corp.urocissa.client.model.MiddleMsg;

// Referenced classes of package com.sanyinggroup.communication.client.api.future:

public class SyncSendFuture implements SendFuture
{

	public static final Logger LOGGER = Logger.getLogger(SyncSendFuture.class);
	
    public SyncSendFuture(String requestId)
    {
        latch = new CountDownLatch(1);
        begin = System.currentTimeMillis();
        isTimeout = false;
        this.requestId = requestId;
        timeout = 30000L;
    }

    public SyncSendFuture(long timeout, String requestId)
    {
        latch = new CountDownLatch(1);
        begin = System.currentTimeMillis();
        isTimeout = false;
        this.timeout = timeout;
        this.requestId = requestId;
        writeResult = true;
        isTimeout = false;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public Throwable getCause()
    {
        return cause;
    }

    public void setTimeout(boolean isTimeout)
    {
        this.isTimeout = isTimeout;
    }

    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return false;
    }

    public boolean isCancelled()
    {
        return false;
    }

    public boolean isDone()
    {
        return false;
    }

    public MiddleMsg get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException
    {
    	LOGGER.info("【获取同步结果等待一定时间中...this】"+this.toString());
    	LOGGER.info("【获取同步结果等待一定时间中...unit:】"+unit.toString());
    	if(latch.await(timeout, unit))
            return response;
        else
            return null;
    }

    public Throwable cause()
    {
        return cause;
    }

    public void setCause(Throwable cause)
    {
        this.cause = cause;
    }

    public boolean isWriteSuccess()
    {
        return writeResult;
    }

    public void setWriteResult(boolean result)
    {
        writeResult = result;
    }

    public String requestId()
    {
        return requestId;
    }

//    public MiddleMsg getResponse()
//    {
//        try
//        {
//            latch.await(timeout, TimeUnit.MILLISECONDS);
//        }
//        catch(InterruptedException e)
//        {
//            e.printStackTrace();
//        }
//        return response;
//    }

    
    public void setResponse(MiddleMsg response)
    {
        
    	LOGGER.info("【回调handle中设置结果，并唤醒get线程】"+( (response==null || response.getBody()==null)?"null msg":response.getBody().toString()));
    	LOGGER.info("【回调handle中设置结果,同步调用等待时间为：】"+(System.currentTimeMillis() - begin));
    	this.response = response;
        latch.countDown();
    }

    public boolean isTimeout()
    {
    	LOGGER.info("【回调handle中设置结果】isTimeout()中同步等待时间为："+(System.currentTimeMillis() - begin));
        if(isTimeout)
            return isTimeout;
        return System.currentTimeMillis() - begin > timeout;
    }

    public String toString()
    {
        return (new StringBuilder("SyncSendFuture [begin=")).append(begin).append(", timeout=").append(timeout).append(", response=").append(response).append(", requestId=").append(requestId).append(", writeResult=").append(writeResult).append(", cause=").append(cause).append(", isTimeout=").append(isTimeout).append("]").toString();
    }

    private CountDownLatch latch;
    private final long begin;
    private long timeout;
    private MiddleMsg response;
    private final String requestId;
    private boolean writeResult;
    private Throwable cause;
    private boolean isTimeout;
    
	@Override
	public void setResponse(Object arg0) {
		// TODO Auto-generated method stub
    	try {
			LOGGER.debug("【回调handle中设置结果，但从反编译出来的代码看，不应该是这个接口的方法，但我这里补上了设置与latch.countDown】");
			LOGGER.debug("【回调handle中设置结果，不应该是这个接口的方法，返回的obj（arg0）是：】"+arg0.toString());
			response=(MiddleMsg)arg0;
			LOGGER.debug("【回调handle中设置结果，并唤醒get线程(补)】"+( (response==null || response.getBody()==null)?"null msg":response.getBody().toString()));
			this.response = response;
			latch.countDown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.warn("【回调handle中设置结果，不应该是这个接口的方法，出现异常了】"+e.toString());
			latch.countDown();
		}
    	
	}
	
	@Override
    public Object getResponse()
    {
        try
        {
        	LOGGER.debug("【获取同步结果等待中】getResponse.this().."+this.toString());
        	LOGGER.debug("【获取同步结果等待中】时长设置为(ms)：.."+timeout);
        	latch.await(timeout, TimeUnit.MILLISECONDS);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
            LOGGER.warn("【获取同步结果等待中.出错了！..】"+e.toString());
        }
        LOGGER.debug("【获取同步结果等待中】等待时间为(ms)："+(System.currentTimeMillis() - begin));
        return response;
    }
	
    /**
     * 获取结果时阻塞中
     */
    public MiddleMsg get()
        throws InterruptedException, ExecutionException
    {
    	LOGGER.info("【获取同步结果等待中.get.this()..】"+this.toString());
    	MiddleMsg middleMsg=(MiddleMsg) getResponse();
    	LOGGER.info("【获取同步结果等待中.get.middleMsg()..】"+middleMsg.toString());
        return middleMsg;
    }
	
}
