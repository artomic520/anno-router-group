package org.artomic.netty.demo.server;

import org.artomic.netty.demo.dto.NoBodyMessage;
import org.artomic.netty.route.ApiMessage;
import org.artomic.netty.route.dynamic.SimpleDynamicProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.netty.buffer.ByteBuf;

@Service
public class ServerDynamicProcessor extends SimpleDynamicProcessor<ByteBuf> {

    @Autowired
    public ServerDynamicProcessor(ServerChannelInboundHandler routerHandler) {
        super(routerHandler, true);
    }

    @Override
    protected ApiMessage<ByteBuf> genNoBodyMessage() {
        return new NoBodyMessage();
    }

}
