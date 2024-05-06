package org.artomic.netty.demo.client.async;

import org.apache.commons.lang3.StringUtils;
import org.artomic.netty.demo.dto.RspMessage;
import org.artomic.netty.route.ApiMessage;
import org.artomic.netty.route.async.AsyncCallback;
import org.artomic.netty.route.async.AsyncCallbackResult;
import org.artomic.netty.route.async.AsyncSimpleProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class ClientAsyncProcessor extends AsyncSimpleProcessor {

	public ClientAsyncProcessor(ThreadPoolTaskExecutor executor) {
		super(executor);
	}

	@Override
	protected void doCallback(AsyncCallback cb, ApiMessage<?> msg) {
		RspMessage<?> rsp = (RspMessage<?>) msg;
		AsyncCallbackResult result = null;
		if (StringUtils.isNotEmpty(rsp.getBody().getCode())
				&& !StringUtils.equals("0", rsp.getBody().getCode())) {
			RuntimeException exception = new RuntimeException(
					"code=" + rsp.getBody().getCode() +" message='" + rsp.getBody().getMsg() + "'");
			result = new AsyncCallbackResult(exception);
		} else {
			result = new AsyncCallbackResult(rsp);
		}
		cb.callback(result);
	}
}
