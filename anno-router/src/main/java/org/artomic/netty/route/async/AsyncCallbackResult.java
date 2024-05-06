package org.artomic.netty.route.async;

public class AsyncCallbackResult {

    private boolean success;
    private Object value;
    private Exception exception;
    
    public AsyncCallbackResult(boolean success) {
        this.success = success;
    }
    
    public AsyncCallbackResult(Object value) {
        this.success = true;
        this.value = value;
    }
    
    public AsyncCallbackResult(Exception exception) {
        this.exception = exception;
        this.success = false;
    }

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
    
}
