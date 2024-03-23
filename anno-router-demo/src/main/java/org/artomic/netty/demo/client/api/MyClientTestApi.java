package org.artomic.netty.demo.client.api;

import org.artomic.netty.demo.dto.HelloMessage;
import org.artomic.netty.demo.dto.RspMessage;
import org.artomic.netty.route.anno.ApiDef;
import org.artomic.netty.route.dynamic.anno.DynamicApi;

@DynamicApi
public interface MyClientTestApi {
    //返回类型void为异步消息，否则为同步消息
    //客户端只一个Session因此无需ApiSession参数
    @ApiDef(action = "clientHello")
    RspMessage<?> hello(HelloMessage req);
}
