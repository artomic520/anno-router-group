package org.artomic.netty.demo.client.spi;

import org.artomic.netty.demo.dto.HelloMessage;
import org.artomic.netty.demo.dto.RspMessage;
import org.artomic.netty.route.anno.ApiDef;
import org.artomic.netty.route.anno.ApiSpi;

@ApiSpi
public class MyClientTestSpi {

    @ApiDef(action = "serverHello")
    public RspMessage<?> hello(HelloMessage req) {
        RspMessage<?> rsp = new RspMessage<>();
        rsp.getBody().setCode("0");
        rsp.getBody().setMsg("client response : jie jie jie");
        return rsp;
    }
}
