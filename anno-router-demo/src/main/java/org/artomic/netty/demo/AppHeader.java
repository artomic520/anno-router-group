package org.artomic.netty.demo;

import org.artomic.netty.route.ApiHeader;

public class AppHeader implements ApiHeader {
    
    private String apiGroup;
    private String apiAction;
    private String msgFrameId;
    private boolean reqMsg = true;
    
    public AppHeader() {
    }

    @Override
    public boolean isReqMsg() {
        return reqMsg;
    }
    
    @Override
    public String getApiGroup() {
        return apiGroup;
    }

    @Override
    public void setApiGroup(String apiGroup) {
        this.apiGroup = apiGroup;
    }

    @Override
    public String getApiAction() {
        return apiAction;
    }

    @Override
    public void setApiAction(String apiAction) {
        this.apiAction = apiAction;
    }

    @Override
    public String getMsgFrameId() {
        return msgFrameId;
    }

    @Override
    public void setMsgFrameId(String msgFrameId) {
        this.msgFrameId = msgFrameId;
    }

    public void setReqMsg(boolean reqMsg) {
        this.reqMsg = reqMsg;
    }

    @Override
    public void asRspMsg() {
        setReqMsg(false);
    }
}
