package org.artomic.netty.route;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.artomic.netty.route.anno.ApiDef;
import org.artomic.netty.route.anno.ApiSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class ApiSpiScanner extends ClassPathBeanDefinitionScanner {
    private static final Logger logger = LoggerFactory.getLogger(ApiSpiScanner.class);

    static Map<String, Map<String, ApiInvokeAssistant>> apiInvokeMap = new HashMap<>();

    private ClassLoader classLoader;
    
    private String[] scanPaths;

    public ApiSpiScanner(BeanDefinitionRegistry registry, ClassLoader classLoader, String[] scanPaths) {
        super(registry, false);
        this.classLoader = classLoader;
        this.scanPaths = scanPaths;
        this.addIncludeFilter(new AnnotationTypeFilter(ApiSpi.class));
    }
    
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        if (beanDefinition.getMetadata().isConcrete()) {
            return true;
        }
        return false;
    }
    
    @Override
	protected void postProcessBeanDefinition(AbstractBeanDefinition beanDefinition, String beanName) {
		super.postProcessBeanDefinition(beanDefinition, beanName);
		Class<?> beanClass = null;
        try {
            beanClass = ClassUtils.forName(beanDefinition.getBeanClassName(), this.classLoader);
        } catch (Exception e) {
            logger.error("", e);
            return;
        }
        
        String scanPath = getScanPath(beanClass);
        ApiSpi apiTask = beanClass.getAnnotation(ApiSpi.class);
        importAssisters(scanPath, beanClass, apiTask);
	}

    private Map<String, ApiInvokeAssistant> getOrNewApiMap(String scanPath) {
        Map<String, ApiInvokeAssistant> map = apiInvokeMap.get(scanPath);
        if (map == null) {
            map = new HashMap<>();
            apiInvokeMap.put(scanPath, map);
        }
        return map;
    }

    private String getScanPath(Class<?> beanClass) {
        String className = beanClass.getName();
        for (String path : scanPaths) {
            if (className.startsWith(path)) {
                return path;
            }
        }
        return beanClass.getPackage().getName();
    }

    private void importAssisters(String scanPath, Class<?> beanClass, ApiSpi apiTask) {

        Map<String, ApiInvokeAssistant> apiMap = getOrNewApiMap(scanPath);

        Method[] methods = beanClass.getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            ApiDef apiDef = method.getAnnotation(ApiDef.class);
            if (apiDef == null || StringUtils.isEmpty(apiDef.action())) {
                continue;
            }
            ApiInvokeAssistant assister = new ApiInvokeAssistant();
            assister.setScanPath(scanPath);
            assister.setAction(apiDef.action());
            assister.setGroup(apiDef.group());
            assister.setDirver(apiTask.driver());
            assister.setProcessorClass(beanClass);
            assister.setMethod(method);
            checkAndPutApiDef(assister, apiMap);
        }
    }

    private void checkAndPutApiDef(ApiInvokeAssistant assister, Map<String, ApiInvokeAssistant> apiMap) {
        String key = genApiKey(assister.getDirver(), assister.getGroup(), assister.getAction());
        if (apiMap.containsKey(key)) {
            logger.error("api Duplicate Definition.api info:{}, api info:{}", apiMap.get(key), assister);
            throw new RuntimeException("api Duplicate Definition key=" + key);
        } else {
            apiMap.put(key, assister);
        }
    }
    
    static String genApiKey(String driver, String group, String action) {
        Assert.notNull(action, "action must ben non-null");
        String key = (driver == null?"":driver) + "_" +  (group == null?"":group) + "_" + action;
        return key;
    }

}
