package org.artomic.netty.route.async;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.artomic.netty.route.Constants;
import org.artomic.netty.route.exception.AnnoRouterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncToSyncAssistant implements AsyncCallback {
    private static final Logger logger = LoggerFactory.getLogger(AsyncToSyncAssistant.class);

    private CountDownLatch latch;
    private AsyncCallbackResult result; 
    
    public AsyncToSyncAssistant() {
        latch = new CountDownLatch(1);
    }
    
    @Override
    public void callback(AsyncCallbackResult result) {
        this.result = result;
        latch.countDown();
    }

    /**
     * 等待命令结束
     * @return
     */
    public AsyncCallbackResult waitFinish() {
        return waitFinish(Constants.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }
    
    /**
     * 
     * @param time 时长
     * @param unit 单位
     * @return
     */
    public AsyncCallbackResult waitFinish(long time, TimeUnit unit) {
        try {
            latch.await(time, unit);
        } catch (InterruptedException e) {
        }
        if (result == null) {
        	logger.error("wait finish timeout");
            throw new AnnoRouterException(Constants.ERR_MSG_RESP_TIMEOUT, "wait finish timeout");
        }
        return result;
    }
}
