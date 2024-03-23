package org.artomic.netty.route.dynamic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import org.artomic.netty.route.ApiMessage;
import org.artomic.netty.route.ApiSession;
import org.artomic.netty.route.HalfDecodeMsg;
import org.artomic.netty.route.SimpleRouterChannelHandler;
import org.artomic.netty.route.anno.ApiDef;
import org.artomic.netty.route.anno.ApiSessionId;
import org.artomic.netty.route.exception.AnnoRouterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimpleDynamicProcessor<T> implements IDynamicImplProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SimpleDynamicProcessor.class);
    
    protected SimpleRouterChannelHandler<T> routerHandler;
    
    private boolean multiSession;
    
    public SimpleDynamicProcessor(SimpleRouterChannelHandler<T> routerHandler, boolean multiSession) {
        this.routerHandler = routerHandler;
        this.multiSession = multiSession;
    }

    @Override
    public void creatingPreprocess(Class<?> dynamicImplInterface) {
        // check interface method define in debug environment
        
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object doMethodInvoke(Class<?> dynamicImplInterface, Method method, Object[] args) {
        ApiDef apiDef = method.getAnnotation(ApiDef.class);
        if (apiDef == null) {
            logger.error("no ApiDef annonation at method. do nothing. dynamicImplInterface:{}, method:{}", 
                    dynamicImplInterface, method);
            throw new IllegalArgumentException("No ApiDef annonation at method. do nothing");
        }
        
        ApiSession vs = getSession(method, args);
        ApiMessage<T> msg = getReqMsg(method, args);
        if (msg == null) {
            msg = genNoBodyMessage();
        }
        
        logger.debug("Send message api group :{}, api action : {}, message :{}", apiDef.group(), apiDef.action(), msg);
        msg.obtainHeader().setApiGroup(apiDef.group());
        msg.obtainHeader().setApiAction(apiDef.action());
        Class<?> returnType = method.getReturnType();
        if (returnType.equals(Void.TYPE)) {
            vs.getChannel().writeAndFlush(msg);
            return null;
        } else {
            if (ApiMessage.class.isAssignableFrom(returnType)) {
                HalfDecodeMsg<T> halfDecodeMsg =  routerHandler.synSendMsg(vs, msg);
                ApiMessage<T> rsp = continueDecodeMsg(halfDecodeMsg, (Class<ApiMessage<T>>)returnType);
                logger.debug("Receive message api group :{}, api action : {}, message :{}", 
                        apiDef.group(), apiDef.action(), rsp);
                preRspProcess(rsp);
                return rsp;
            } else {
                logger.error("no ApiDef annonation at method. do nothing. dynamicImplInterface:{}, method:{}, returnType:{}", 
                        dynamicImplInterface, method, returnType);
                throw new IllegalArgumentException("Api returnType is no ApiMessage");
            }
        }
    }
    
    /**
     * 生成没有body消息
     * @return
     */
    abstract protected ApiMessage<T> genNoBodyMessage();
    
    private ApiSession getSession(Method method, Object[] args) {
        Parameter[] paras = method.getParameters();
        for (int i = 0; i < paras.length; i++) {
            Parameter para = paras[i];
            if (para.getAnnotation(ApiSessionId.class) != null) {
                Object obj = args[i];
                if (obj instanceof String) {
                    String sessionId = args[i].toString();
                    ApiSession session = routerHandler.findSession(sessionId);
                    if (session == null) {
                        throw new AnnoRouterException(AnnoRouterException.ERR_SESSION_NOREADY, "Connection is no ready");
                    }
                    return session;
                    
                } else if (obj instanceof ApiSession) {
                    return (ApiSession)obj;
                }
            } else {
                if (ApiSession.class.isAssignableFrom(para.getType())) {
                    return (ApiSession)args[i];
                }
            }
        }
        if (multiSession) {
            logger.error("Method ApiSession or @ApiSessionId para no found.");
            throw new IllegalArgumentException("Method ApiSession or @ApiSessionId para no found.");
        } else {
            List<ApiSession> sessions = routerHandler.getAllSession();
            if (!sessions.isEmpty()) {
                return sessions.get(0);
            } else {
                throw new AnnoRouterException(AnnoRouterException.ERR_SESSION_NOREADY, "Connection is no ready");
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private ApiMessage<T> getReqMsg(Method method, Object[] args) {
        Class<?>[] paraTypes = method.getParameterTypes();
        for (int i = 0; i < paraTypes.length ; i++) {
            Class<?> paraType = paraTypes[i];
            if (ApiMessage.class.isAssignableFrom(paraType)) {
                return (ApiMessage<T>)args[i];
            }
        }
        return null;
    }
    
    protected ApiMessage<T> continueDecodeMsg(HalfDecodeMsg<T> req, Class<ApiMessage<T>> returnPara) {
        try {
            ApiMessage<T> message = returnPara.getConstructor().newInstance();
            message.setupHeader(req.getHeader());
            message.decodeBody(req.getUndecodedMsg());
            return message;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            logger.error("", e);
            logger.error("ApiMessage must have a public Constructor. ApiMessage class:{}", returnPara);
            throw new IllegalArgumentException(returnPara.getName() + " must have a public Constructor");
        }
    }
    
    /**
     * 响应消息返回前处理，用途：返回消息发生业务错误时，以异常的形式抛出
     * @param rsp
     */
    protected void preRspProcess(ApiMessage<T> rsp) {
        //TODO
//        if (StringUtils.isNotEmpty(rsp.getCode()) && !StringUtils.equals(GlobalConstant.SUCCESS_STR_CODE, rsp.getCode())) {
//            throw new BizException(Long.parseLong(rsp.getCode())).setOriMsg(rsp.getMsg());
//        }
    }

    public boolean isMultiSession() {
        return multiSession;
    }

    public void setMultiSession(boolean multiSession) {
        this.multiSession = multiSession;
    }
    
}
