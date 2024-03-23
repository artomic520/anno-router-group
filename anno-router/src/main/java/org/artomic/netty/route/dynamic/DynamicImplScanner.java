package org.artomic.netty.route.dynamic;

import java.util.Arrays;
import java.util.Set;

import org.artomic.netty.route.dynamic.anno.DynamicApi;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

/**
 * 扫描器
 */
public class DynamicImplScanner extends ClassPathBeanDefinitionScanner {
    private ClassLoader classLoader;
    private Class<?> processClass;

    public DynamicImplScanner(BeanDefinitionRegistry registry, ClassLoader classLoader, Class<?> processorClass) {
        super(registry, false);
        this.classLoader = classLoader;
        this.processClass = processorClass;
        this.addIncludeFilter(new AnnotationTypeFilter(DynamicApi.class));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        if (beanDefinition.getMetadata().isInterface()) {
            try {
                Class<?> target = ClassUtils.forName(beanDefinition.getMetadata().getClassName(), this.classLoader);
                return !target.isAnnotation();
            } catch (Exception e) {
                logger.error("load class exception:", e);
            }
        }

        return false;
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        if (beanDefinitions.isEmpty()) {
            logger.warn("No @DynamicImpl was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {

        for(BeanDefinitionHolder holder : beanDefinitions) {
            GenericBeanDefinition definition = (GenericBeanDefinition)holder.getBeanDefinition();
            logger.debug("Creating DynamicImplBean with name '" + holder.getBeanName() + "' and '" + definition.getBeanClassName() + "' Interface");
//            definition.getConstructorArgumentValues().addGenericArgumentValue(Objects.requireNonNull(definition.getBeanClassName()));
            try {
                definition.getConstructorArgumentValues().addIndexedArgumentValue(0, ClassUtils.forName(definition.getBeanClassName(), classLoader));
                definition.getConstructorArgumentValues().addIndexedArgumentValue(1, processClass);
            } catch (ClassNotFoundException e) {
                logger.error("load class exception:", e);
            }
            
            //设置bean为DynamicImplFactoryBean
            definition.setBeanClass(DynamicImplFactoryBean.class);
        }

    }
}
