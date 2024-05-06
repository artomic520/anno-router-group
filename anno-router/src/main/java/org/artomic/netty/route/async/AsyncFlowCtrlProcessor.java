package org.artomic.netty.route.async;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.artomic.netty.route.ApiMessage;
import org.artomic.netty.route.Constants;
import org.artomic.netty.route.exception.AnnoRouterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

/**
 * 异步流控处理器
 * 用途：控制发往远端的消息并发量，避免远端消息处理阻塞。
 * concurrentSize：并发请求数量
 * waitCapacity：等待发送消息队列长度
 *
 */
public abstract class AsyncFlowCtrlProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AsyncFlowCtrlProcessor.class);
    private int concurrentSize = 10;//队列的并发数量，等待响应的ReqQueueItem
    private int waitCapacity = 90;//队列最大等待数量
    private DelayQueue<TimeoutItem> timeoutQueue = new DelayQueue<>();//超时队列
    //key:请求ID
    private ConcurrentHashMap<String, ReqQueueItem> waitRspMap = new ConcurrentHashMap<>();
    //key:队列ID
    private ConcurrentHashMap<String, AtomicInteger> waitAmountMap = new ConcurrentHashMap<>();
    //key:队列ID
    private ConcurrentHashMap<String, Queue<ReqQueueItem>> queueMap = new ConcurrentHashMap<>();
    
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    
    private ThreadPoolTaskExecutor executor = null;
    
    public AsyncFlowCtrlProcessor(ThreadPoolTaskExecutor executor) {
        this(10, executor);
    }
    
    public AsyncFlowCtrlProcessor(int concurrentSize, ThreadPoolTaskExecutor executor) {
        this(concurrentSize, executor, null, null);
    }
    
    public AsyncFlowCtrlProcessor(int concurrentSize, ThreadPoolTaskExecutor executor, String timeOutThreadName, String takeThreadName) {
        this.concurrentSize = concurrentSize;
        this.executor = executor;
        startTimeOutThread(timeOutThreadName);
        startLoopTakeThread(takeThreadName);
    }
    
    private void startTimeOutThread(String timeOutThreadName) {
        if (timeOutThreadName == null) {
            timeOutThreadName = "t-flow-control-timeout";
        }
        new Thread(()-> {
            doTimeoutWork();
        }, timeOutThreadName).start();
    }
    
    private void startLoopTakeThread(String takeThreadName) {
        if (takeThreadName == null) {
            takeThreadName = "t-flow-control-take";
        }
        new Thread(()-> {
            loopWork();
        }, takeThreadName).start();
    }
    
    private void doTimeoutWork() {
        while (true) {
            try {
                TimeoutItem timeoutItem = timeoutQueue.take();
                ReqQueueItem reqItem = waitRspMap.remove(timeoutItem.getFrameId());
                if (reqItem != null) {
                    waitAmountMap.get(reqItem.getQueueId()).decrementAndGet();
                    AsyncCallbackResult result = new AsyncCallbackResult(timeOutException());
                    executor.execute(() -> reqItem.getCallback().callback(result));
                }
            } catch (InterruptedException e) {
            } catch (Exception e) {
                logger.info("", e);
            }
        }
    }
    
    protected RuntimeException timeOutException() {
    	return new AnnoRouterException(Constants.ERR_MSG_RESP_TIMEOUT); 
    }
    
    private void loopWork() {
        while (true) {
            lock.lock();
            try {
                if (canAwait()) {
                    condition.await();
                }
                doTakeWork();
            } catch (InterruptedException e) {
            } finally {
                lock.unlock();
            }
        }
    }
    
    private boolean canAwait() {
        for (Map.Entry<String, Queue<ReqQueueItem>> entry : queueMap.entrySet()) {
            String key = entry.getKey();
            Queue<ReqQueueItem> queue = entry.getValue();
            //queue is not empty and wait response amount is less than concurrentSize
            if (!queue.isEmpty() && waitAmountMap.get(key).get() < concurrentSize) {
                return false;
            }
        }
        return true;
    }
    
    private void doTakeWork() {
        //获取队列中数据进行调用，直达队列为空或并发数满
        Set<Map.Entry<String, Queue<ReqQueueItem>>> set = queueMap.entrySet();
        for (;;) {
            if (takeInOrder(set) == false) {
                break;
            }
        }
    }
    
    private boolean takeInOrder(Set<Map.Entry<String, Queue<ReqQueueItem>>> set) {
        boolean hasData = false;
        for (Map.Entry<String, Queue<ReqQueueItem>> entry : set) {
            String key = entry.getKey();
            if (waitAmountMap.get(key).get() < concurrentSize) {
                ReqQueueItem reqItem = entry.getValue().poll();
                if (reqItem != null) {
                    exeReq(reqItem);
                    hasData = true;
                }
            }
        }
        return hasData;
    }
    
    private void exeReq(ReqQueueItem reqItem) {
        waitAmountMap.get(reqItem.getQueueId()).incrementAndGet();
        waitRspMap.put(reqItem.getFrameId(), reqItem);
        executor.submit(() -> {
            try {
                reqItem.getRun().run();
                
                TimeoutItem ti = new TimeoutItem(reqItem.getFrameId(), reqItem.getTimeout());
                reqItem.setTimeoutItem(ti);
                timeoutQueue.put(ti);
            } catch (Exception e) {
                waitRspMap.remove(reqItem.getFrameId());
                waitAmountMap.get(reqItem.getQueueId()).decrementAndGet();
                //发送消息异常触发回调
                reqItem.getCallback().callback(new AsyncCallbackResult(e));
            }
        });
    }
    
    public void doCall(ReqQueueItem reqItem) {
        doCall(reqItem, TimeUnit.SECONDS.toMillis(Constants.DEFAULT_TIMEOUT));
    }
    
    public void doCall(ReqQueueItem reqItem, long timeout) {
        Assert.notNull(reqItem.getQueueId(), "queueId must not be null");
        Assert.notNull(reqItem.getFrameId(), "frameId must not be null");
        Assert.notNull(reqItem.getCallback(), "callback must not be null");
        Assert.notNull(reqItem.getRun(), "run must not be null");
        reqItem.setTimeout(timeout);
        lock.lock();
        try {
            Queue<ReqQueueItem> queue = getQueue(reqItem.getQueueId());
            if (queue.size() >= waitCapacity) {
                throw remoteEndBusyException();
            }
            queue.add(reqItem);
            int value = waitAmountMap.get(reqItem.getQueueId()).get();
            if (value < concurrentSize) {
                condition.signal();
            }
        } finally {
            lock.unlock();
        }
    }
    
    protected RuntimeException remoteEndBusyException() {
    	return new AnnoRouterException(Constants.ERR_REMOTE_END_BUSY); 
    }
    
    private Queue<ReqQueueItem> getQueue(String queueId) {
        return queueMap.computeIfAbsent(queueId, (id) -> {
            waitAmountMap.put(queueId, new AtomicInteger(0));
            return new ConcurrentLinkedQueue<ReqQueueItem>();
        });
    }
    
    /**
     * 通知消息
     * @param rsp
     */
    public void notifyRespMsg(ApiMessage<?> rsp) {
        ReqQueueItem reqItem = waitRspMap.remove(rsp.obtainHeader().getMsgFrameId());
        if (reqItem != null) {
            timeoutQueue.remove(reqItem.getTimeoutItem());
            int oriValue = waitAmountMap.get(reqItem.getQueueId()).getAndDecrement();
            executor.execute(() -> doCallback(reqItem, rsp));
            if (oriValue == concurrentSize) {//原并发数已满，需尝试唤醒等待队列
                lock.lock();
                try {
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            }
        }
    }
    
    protected abstract void doCallback(ReqQueueItem reqItem, ApiMessage<?> rsp);
    
//    protected void doCallback(ReqQueueItem reqItem, ApiMessage<?> rsp) {
//    //处理示例
//        AsyncCallbackResult result = null;
//        if (StringUtils.isNotEmpty(rsp.getCode()) && !StringUtils.equals(GlobalConstant.SUCCESS_STR_CODE, rsp.getCode())) {
//            BizException exception = new BizException(Long.parseLong(rsp.getCode())).setOriMsg(rsp.getMsg());
//            result = new AsyncCallbackResult(exception);
//        } else {
//            result = new AsyncCallbackResult(rsp);
//        }
//        reqItem.getCallback().callback(result);
//    }
    
    /**
     * 移除队列
     * @param queueId
     */
    public void removeQueue(String queueId) {
        lock.lock();
        try {
            Queue<ReqQueueItem> queue = queueMap.get(queueId);
            if (!queue.isEmpty() || waitAmountMap.get(queueId).get() != 0) {
                logger.error("queueId={} has datas to process, can not be removed");
                throw remoteEndBusyException();
            }
            queueMap.remove(queueId);
            waitAmountMap.remove(queueId);
        } finally {
            lock.unlock();
        }
    }
    
    public int getConcurrentSize() {
        return concurrentSize;
    }

    public void setConcurrentSize(int concurrentSize) {
        this.concurrentSize = concurrentSize;
    }

    public int getWaitCapacity() {
        return waitCapacity;
    }

    public void setWaitCapacity(int waitCapacity) {
        this.waitCapacity = waitCapacity;
    }
    
    public static class ReqQueueItem {
        private String queueId; 
        private String frameId;
        private AsyncCallback callback;
        private Runnable run;
        private long timeout;
        private TimeoutItem timeoutItem;
        
        public ReqQueueItem() {
            
        }
        
        public ReqQueueItem(String queueId, String frameId, AsyncCallback callback, Runnable run) {
            this.queueId = queueId;
            this.frameId = frameId;
            this.callback = callback;
            this.run = run;
        }

		public String getQueueId() {
			return queueId;
		}

		public void setQueueId(String queueId) {
			this.queueId = queueId;
		}

		public String getFrameId() {
			return frameId;
		}

		public void setFrameId(String frameId) {
			this.frameId = frameId;
		}

		public AsyncCallback getCallback() {
			return callback;
		}

		public void setCallback(AsyncCallback callback) {
			this.callback = callback;
		}

		public Runnable getRun() {
			return run;
		}

		public void setRun(Runnable run) {
			this.run = run;
		}

		public long getTimeout() {
			return timeout;
		}

		public void setTimeout(long timeout) {
			this.timeout = timeout;
		}

		public TimeoutItem getTimeoutItem() {
			return timeoutItem;
		}

		public void setTimeoutItem(TimeoutItem timeoutItem) {
			this.timeoutItem = timeoutItem;
		}

		@Override
		public String toString() {
			return "ReqQueueItem [queueId=" + queueId + ", frameId=" + frameId + ", callback=" + callback + ", run="
					+ run + ", timeout=" + timeout + ", timeoutItem=" + timeoutItem + "]";
		}
        
    }
    
    
    public static class TimeoutItem implements Delayed {
        private long time;//触发时间
        private String frameId;
        
        /**
         * @param delayTime 单位：毫秒
         */
        public TimeoutItem(String frameId, long delayTime) {
            this.frameId = frameId;
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

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public String getFrameId() {
			return frameId;
		}

		public void setFrameId(String frameId) {
			this.frameId = frameId;
		}

		@Override
		public String toString() {
			return "TimeoutItem [time=" + time + ", frameId=" + frameId + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((frameId == null) ? 0 : frameId.hashCode());
			result = prime * result + (int) (time ^ (time >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TimeoutItem other = (TimeoutItem) obj;
			if (frameId == null) {
				if (other.frameId != null)
					return false;
			} else if (!frameId.equals(other.frameId))
				return false;
			if (time != other.time)
				return false;
			return true;
		}
        
    }
}
