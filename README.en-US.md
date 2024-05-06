### Introduction
Anno router is a fully duplex annotated routing processor based on Springboot+Netty. The functionality is similar to the @PostMapping annotation provided by SpringMVC and the JPA @Repository annotation. This component is used to reduce the complexity of socket long connection business development. For detailed usage, please refer to the "anno-router-demo" project
### feature
1. Based on annotation processing to receive messages (similar to annotation capabilities such as SpringMVC Post/Get)
2. Send synchronous messages based on annotation interfaces (similar to JPA Repository annotation capabilities)
### Effect of Application
#### Server receives message definition:
```Java
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
#### Server sends message definition:
```Java
@DynamicApi
public interface MyServerTestApi {
    //Return type void is asynchronous message, otherwise it is synchronous message
    @ApiDef(action = "serverHello")
    RspMessage<?> hello(ApiSession as, HelloMessage req);
}

//you can also use annonation @ApiSessionId
@DynamicApi
public interface MyServerTestApi {
    @ApiDef(action = "serverHello")
    RspMessage<?> hello(@ApiSessionId String sessionId, HelloMessage req);
}
```
#### Client receives message definition:
```Java
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
#### Client sends message definition:
```Java
@DynamicApi
Public interface MyClientTestApi{
    //Return type void is asynchronous message, otherwise it is synchronous message
    //The client only has one Session, so there is no need for the ApiSession parameter
    @ApiDef (action="clientHello")
    RspMessage<?> Hello (HelloMessage req);
}
```
#### Message sending and receiving demonstration:
```Java
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
#### Output:
```Java
Server initial
Client connect success
Client sent: ni shi ge hao ren
Server response: jie jie jie jie
Server sent: ni ye shi ge hao ren
Client response: jie jie jie
```
### License
Apache License Version 2.0