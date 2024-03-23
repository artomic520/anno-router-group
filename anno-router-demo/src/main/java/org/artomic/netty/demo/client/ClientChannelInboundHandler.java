package org.artomic.netty.demo.client;

import org.artomic.netty.route.SimpleRouterChannelHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
@Service
public class ClientChannelInboundHandler extends SimpleRouterChannelHandler<ByteBuf> {
    private static final String[] scanPaths = {"org.artomic.netty.demo.client.spi"};
    
    @Autowired
    public ClientChannelInboundHandler(ClientApiInvokeProcessor invokeProcessor) {
        super(scanPaths, invokeProcessor);
    }

}
