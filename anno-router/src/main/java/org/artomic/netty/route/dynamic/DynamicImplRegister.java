package org.artomic.netty.route.dynamic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.artomic.netty.route.dynamic.anno.DynamicImplScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;


/**
 * 动态接口注册器
 */
public class DynamicImplRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware {
    private static final Logger logger = LoggerFactory.getLogger(DynamicImplRegister.class);
    private ClassLoader classLoader;
    private ResourceLoader resourceLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(DynamicImplScan.class.getName(), true));
        Set<String> packages = new HashSet<>();
        if (attributes == null) {
            throw new RuntimeException("DynamicImplScan config is error");
        }
        addPackages(packages, attributes.getStringArray("value"));
        addPackages(packages, attributes.getStringArray("basePackages"));
        addClasses(packages, attributes.getStringArray("basePackageClasses"));
        String className = attributes.getString("processorClass");
        Class<?> processor = null;
        try {
            processor =  ClassUtils.forName(className, classLoader);
        } catch (ClassNotFoundException e) {
            logger.error("", e);
        }
        if (processor == null) {
            throw new RuntimeException("DynamicImplScan processorClassName config must not be empty!");
        }
        
        if (packages.isEmpty()) {
            return;
        }

        DynamicImplScanner scanner = new DynamicImplScanner(registry, this.classLoader, processor);
        if (this.resourceLoader != null) {
            scanner.setResourceLoader(this.resourceLoader);
        }

        scanner.doScan(StringUtils.toStringArray(packages));
    }

    private void addPackages(Set<String> packages, String[] values) {
        if (values != null) {
            Collections.addAll(packages, values);
        }
    }

    private void addClasses(Set<String> packages, String[] values) {
        if (values != null) {
            for(int i = 0; i < values.length; i++) {
                String value = values[i];
                packages.add(ClassUtils.getPackageName(value));
            }
        }
    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
