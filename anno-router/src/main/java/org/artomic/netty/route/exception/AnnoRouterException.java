package org.artomic.netty.route.exception;

public class AnnoRouterException extends RuntimeException {
    private static final long serialVersionUID = 9017071389695394060L;
    
    public static final long ERR_MSG_RESP_TIMEOUT = 10;
    public static final long ERR_SESSION_NOREADY = 11;
    
    private long errCode;
    
    public AnnoRouterException(long errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public long getErrCode() {
        return errCode;
    }
    
}
