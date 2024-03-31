package org.artomic.netty.route.anno;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

@Retention(RUNTIME)
@Target(TYPE)
@Component
public @interface ApiSpi {
	@AliasFor(annotation = Component.class)
    String value() default "";//component name
    String driver() default "";//driver type
}
