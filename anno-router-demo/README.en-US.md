### Introduction
This project is used to provide a detailed introduction to the usage of anno router

### Step
Taking the socker server as an example, this article introduces the usage of anno root
1. Define the message header AppHeader and implement the ApiHeader interface. The message header contains message instructions, message direction, and message frame identifier
   > ApiGroup and apiAction are used to identify message instructions, where apiGroup is optional, apiAction is mandatory, and the values of apiGroup+apiAction must be unique

   > The isReqMsg method is used to identify whether a message is a request or a response message, where true represents the request and false represents the response. This method is used in conjunction with msgFrameId to support synchronous response functionality for messages. If synchronous response function is not required, isReqMsg returns true by default, and the value of msgFrameId is null

2. Define the message body abstract class AppApiMessage and implement the ApiMessage interface. Used to obtain and set message headers, as well as decode message bodies
   > ObtainHeader and setupHeader, used to retrieve and set message headers

   > The decodeBody method decodes the message body. The method input parameter is the undecodedMsg in the abstract method return value HalfDecodeMsg implemented in step 7. In this example, the decoded data is a byte stream, so ByteBuf of netty is used. In this example, the message body is JSON data, so the Jackson tool is used to directly decode the data without the need to decode it separately in subclasses.

3. Create a message receiving call processor called ServerApiInvokeProcessor, inheriting the ApiInvokeProcessor abstract class, which is used for routing processing after receiving messages. The following methods need to be implemented.
   > The getBean method returns all registered SPI beans, usually obtained directly using the AppContext. getBean method.

   > The doHandleException method, used for unified exception handling, returns a response message from the opposite end.
   
4. Create a message dynamic interface to send the processor ServerDynamicProcessor, inheriting the SimpleDynamic Processor abstract class. This class is a dynamic implementation class for the Message Sending Interface API. This class needs to implement the following two points
   > Construction method to inject the InboundHandler object in step 5, and set the server-side multisession to true

   > Implement the genNoBodyMessage method to return a special message with only the message header but no message body.
   
5. Create a netty InboundHandler message routing processor object, ServerChannelInboundHandler, inheriting the SimpleRouterChannelHandler abstract class.
   > The construction method needs to inject the ServerApiInvokeProcessor from step 3, where scanPaths is the SPI scan path

   > This object often rewrites methods such as userEventTriggered, channelInactive, and channelActive to perform business operations such as connection timing handshake, reconnection, and login. When rewriting, pay attention to calling the super method to avoid losing the business logic of the parent class
6. Create an encoder AppEncoder, which is the same step as other netty projects. Encode the message when sending it
7. Create a decoder AppDecoder, inherit the HalfDecoder abstract class, and implement the decodeHalfMsg method. The HalfDecodeMsg object returned by this method includes the message header ApiHeader and the undedecoded message body undecodedMsg. UndecodedMsg is the input parameter for the second step of message decoding.
8. Create a connection manager using netty, add AppEncoder, AppDecoder, and ServerChannelInboundHandler
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
                        
                        p.addLast(new AppDecoder());//
                        p.addLast(new AppEncoder());//
                        p.addLast(executorGroup, serverChannelInboundHandler);//
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
9.  Configure Spring Configuration, add SPI scan path and Dynamic API scan path
```java
@Configuration
@DynamicImplScan(value = {"org.artomic.netty.demo.server.api"}, 
                    processorClass = ServerDynamicProcessor.class)
@ApiSpiScan(value = {"org.artomic.netty.demo.server.spi"})
public class ServerConfig {

}

```
10. Define SPI and API separately. Request and response messages in SPI and API must inherit AppApiMessage.
```java
//SPI definition and implementation
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
//API definition
package org.artomic.netty.demo.server.api;
@DynamicApi
public interface MyServerTestApi {
    //Return type void is asynchronous message, otherwise it is synchronous message
    @ApiDef(action = "serverHello")
    RspMessage<?> hello(ApiSession as, HelloMessage req);
}

```
11. Write a test class, please refer to "org. atomic. net. demo. MyTestService" for details



Note: The steps may seem cumbersome, but most of them are necessary steps for springboot netty programming