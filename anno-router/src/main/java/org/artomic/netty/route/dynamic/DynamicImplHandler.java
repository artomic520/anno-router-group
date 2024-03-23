package org.artomic.netty.route.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 实际执行器
 *
 */
public class DynamicImplHandler implements InvocationHandler {
    private IDynamicImplProcessor provider;
    private Class<?> dynamicImplInterface;

    public DynamicImplHandler(IDynamicImplProcessor provider, Class<?> dynamicImplInterface) {
        this.provider = provider;
        this.dynamicImplInterface = dynamicImplInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        if ("toString".equals(method.getName())) {
            return "proxy$" + method.getDeclaringClass();
        } else {
            return provider.doMethodInvoke(dynamicImplInterface, method, args);
        }
    }
}

