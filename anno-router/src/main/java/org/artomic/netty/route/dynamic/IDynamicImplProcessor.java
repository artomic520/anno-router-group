package org.artomic.netty.route.dynamic;

import java.lang.reflect.Method;

/**
 * 动态接口实现处理器
 */
public interface IDynamicImplProcessor {
    
    /**
     * 创建代理对象前的预处理。
     * 可用来保存接口定义上的各种信息，比如接口注解，方法注解等.
     * 接口定义校验也在此进行
     * @param dynamicImplInterface
     */
    void creatingPreprocess(Class<?> dynamicImplInterface);

    /**
     * 实际接口调用
     * @param dynamicImplInterface 标注有@DynamicImpl的接口
     * @param method 执行的方法
     * @param paras 方法参数
     * @return 方法返回
     */
    Object doMethodInvoke(Class<?> dynamicImplInterface, Method method, Object[] paras);
}
