package org.artomic.netty.demo.server.api;

import org.artomic.netty.demo.dto.HelloMessage;
import org.artomic.netty.demo.dto.RspMessage;
import org.artomic.netty.route.ApiSession;
import org.artomic.netty.route.anno.ApiDef;
import org.artomic.netty.route.dynamic.anno.DynamicApi;

@DynamicApi
public interface MyServerTestApi {
    
    @ApiDef(action = "serverHello")
    RspMessage<?> hello(ApiSession as, HelloMessage req);
}
