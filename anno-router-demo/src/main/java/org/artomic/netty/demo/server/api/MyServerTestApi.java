package org.artomic.netty.demo.server.api;

import org.artomic.netty.demo.dto.HelloMessage;
import org.artomic.netty.demo.dto.RspMessage;
import org.artomic.netty.route.ApiSession;
import org.artomic.netty.route.anno.ApiDef;
import org.artomic.netty.route.dynamic.anno.DynamicApi;

@DynamicApi
public interface MyServerTestApi {
    //返回类型void为异步消息，否则为同步消息
    @ApiDef(action = "serverHello")
    RspMessage<?> hello(ApiSession as, HelloMessage req);
}
