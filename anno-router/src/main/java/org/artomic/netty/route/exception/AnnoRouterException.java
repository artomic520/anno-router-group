package org.artomic.netty.route.exception;

public class AnnoRouterException extends RuntimeException {
    private static final long serialVersionUID = 9017071389695394060L;
    
    private long errCode;
    
    public AnnoRouterException(long errCode) {
    	this(errCode, "");
    }
    
    public AnnoRouterException(long errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public long getErrCode() {
        return errCode;
    }
    
}
