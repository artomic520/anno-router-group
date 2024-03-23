package org.artomic.netty.demo.server;

import org.artomic.netty.demo.dto.RspMessage;
import org.artomic.netty.route.ApiInvokeProcessor;
import org.artomic.netty.route.ApiMessage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import io.netty.buffer.ByteBuf;

@Service
public class ServerApiInvokeProcessor extends ApiInvokeProcessor<ByteBuf> implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    
    @Override
    protected Object getBean(Class<?> c) {
        return applicationContext.getBean(c);
    }

    @Override
    protected ApiMessage<ByteBuf> doHandleException(Throwable throwable, ApiMessage<ByteBuf> req) {
        RspMessage<?> rsp = new RspMessage<>();
        rsp.getBody().setCode("1099");//only for test
        rsp.getBody().setMsg(throwable.getMessage());
        return rsp;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
