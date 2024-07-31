package org.artomic.netty.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.artomic.netty.demo.client.api.MyClientTestApi;
import org.artomic.netty.demo.client.async.MyAsyncService;
import org.artomic.netty.demo.dto.HelloMessage;
import org.artomic.netty.demo.dto.RspMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PerfTestService {
	private static final Logger logger = LoggerFactory.getLogger(PerfTestService.class);

    @Autowired
    private MyClientTestApi myClientTestApi;
    @Autowired
    private MyAsyncService myAsyncService;
    
    private void sleep(long millis) {
    	try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
    }
    
    public void synPerf() {
    	sleep(1000);
    	int threadNum = 5;
    	List<Thread> list = new ArrayList<>();
    	for (int i=0; i<threadNum; i++) {
    		final int mark = i;
    		Thread t = new Thread(()-> {
    			doSyncPerf(mark);
    		});
    		list.add(t);
    		t.start();
    	}
    	for (int i=0; i<threadNum; i++) {
    		try {
				list.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
    
    private void doSyncPerf(int mark) {
    	long startTime = System.currentTimeMillis();
    	int num = 100000;
    	int suc = 0;
    	logger.warn("-------Start perf test {} ---------", mark);
    	for (int i=0; i<num; i++) {
    		try {
    			RspMessage<?> rsp = myClientTestApi.hello(new HelloMessage("Hello world!"));
    			rsp.getBody().getMsg();
    			suc ++;
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	long endTime = System.currentTimeMillis();
    	System.out.println("----------- hello perf : " + (suc * 1000)/(endTime - startTime) + "/sec -----------");
    	if (suc != num) {
    		System.err.println("fail count :" + (num - suc));
    	}
    	logger.warn("---------Perf test {} finished---------", mark);
    }
    
    public void asynPerf() {
    	doAsyncPerf();
		sleep(10 * 1000);
    	doAsyncPerf();
    	sleep(10 * 1000);
    	doAsyncPerf();
    }
    
    private void doAsyncPerf() {
    	int num = 100000;//数量太大，会导致线程池队列溢出，性能会下降。
    	final AtomicLong asyncNum = new AtomicLong(0);
    	final AtomicLong sucNum = new AtomicLong(0);
    	final long startTime = System.currentTimeMillis();
    	logger.warn("-------Start perf test---------");
    	for (int i=0; i< num; i++) {
	    	myAsyncService.asyncTest(new HelloMessage("Hello world!"), (result) -> {
	    		if (result.isSuccess()) {
	    			sucNum.addAndGet(1);
	    		}
	    		long nowNum = asyncNum.addAndGet(1);
	    		if (nowNum == num) {
	    			long endTime = System.currentTimeMillis();
	    			System.out.println("----------- hello async perf : " + (sucNum.get() * 1000)/(endTime - startTime) + "/sec -----------");
	    			System.out.println("----------- success num : " + sucNum.get());
	    			logger.warn("---------Perf test finished---------");
	    		}
	    	});
    	}
    }
    
}
