package org.artomic.netty.route.anno;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.artomic.netty.route.ApiSpiRegister;
import org.springframework.context.annotation.Import;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({ApiSpiRegister.class})
public @interface ApiSpiScan {

    String[] value() default {};
}
