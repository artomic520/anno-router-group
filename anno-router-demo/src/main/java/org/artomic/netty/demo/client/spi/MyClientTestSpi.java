package org.artomic.netty.demo.client.spi;

import org.artomic.netty.demo.client.async.MyAsyncService;
import org.artomic.netty.demo.dto.HelloMessage;
import org.artomic.netty.demo.dto.RspMessage;
import org.artomic.netty.route.anno.ApiDef;
import org.artomic.netty.route.anno.ApiSpi;
import org.springframework.beans.factory.annotation.Autowired;

@ApiSpi
public class MyClientTestSpi {
	@Autowired
	private MyAsyncService myAsyncService;

    @ApiDef(action = "serverHello")
    public RspMessage<?> hello(HelloMessage req) {
        RspMessage<?> rsp = new RspMessage<>();
        rsp.getBody().setCode("0");
        rsp.getBody().setMsg("client response : jie jie jie");
        return rsp;
    }
    
    @ApiDef(action = "asyncTest")
    public void asyncTest(RspMessage<?> rsp) {
    	myAsyncService.notifyMsg(rsp);
    }
    
}
