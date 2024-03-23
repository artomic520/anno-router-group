package org.artomic.netty.demo.client;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.artomic.netty.demo.codec.AppDecoder;
import org.artomic.netty.demo.codec.AppEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;

@Service
public class ClientConnMgmt {

    @Autowired
    private ClientChannelInboundHandler clientChannelInboundHandler;
    
    public boolean connect() {
        final InetSocketAddress socketAddr = 
                InetSocketAddress.createUnresolved("127.0.0.1", 9919);
        EventLoopGroup loop = new NioEventLoopGroup(1);
        UnorderedThreadPoolEventExecutor executorGroup = new UnorderedThreadPoolEventExecutor(5);
        
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(loop)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    
                    p.addLast(new AppDecoder());
                    
                    p.addLast(new AppEncoder());
                    
                    p.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
                    p.addLast(executorGroup, clientChannelInboundHandler);
                }
            });
        bootstrap.remoteAddress(socketAddr);
        ChannelFuture channelFuture = bootstrap.connect();
        try {
            return channelFuture.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        return false;
    }
}
