package org.artomic.netty.route;

/**
 * 
 * @param <T> body解码对象类型,
 */
public interface ApiMessage<T> {
    
    /**
     * 获取消息头 alias getHeader
     * @return
     */
    ApiHeader obtainHeader();
    
    /**
     * 安装消息头 alias setHeader
     * @param header
     */
    void setupHeader(ApiHeader header);
    
    /**
     * 消息体解码
     * @param in
     */
    void decodeBody(T in);
}
