package org.artomic.netty.route.async;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.artomic.netty.route.ApiMessage;
import org.artomic.netty.route.Constants;
import org.artomic.netty.route.exception.AnnoRouterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

/**
 * 异步消息处理器
 * 如未指定线程池，则使用netty消息处理的线程池
 */
public abstract class AsyncSimpleProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AsyncSimpleProcessor.class);

    private DelayQueue<TimeoutItem> timeoutQueue = new DelayQueue<>();//超时队列
    private ConcurrentHashMap<String, ReqQueueItem> callbackMap = new ConcurrentHashMap<>();//key:frameId
    private ThreadPoolTaskExecutor executor = null;
    
    public AsyncSimpleProcessor(ThreadPoolTaskExecutor executor) {
        this(executor, null);
    }
    
    public AsyncSimpleProcessor(ThreadPoolTaskExecutor executor, String timeoutName) {
        this.executor = executor;
        startTimeoutThread(timeoutName);
    }
    
    private void startTimeoutThread(String timeoutName) {
        if (timeoutName == null) {
            timeoutName = "t-async-timeout";
        }
        new Thread(()-> {
            doTimeoutWork();
        }, timeoutName).start();
    }

    private void doTimeoutWork() {
        while (true) {
            try {
                TimeoutItem item = timeoutQueue.take();
                ReqQueueItem reqItem = callbackMap.remove(item.frameId);
                if (reqItem != null) {
                    AsyncCallbackResult result = new AsyncCallbackResult(timeOutException());
                    if (executor != null) {
                        executor.execute(() -> reqItem.callback.callback(result));
                    } else {
                        reqItem.callback.callback(result);
                    }
                }
            } catch (InterruptedException e) {
            } catch (Exception e) {
                logger.error("doTimeoutWork exception", e);
            }
        }
    }
    
    protected RuntimeException timeOutException() {
    	return new AnnoRouterException(Constants.ERR_MSG_RESP_TIMEOUT); 
    }
    
    /**
     * 
     * @param frameId
     * @param callback
     * @param run
     */
    public void doCall(String frameId, AsyncCallback callback, Runnable run) {
        doCall(frameId, callback, run, TimeUnit.SECONDS.toMillis(Constants.DEFAULT_TIMEOUT));
    }
    
    /**
     * 
     * @param frameId
     * @param callback
     * @param run
     * @param timeout
     */
    public void doCall(String frameId, AsyncCallback callback, Runnable run, long timeout) {
        Assert.notNull(frameId, "frameId must not be null");
        Assert.notNull(callback, "callback must not be null");
        Assert.notNull(run, "run must not be null");
        TimeoutItem item = new TimeoutItem(timeout);
        item.frameId = frameId;
        ReqQueueItem reqItem = new ReqQueueItem();
        reqItem.callback = callback;
        reqItem.timeoutItem = item;
        callbackMap.put(frameId, reqItem);
        try {
            run.run();
            timeoutQueue.put(item);
        } catch (Exception e) {
            callbackMap.remove(frameId);
            throw e;
        }
    }
    
    
    /**
     * 通知消息
     * @param rsp
     */
    public void notifyRespMsg(ApiMessage<?> rsp) {
        ReqQueueItem reqItem = callbackMap.remove(rsp.obtainHeader().getMsgFrameId());
        if (reqItem != null) {
            timeoutQueue.remove(reqItem.timeoutItem);
            if (executor != null) {
                executor.execute(() -> doCallback(reqItem.callback, rsp));
            } else {
                doCallback(reqItem.callback, rsp);
            }
        }
    }
    
    abstract protected void doCallback(AsyncCallback cb, ApiMessage<?> rsp);
    
//    protected void doCallback(AsyncCallback cb, ApiMessage<?> rsp) {
//        AsyncCallbackResult result = null;
//        if (StringUtils.isNotEmpty(rsp.getCode()) && !StringUtils.equals(GlobalConstant.SUCCESS_STR_CODE, rsp.getCode())) {
//            BizException exception = new BizException(Long.parseLong(rsp.getCode())).setOriMsg(rsp.getMsg());
//            result = new AsyncCallbackResult(exception);
//        } else {
//            result = new AsyncCallbackResult(rsp);
//        }
//        cb.callback(result);
//    }
    
    private static class ReqQueueItem {
        private AsyncCallback callback;
        private TimeoutItem timeoutItem;
    }
    
    private static class TimeoutItem implements Delayed {
        long time;//触发时间
        String frameId;
        
        /**
         * @param delayTime 单位：毫秒
         */
        public TimeoutItem(long delayTime) {
            this.time = System.currentTimeMillis() + delayTime;
        }
        
        @Override
        public int compareTo(Delayed o) {
            TimeoutItem item = (TimeoutItem)o;
            long dif = this.time - item.time;
            return dif <= 0 ? -1 : 1;
        }
        @Override
        public long getDelay(TimeUnit unit) {
            return time - System.currentTimeMillis();
        }

    }
}
