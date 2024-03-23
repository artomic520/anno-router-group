package org.artomic.netty.route.dynamic.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.artomic.netty.route.dynamic.DynamicImplRegister;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({DynamicImplRegister.class})
public @interface DynamicImplScan {
    
    /**
     * 扫描包列表
     * @return
     */
    @AliasFor("basePackages")
    String[] value() default {};
    /**
     * 扫描包列表
     * @return
     */
    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * 接口列表
     * @return
     */
    Class<?>[] basePackageClasses() default {};
    
    /**
     * 动态调用的实现类，需实现IDynamicImplProcessor接口
     * @return
     */
    Class<?> processorClass();
    
}

