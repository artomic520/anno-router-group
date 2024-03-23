package org.artomic.netty.route;

public interface ApiHeader {
    
    /**
     * 是否请求消息， 
     * @return true: request message  false: response message
     */
    boolean isReqMsg();
    
    /**
     * 设为响应消息，
     */
    void asRspMsg();
    
    /**
     * API Group
     * @return
     */
    String getApiGroup();
    
    /**
     * API Group
     * @param apiGroup
     */
    void setApiGroup(String apiGroup);
    
    /**
     * API action
     * @return
     */
    String getApiAction();
    
    /**
     * API action
     * @param apiAction
     */
    void setApiAction(String apiAction);
    
    /**
     * 消息帧ID
     * @return
     */
    String getMsgFrameId();
    
    /**
     * 消息帧ID
     * @param msgFrameId
     */
    void setMsgFrameId(String msgFrameId);
    
}
