package org.artomic.netty.route;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.artomic.netty.route.anno.ApiSpiScan;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

public class ApiSpiRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware{

    private ClassLoader classLoader;
    private ResourceLoader resourceLoader;
    
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(ApiSpiScan.class.getName(), true));
        if (attributes == null) {
            throw new RuntimeException("ApiTaskScan config is error");
        }
        Set<String> packages = new HashSet<>();
        addPackages(packages, attributes.getStringArray("value"));
        String[] scanPaths =  StringUtils.toStringArray(packages);
        ApiSpiScanner scanner = new ApiSpiScanner(registry, classLoader, scanPaths);
        if (this.resourceLoader != null) {
            scanner.setResourceLoader(this.resourceLoader);
        }
        scanner.scan(scanPaths);
    }

    private void addPackages(Set<String> packages, String[] values) {
        if (values != null) {
            Collections.addAll(packages, values);
        }
    }
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
