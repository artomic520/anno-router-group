package org.artomic.netty.demo.client.async;

import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.PostConstruct;

import org.artomic.netty.demo.client.api.MyClientTestApi;
import org.artomic.netty.demo.dto.HelloMessage;
import org.artomic.netty.route.ApiMessage;
import org.artomic.netty.route.async.AsyncCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;


@Service
public class MyAsyncService {
	@Autowired
	private MyClientTestApi myClientTestApi;
	
	private ClientAsyncProcessor asyncProcessor;
	
	@PostConstruct
	private void initial() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(50);
		executor.setQueueCapacity(10000);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();		
		asyncProcessor = new ClientAsyncProcessor(executor);
		
	}
	
	public void asyncTest(HelloMessage req, AsyncCallback cb) {
		asyncProcessor.doCall(req.obtainHeader().getMsgFrameId(), cb, 
				() -> myClientTestApi.asyncTest(req));
	}
	
	public void notifyMsg(ApiMessage<?> rsp) {
		asyncProcessor.notifyRespMsg(rsp);
	}
	
}
