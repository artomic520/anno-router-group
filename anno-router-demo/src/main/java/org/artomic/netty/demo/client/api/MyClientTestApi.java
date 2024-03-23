package org.artomic.netty.demo.client.api;

import org.artomic.netty.demo.dto.HelloMessage;
import org.artomic.netty.demo.dto.RspMessage;
import org.artomic.netty.route.anno.ApiDef;
import org.artomic.netty.route.dynamic.anno.DynamicApi;

@DynamicApi
public interface MyClientTestApi {
    
    @ApiDef(action = "clientHello")
    RspMessage<?> hello(HelloMessage req);
}
