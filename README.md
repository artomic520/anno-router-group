### 简介
anno-router是基于Springboot+Netty全双工注解型路由处理器。功能类似SpringMVC的@PostMapping等注解 + JAP @Repository注解提供的能力。该组件用于简化socket长连接业务开发的复杂度。 详细使用方法参见“anno-router-demo”工程



#### 服务端接收消息定义：
```java
@ApiSpi
public class MyServerTestSpi {
    @ApiDef(action = "clientHello")
    public RspMessage<?> hello(ApiSession session, HelloMessage req) {
        RspMessage<?> rsp = new RspMessage<>();
        rsp.getBody().setCode("0");
        rsp.getBody().setMsg("server response : jie jie jie");
        return rsp;
    }
}
```
#### 服务端发送消息定义:
```java
@DynamicApi
public interface MyServerTestApi {
    //返回类型void为异步消息，否则为同步消息
    @ApiDef(action = "serverHello")
    RspMessage<?> hello(ApiSession as, HelloMessage req);
}

//也可以使用 @ApiSessionId
@DynamicApi
public interface MyServerTestApi {
    @ApiDef(action = "serverHello")
    RspMessage<?> hello(@ApiSessionId String sessionId, HelloMessage req);
}
```
#### 客户端接收消息定义：
```java
@ApiSpi
public class MyClientTestSpi {

    @ApiDef(action = "serverHello")
    public RspMessage<?> hello(HelloMessage req) {
        RspMessage<?> rsp = new RspMessage<>();
        rsp.getBody().setCode("0");
        rsp.getBody().setMsg("client response : jie jie jie");
        return rsp;
    }
}
```
#### 客户端发送消息定义:
```java
@DynamicApi
public interface MyClientTestApi {
    //返回类型void为异步消息，否则为同步消息
    //客户端只一个Session因此无需ApiSession参数
    @ApiDef(action = "clientHello")
    RspMessage<?> hello(HelloMessage req);
}
```
#### 消息收发演示:
```java
@Service
public class MyTestService {
    @Autowired
    private ClientConnMgmt client;
    @Autowired
    private ServerConnMgmt server;
    @Autowired
    private MyClientTestApi myClientTestApi;
    @Autowired
    private MyServerTestApi myServerTestApi;
    @Autowired
    private ServerChannelInboundHandler serverChannelInboundHandler;
    
    @PostConstruct
    public void init() {
        
        new Thread(()-> {
            try {
                doTest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "test-thread").start();
    }
    
    private void doTest() throws Exception {
        Thread.sleep(3 * 1000L);
        server.startServer();
        System.out.println("Server initial");
        Thread.sleep(2 * 1000L);
        boolean connected = client.connect();
        System.out.println("client connect " +  (connected?"success" : "fail"));
        Thread.sleep(2 * 1000L);
        String clientSay = "ni shi ge hao ren";
        System.out.println("client send:" + clientSay);
        RspMessage<?> rsp = myClientTestApi.hello(new HelloMessage(clientSay));
        System.out.println(rsp.getBody().getMsg());
        
        Thread.sleep(2 * 1000L);
        ApiSession as = serverChannelInboundHandler.getAllSession().get(0);
        String serverSay = "ni ye shi ge hao ren";
        System.out.println("server send:" + serverSay);
        RspMessage<?> rsp2 = myServerTestApi.hello(as, new HelloMessage(serverSay));
        System.out.println(rsp2.getBody().getMsg());
        
    }

}

```
#### 输出:
```java
Server initial
client connect success
client send:ni shi ge hao ren
server response : jie jie jie
server send:ni ye shi ge hao ren
client response : jie jie jie
```
