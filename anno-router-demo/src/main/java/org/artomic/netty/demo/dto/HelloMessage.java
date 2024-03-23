package org.artomic.netty.demo.dto;

import java.util.UUID;

import org.artomic.netty.demo.AppApiMessage;

public class HelloMessage extends AppApiMessage<HelloMessage.Body> {

    public HelloMessage() {
        this(null);
    }
    
    public HelloMessage(String msg) {
        super();
        obtainHeader().setMsgFrameId(UUID.randomUUID().toString());
        
        Body body = new Body();
        body.setMsg(msg);
        setBody(body);
    }
    
    @Override
    protected Class<?> getBodyClass() {
        return HelloMessage.Body.class;
    }

    public static class Body {
        private String msg;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
        
    }
}
