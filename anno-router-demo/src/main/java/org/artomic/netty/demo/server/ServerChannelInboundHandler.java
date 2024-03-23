package org.artomic.netty.demo.server;

import org.artomic.netty.route.SimpleRouterChannelHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
@Service
public class ServerChannelInboundHandler extends SimpleRouterChannelHandler<ByteBuf> {

    private static final String[] scanPaths = {"org.artomic.netty.demo.server.spi"};
    
    @Autowired
    public ServerChannelInboundHandler(ServerApiInvokeProcessor invokeProcessor) {
        super(scanPaths, invokeProcessor);
    }

}
