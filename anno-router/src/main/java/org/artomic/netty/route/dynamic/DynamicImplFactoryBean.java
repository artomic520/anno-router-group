package org.artomic.netty.route.dynamic;

import java.lang.reflect.Proxy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

/**
 * 为接口创建的实现类
 */
public class DynamicImplFactoryBean implements SmartFactoryBean<Object>, ApplicationContextAware {
    private Class<?> dynamicImplInterface;
    private Class<?> processClass;
    private IDynamicImplProcessor provider;

    public DynamicImplFactoryBean(Class<?> dynamicImplInterface, Class<?> processClass) {
        this.dynamicImplInterface = dynamicImplInterface;
        this.processClass = processClass;
        this.checkBean();
    }

    @Override
    public Object getObject() {
        provider.creatingPreprocess(dynamicImplInterface);
        return Proxy.newProxyInstance(this.dynamicImplInterface.getClassLoader(), new Class[]{this.dynamicImplInterface}, 
                new DynamicImplHandler(this.provider, dynamicImplInterface));
    }

    private void checkBean() {
        Assert.isTrue(this.dynamicImplInterface.isInterface(), "@DynamicImpl must be at Interface");
    }
    @Override
    public boolean isSingleton() {
        return true;
    }
    @Override
    public boolean isPrototype() {
        return false;
    }
    @Override
    public Class<?> getObjectType() {
        return this.dynamicImplInterface;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (provider == null) {
            provider = (IDynamicImplProcessor)applicationContext.getBean(processClass);
        }
    }
}

