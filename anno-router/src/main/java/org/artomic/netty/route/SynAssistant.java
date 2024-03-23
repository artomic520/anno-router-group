package org.artomic.netty.route;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SynAssistant<T> {

    private CountDownLatch latch;
    private String reqFrameId;
    private T attatchObj;
    
    public SynAssistant(String reqFrameId) {
        this.reqFrameId = reqFrameId;
        latch = new CountDownLatch(1);
    }
    
    public void notifyMsg(T obj) {
        attatchObj = obj;
        latch.countDown();
    }

    public T await() {
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
        return attatchObj;
    }
    
    public T await(long timeout, TimeUnit unit) {
        try {
            latch.await(timeout, unit);
        } catch (InterruptedException e) {
        }
        return attatchObj;
    }

    public String getReqFrameId() {
        return reqFrameId;
    }

    public void setReqFrameId(String reqFrameId) {
        this.reqFrameId = reqFrameId;
    }

    public T getAttatchObj() {
        return attatchObj;
    }

    public void setAttatchObj(T attatchObj) {
        this.attatchObj = attatchObj;
    }
}
