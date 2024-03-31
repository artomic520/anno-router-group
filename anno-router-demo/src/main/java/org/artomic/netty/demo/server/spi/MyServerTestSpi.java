package org.artomic.netty.demo.server.spi;

import javax.annotation.PostConstruct;

import org.artomic.netty.demo.dto.HelloMessage;
import org.artomic.netty.demo.dto.RspMessage;
import org.artomic.netty.route.ApiSession;
import org.artomic.netty.route.anno.ApiDef;
import org.artomic.netty.route.anno.ApiSpi;

@ApiSpi
public class MyServerTestSpi {
	
	@PostConstruct
	public void init() {
		System.out.println("MyServerTestSpi1 initial");
	}

    @ApiDef(action = "clientHello")
    public RspMessage<?> hello(ApiSession session, HelloMessage req) {
        RspMessage<?> rsp = new RspMessage<>();
        rsp.getBody().setCode("0");
        rsp.getBody().setMsg("server response : jie jie jie");
        return rsp;
    }
}
