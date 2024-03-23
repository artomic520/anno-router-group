package org.artomic.netty.route;

public class HalfDecodeMsg<T>  {
    
    private ApiHeader header;
    
    private T undecodedMsg;
    
    public HalfDecodeMsg(ApiHeader header, T undecodedMsg) {
        this.header = header;
        this.undecodedMsg = undecodedMsg;
    }
    
    public ApiHeader getHeader() {
        return header;
    }

    public T getUndecodedMsg() {
        return undecodedMsg;
    }
    
}
