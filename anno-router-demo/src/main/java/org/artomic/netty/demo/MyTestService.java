package org.artomic.netty.demo;

import javax.annotation.PostConstruct;

import org.artomic.netty.demo.client.ClientConnMgmt;
import org.artomic.netty.demo.client.api.MyClientTestApi;
import org.artomic.netty.demo.client.async.MyAsyncService;
import org.artomic.netty.demo.dto.HelloMessage;
import org.artomic.netty.demo.dto.RspMessage;
import org.artomic.netty.demo.server.ServerChannelInboundHandler;
import org.artomic.netty.demo.server.ServerConnMgmt;
import org.artomic.netty.demo.server.api.MyServerTestApi;
import org.artomic.netty.route.ApiSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyTestService {
    @Autowired
    private ClientConnMgmt client;
    @Autowired
    private ServerConnMgmt server;
    @Autowired
    private MyClientTestApi myClientTestApi;
    @Autowired
    private MyServerTestApi myServerTestApi;
    @Autowired
    private MyAsyncService myAsyncService;
    @Autowired
    private PerfTestService perfTestService;
    
    @Autowired
    private ServerChannelInboundHandler serverChannelInboundHandler;
    
    @PostConstruct
    private void init() {
    	startTest();
    }
    
    public void startTest() {
        new Thread(()-> {
            try {
            	startSrv();
                doTest();
                perfTestService.synPerf();
//                perfTestService.asynPerf();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            	stopSrv();
            }
        }, "test-thread").start();
    }
    
    private void startSrv() throws Exception {
        server.startServer();
        System.out.println("Server initial");
        Thread.sleep(2 * 1000L);
        boolean connected = client.connect();
        System.out.println("client connect " +  (connected?"success" : "fail"));
    }
    
    private void doTest() throws Exception {
        
        Thread.sleep(2 * 1000L);
        String clientSay = "ni shi ge hao ren";
        System.out.println("client send:" + clientSay);
        RspMessage<?> rsp = myClientTestApi.hello(new HelloMessage(clientSay));
        System.out.println(rsp.getBody().getMsg());
        System.out.println("-------------------");
        
        Thread.sleep(2 * 1000L);
        ApiSession as = serverChannelInboundHandler.getAllSession().get(0);
        String serverSay = "ni ye shi ge hao ren";
        System.out.println("server send:" + serverSay);
        RspMessage<?> rsp2 = myServerTestApi.hello(as, new HelloMessage(serverSay));
        System.out.println(rsp2.getBody().getMsg());
        
        System.out.println("-------------------");
        Thread.sleep(2 * 1000L);
        String asynSay = "ni shi ge async hao ren";
        System.out.println("server send:" + asynSay);
        myAsyncService.asyncTest(new HelloMessage(asynSay), (result) -> {
        	System.out.println(((RspMessage<?>)result.getValue()).getBody().getMsg());
        });
        
    }
    
    private void stopSrv() {
    	try {
			Thread.sleep(5 * 1000);
			client.disConnect();
			
			Thread.sleep(3 * 1000);
			server.stopServer();
			Thread.sleep(3 * 1000);
			System.exit(0);
		} catch (InterruptedException e) {
		}
    }

}
