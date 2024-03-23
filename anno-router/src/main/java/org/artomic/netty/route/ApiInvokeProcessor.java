package org.artomic.netty.route;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;


public abstract class ApiInvokeProcessor<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiInvokeProcessor.class);
    
    private javax.validation.Validator globalValidator; 
    
    public ApiInvokeProcessor() {
        
    }
    public ApiInvokeProcessor(javax.validation.Validator globalValidator) {
        this.globalValidator = globalValidator;
    }
    
    private Map<String, ApiInvokeAssistant> getByScanPath(String scanPath) {
        return ApiSpiScanner.apiInvokeMap.get(scanPath);
    }
    
    /**
     * API是否有定义
     * @param scanPaths
     * @param apiHeader
     * @return
     */
    public boolean isApiDef(String[]scanPaths, ApiHeader apiHeader) {
        return isApiDef(null, scanPaths, apiHeader);
    }
    
    /**
     * 
     * @param driver
     * @param scanPaths
     * @param apiHeader
     * @return
     */
    public boolean isApiDef(String driver, String[]scanPaths, ApiHeader apiHeader) {
        ApiInvokeAssistant assistant = findAssistant(driver, scanPaths,  apiHeader);
        return assistant != null;
    }
    
    private ApiInvokeAssistant findAssistant(String driver, String[] scanPaths, ApiHeader apiHeader) {
        String strDriver = driver==null?"":driver;
        String keyDriver = ApiSpiScanner.genApiKey(strDriver, apiHeader.getApiGroup(),apiHeader.getApiAction());
        String keyCommon = ApiSpiScanner.genApiKey(null, apiHeader.getApiGroup(), apiHeader.getApiAction());
        ApiInvokeAssistant assister = null;
        for (String scanPath : scanPaths) {
            Map<String, ApiInvokeAssistant> apiMap = getByScanPath(scanPath);
            if (apiMap != null) {
                assister = apiMap.get(keyDriver);
                if (assister == null) {
                    assister = apiMap.get(keyCommon);
                }
                if (assister != null) {
                    break;
                }
            }
        }
        return assister;
    }
    
    /**
     * API调用
     * @param driver 驱动
     * @param scanPaths 扫描路径列表
     * @param req api req
     * @param vs session会话
     * @return ApiMessage: response message
     */
    public ApiMessage<T> invokeApi(String driver, String[] scanPaths, HalfDecodeMsg<T> req, ApiSession vs) {
        //find assister
        ApiInvokeAssistant assister = findAssistant(driver, scanPaths, req.getHeader());
        if (assister == null) {
            logger.error("Api group={} action={} is not defined", req.getHeader().getApiGroup(), req.getHeader().getApiAction());
            return null;
        }
        logger.debug("Receive message from '{}', apiGroup={}  apiAction ={}", 
                vs.getChannel().remoteAddress(), req.getHeader().getApiGroup(), req.getHeader().getApiAction());
        
        ApiMessage<T> rsp = doMethodInvoke(assister, req, vs);
        if (rsp != null) {
            rsp.obtainHeader().setApiGroup(req.getHeader().getApiGroup());
            rsp.obtainHeader().setApiAction(req.getHeader().getApiAction());
            rsp.obtainHeader().setMsgFrameId(req.getHeader().getMsgFrameId());
            rsp.obtainHeader().asRspMsg();
            logger.debug("Send message to '{}', apiGroup={}  apiAction ={}, ApiMessage={}", 
                    vs.getChannel().remoteAddress(), req.getHeader().getApiGroup(), req.getHeader().getApiAction(), rsp);
        }
        
        return rsp;
    }
    
    @SuppressWarnings("unchecked")
    private ApiMessage<T> doMethodInvoke(ApiInvokeAssistant assister, HalfDecodeMsg<T> req, ApiSession as) {
        Object instance = getBean(assister.getProcessorClass());
        boolean isOneWay = assister.isMethodOneway();
        Method method = assister.getMethod();
        Class<?>[] paras = method.getParameterTypes();
        Object[] args = null;
        ApiMessage<T> newReq = null;
        if (paras != null && paras.length > 0) {
            if (paras.length > 2) {
                logger.error("only allowed at most 2 parameters. assister :{}", assister);
                throw new IllegalArgumentException("Only allowed at most 2 parameters!");
            }
            args = new Object[paras.length];
            for (int i = 0; i < paras.length; i++) {
                if (ApiSession.class.isAssignableFrom(paras[i])) {
                    args[i] = as;
                } else if (ApiMessage.class.isAssignableFrom(paras[i])) {
                    newReq = continueDecodeMsg(req, (Class<ApiMessage<T>>)paras[i]);//参数再转换 
                    args[i] = newReq;
                } else {
                    logger.error("Only ApiMessage or ApiSession parameter permited. invalid parameter : {}. assister:{}", paras[i], assister);
                    throw new IllegalArgumentException("Only ApiMessage or ApiSession parameter permited!");
                }
            }
        }
        try {
            if (newReq != null) {
                reqParaValid(newReq);
            }
            return (ApiMessage<T>) method.invoke(instance, args);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            logger.error("Method invoke fail!", e);
        } catch (InvocationTargetException e) {
            Throwable tr = e.getTargetException();
            logger.error("Api Group={}, action={} process InvocationTargetException occor. request :{}", 
                    req.getHeader().getApiGroup(), req.getHeader().getApiAction(), req);
            logger.error("", tr);
            if (!isOneWay) {
                return handleException(tr, req.getHeader(), newReq);
            }
        } catch (Throwable tr) {
            logger.error("Api Group={}, action={} process Throwable occor. request :{}", 
                    req.getHeader().getApiGroup(), req.getHeader().getApiAction(), req);
            logger.error("", tr);
            if (!isOneWay) {
                return handleException(tr, req.getHeader(), newReq);
            }
        }
        return null;
    }
    
    protected ApiMessage<T> handleException (Throwable throwable, ApiHeader header, @Nullable ApiMessage<T> req){
        ApiMessage<T> rsp = doHandleException(throwable, req);
        rsp.obtainHeader().setApiAction(header.getApiAction());
        rsp.obtainHeader().setApiGroup(header.getApiGroup());
        rsp.obtainHeader().setMsgFrameId(req.obtainHeader().getMsgFrameId());
        
        return rsp;
    }
    
    /**
     * 初始请求消息转换为方法参数对象
     * @param req
     * @param methodPara
     * @return
     */
    protected ApiMessage<T> continueDecodeMsg(HalfDecodeMsg<T> req, Class<ApiMessage<T>> methodPara) {
        try {
            ApiMessage<T> message = methodPara.getConstructor().newInstance();
            message.setupHeader(req.getHeader());
            message.decodeBody(req.getUndecodedMsg());
            return message;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            //TODO
            logger.error("", e);
            logger.error("ApiMessage must have a public Constructor. ApiMessage class:{}", methodPara);
            throw new IllegalArgumentException(methodPara.getName() + " must have a public Constructor");
        }
    }
    
    private void reqParaValid(ApiMessage<T> req) {
        //编程式注解校验
        if (globalValidator == null) {
            return;
        }
        Set<ConstraintViolation<ApiMessage<T>>> set = globalValidator.validate(req);
        
        if (!set.isEmpty()) {
            String errMsg  = StringUtils.join(
                    set.stream().map(item -> item.getPropertyPath() + item.getMessage()).iterator(), ";");
            throw new IllegalArgumentException("Invalid para:" + errMsg);
        }
    }
    
    abstract protected Object getBean(Class<?> c);
    
    /**
     * 用于业务异常封装
     * @param throwable
     * @param req
     * @return
     */
    abstract protected ApiMessage<T> doHandleException(Throwable throwable, @Nullable ApiMessage<T> req);
    
}
