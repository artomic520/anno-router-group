package org.artomic.netty.demo.server;

import java.util.concurrent.TimeUnit;

import org.artomic.netty.demo.codec.AppDecoder;
import org.artomic.netty.demo.codec.AppEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;

@Service
public class ServerConnMgmt {
    @Autowired
    private ServerChannelInboundHandler serverChannelInboundHandler;

    public void startServer() {
        new Thread(()->{
            doStart();
        },"server-thead").start();
    }
    
    private void doStart() {
        UnorderedThreadPoolEventExecutor executorGroup = new UnorderedThreadPoolEventExecutor(5);
        
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        
                        p.addLast(new AppDecoder());
                        
                        p.addLast(new AppEncoder());
                        
                        p.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
                        p.addLast(executorGroup, serverChannelInboundHandler);
                    }
                    
                });
            try {
                // Start the server.
                Channel serverChannel = b.bind(9919).sync().channel();
                serverChannel.closeFuture().sync();
            } catch (InterruptedException e) {
            }
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            executorGroup.shutdown();
        }
    }
}
