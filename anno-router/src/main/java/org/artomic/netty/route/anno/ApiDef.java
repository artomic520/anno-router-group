package org.artomic.netty.route.anno;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Retention(RUNTIME)
@Target(METHOD)
public @interface ApiDef {

    String value() default "";
    
    String action() default "";
    
    String group() default "";
}
