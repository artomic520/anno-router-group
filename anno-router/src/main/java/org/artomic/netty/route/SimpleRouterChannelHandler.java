package org.artomic.netty.route;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.artomic.netty.route.exception.AnnoRouterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public abstract class SimpleRouterChannelHandler<T> extends SimpleChannelInboundHandler<HalfDecodeMsg<T>> {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleRouterChannelHandler.class);
    
    protected Map<String, ApiSession> sessionMap = new ConcurrentHashMap<>();
    
    private Map<String, SynAssistant<HalfDecodeMsg<T>>> syncInfoMap = new ConcurrentHashMap<>();
    
    private long timeOut = Constants.DEFAULT_TIMEOUT;
    private String driver;
    private String[] scanPaths;
    protected ApiInvokeProcessor<T> invokeProcessor;
    
    public SimpleRouterChannelHandler(String[] scanPaths, ApiInvokeProcessor<T> invokeProcessor) {
        this(null, scanPaths, invokeProcessor);
    }
    
    public SimpleRouterChannelHandler(String driver, String[] scanPaths, ApiInvokeProcessor<T> invokeProcessor) {
        this.driver = driver;
        this.scanPaths = scanPaths;
        this.invokeProcessor = invokeProcessor;
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HalfDecodeMsg<T> msg) throws Exception {
        Attribute<String> attr =  ctx.channel().attr(AttributeKey.valueOf(Constants.KEY_SESSION_ID));
        ApiSession as = sessionMap.get(attr.get());
        if (msg.getHeader().isReqMsg()) {
            ApiMessage<T> rsp = invokeProcessor.invokeApi(driver, scanPaths, msg, as);
            if (rsp != null) {
                ctx.channel().writeAndFlush(rsp);
            }
        } else {
            String frameId = msg.getHeader().getMsgFrameId();
            SynAssistant<HalfDecodeMsg<T>> assistant = syncInfoMap.get(frameId);
            if (assistant != null) {
                assistant.notifyMsg(msg);
            } else {
                if (invokeProcessor.isApiDef(driver, scanPaths, msg.getHeader())) {
                    invokeProcessor.invokeApi(driver, scanPaths, msg, as);
                } else {
                    logger.warn("receive response message which do not need notify, Api group={} action={}",
                            msg.getHeader().getApiGroup(), msg.getHeader().getApiAction());
                }
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        UUID id = UUID.randomUUID();
        Channel channel = ctx.channel();
        ApiSession vs = new ApiSession(id.toString(), channel);
        Attribute<String> attr =  channel.attr(AttributeKey.valueOf(Constants.KEY_SESSION_ID));
        attr.set(vs.getId());
        sessionMap.put(vs.getId(), vs);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Attribute<String> attr = ctx.channel().attr(AttributeKey.valueOf(Constants.KEY_SESSION_ID));
        String vsId = attr.get();
        sessionMap.remove(vsId);
        super.channelInactive(ctx);
    }
    
    /**
     * 修改会话ID
     * @param vs
     * @param newId
     */
    public void changeSessionId(ApiSession vs, String newId) {
        Assert.notNull(vs, "VirtSession must be non-null");
        String oldId = vs.getId();
        if (StringUtils.equals(oldId, newId)) {
            logger.warn("session id is equal, do nothing, id={}", newId); 
            return;
        }
        vs.setId(newId);
        Attribute<String> attr = vs.getChannel().attr(AttributeKey.valueOf(Constants.KEY_SESSION_ID));
        attr.set(vs.getId());
        sessionMap.put(newId, vs);
        sessionMap.remove(oldId);
    }
    
    /**
     * 查找会话
     * @param id
     * @return
     */
    public ApiSession findSession(String id) {
        Assert.notNull(id, "id must be non-null");
        return sessionMap.get(id);
    }
    
    /**
     * 获取所有会话
     * @return
     */
    public List<ApiSession> getAllSession() {
        return new ArrayList<>(sessionMap.values());
    }
    
    protected RuntimeException timeOutException(ApiMessage<T> req) {
    	return new AnnoRouterException(Constants.ERR_MSG_RESP_TIMEOUT, 
                "apigroup=" + req.obtainHeader().getApiGroup() + " apiAction=" + req.obtainHeader().getApiAction() + " response timeout");
    }
    
    /**
     * 发送消息等待响应
     * @param session
     * @param req
     * @return
     */
    public HalfDecodeMsg<T> synSendMsg(ApiSession session, ApiMessage<T> req) {
        String reqId = req.obtainHeader().getMsgFrameId();
        try {
            SynAssistant<HalfDecodeMsg<T>> assistant = registerReqId(reqId);
            if (session.getChannel().isActive()) {
                session.getChannel().writeAndFlush(req);
                HalfDecodeMsg<T> rsp = assistant.await(timeOut, TimeUnit.SECONDS);
                if (rsp == null) {
                    throw timeOutException(req);
                }
                return rsp;
            } else {
                throw new RuntimeException("session is not ready. Session:" + session.toString());
            }
        } finally {
            unRegisterReqId(reqId);
        }
    }
    
    private SynAssistant<HalfDecodeMsg<T>> registerReqId(String reqFrameId) {
        SynAssistant<HalfDecodeMsg<T>> assister = new SynAssistant<>(reqFrameId);
        syncInfoMap.put(reqFrameId, assister);
        return assister;
    }
    
    private void unRegisterReqId(String reqFrameId) {
        syncInfoMap.remove(reqFrameId);
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String[] getScanPaths() {
		return scanPaths;
	}

	public void setScanPaths(String[] scanPaths) {
		this.scanPaths = scanPaths;
	}
}
