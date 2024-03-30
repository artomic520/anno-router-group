### 简介
该工程用于详细介绍anno-router的用法

### 步骤
这里以socker server端为例，介绍anno-rooter的使用方法
1. 定义消息头AppHeader，实现ApiHeader接口。消息头中包含消息指令、消息方向和消息帧标识
   > apiGroup和apiAction用来标识消息指令，其中apiGroup可选，apiAction必选，apiGroup+apiAction的值必须有唯一性

   > isReqMsg方法用来标识消息是请求或者是响应消息，true表示请求，false表示响应。该方法与msgFrameId结合使用，用来支持消息的同步响应功能。如无需支持同步响应功能，isReqMsg默认返回true，msgFrameId值为null

2. 定义消息体抽象类AppApiMessage，实现ApiMessage接口。用来获取、设置消息头，以及对消息体进行解码
   > obtainHeader和setupHeader，用于获取和设置消息头

   > decodeBody方法进行消息体解码。方法入参为第7步实现的抽象方法返回值HalfDecodeMsg中的undecodedMsg。在该示例中，解码的数据是字节流，因此使用netty的ByteBuf。在该示例中消息体为json数据，因此使用jackson工具直接进行数据解码，无需在子类中分别解码。

3. 创建消息接收调用处理器ServerApiInvokeProcessor，继承ApiInvokeProcessor抽象类，该类用于接收消息后的路由处理，需实现以下方法
   > getBean方法，返回所有注册的SPI Bean，一般直接使用ApplicationContext.getBean方法获取Bean。

   > doHandleException方法，用于统一的异常处理，返回对端的响应消息。
   
4. 创建消息动态接口发送处理器ServerDynamicProcessor，继承SimpleDynamicProcessor抽象类。该类是发送消息Interface API的动态实现类。该类需实现以下两点
   > 构造方法注入第5步的InboundHandler对象，server端multiSession设为true

   > 实现genNoBodyMessage方法，返回只有消息头没有消息体的特殊消息。
   
5. 创建netty InboundHandler消息路由处理器对象ServerChannelInboundHandler，继承SimpleRouterChannelHandler抽象类。
   > 构造方法需注入第3步的ServerApiInvokeProcessor，scanPaths为SPI扫描路径

   > 该对象经常重写userEventTriggered、channelInactive、channelActive等方法，用来进行连接定时握手、重连、登录等业务操作，再重写时注意调用super方法，避免丢失父类的业务逻辑
6. 创建编码器AppEncoder，该步骤与其他netty项目相同，发送消息时对消息进行编码
7. 创建解码器AppDecoder，继承HalfDecoder抽象类，实现decodeHalfMsg方法。该方法返回的HalfDecodeMsg对象包含消息头ApiHeader和未完成解码的消息体undecodedMsg。undecodedMsg是第2步消息解码的入参
8. 使用netty创建连接管理器，加入AppEncoder、AppDecoder和ServerChannelInboundHandler
```java

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
                        
                        p.addLast(new AppDecoder());//解码器
                        p.addLast(new AppEncoder());//编码器
                        p.addLast(executorGroup, serverChannelInboundHandler);//消息路由处理器
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
```
9.  配置spring Configuration，添加SPI扫描路径和Dynamic API扫描路径
```java
@Configuration
@DynamicImplScan(value = {"org.artomic.netty.demo.server.api"}, 
                    processorClass = ServerDynamicProcessor.class)
@ApiSpiScan(value = {"org.artomic.netty.demo.server.spi"})
public class ServerConfig {

}

```
10. 分别定义SPI和API。SPI和API中的请求消息和响应消息需继承AppApiMessage。
```java
//SPI定义和实现
package org.artomic.netty.demo.server.spi;

@ApiSpi
public class MyServerTestSpi {

    @ApiDef(action = "clientHello")
    public RspMessage<?> hello(ApiSession session, HelloMessage req) {
        RspMessage<?> rsp = new RspMessage<>();
        rsp.getBody().setCode("0");
        rsp.getBody().setMsg("server response : jie jie jie");
        return rsp;
    }
    //HelloMessage extend AppApiMessage 
}
//API定义
package org.artomic.netty.demo.server.api;
@DynamicApi
public interface MyServerTestApi {
    //返回类型void为异步消息，否则为同步消息
    @ApiDef(action = "serverHello")
    RspMessage<?> hello(ApiSession as, HelloMessage req);
}

```
11. 编写测试类，详细参见org.artomic.netty.demo.MyTestService

