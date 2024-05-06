package org.artomic.netty.route;

public interface Constants {
	long DEFAULT_TIMEOUT = 30;//unit seccond
	String KEY_SESSION_ID = "KEY_SESSION_ID";
	
	//error code
	long ERR_MSG_RESP_TIMEOUT = 10;
	long ERR_SESSION_NOREADY = 11;
	long ERR_REMOTE_END_BUSY = 12;
}
