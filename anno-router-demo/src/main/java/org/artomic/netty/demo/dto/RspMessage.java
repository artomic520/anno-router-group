package org.artomic.netty.demo.dto;

import org.artomic.netty.demo.AppApiMessage;

public class RspMessage<T> extends AppApiMessage<RspMessage.RspBody<T>> {

    public RspMessage() {
        super();
        setBody(new RspBody<>());
        obtainHeader().setReqMsg(false);
    }
    
    @Override
    protected Class<?> getBodyClass() {
        return RspBody.class;
    }
    
    public static class RspBody<V> {
        private String code;
        private String msg;
        private V data;
        
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
        public V getData() {
            return data;
        }
        public void setData(V data) {
            this.data = data;
        }
    }
}
