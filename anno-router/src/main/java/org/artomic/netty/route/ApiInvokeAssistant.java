package org.artomic.netty.route;

import java.lang.reflect.Method;

public class ApiInvokeAssistant {
    
    private String scanPath;//扫描路径
    
    private String dirver;//
    
    private String group;
    
    private String action;
    
    private Method method;
    
    private Class<?> processorClass;//处理器类
    
    public ApiInvokeAssistant() {
        
    }

    public String getScanPath() {
        return scanPath;
    }

    public void setScanPath(String scanPath) {
        this.scanPath = scanPath;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDirver() {
        return dirver;
    }

    public void setDirver(String dirver) {
        this.dirver = dirver;
    }


    public Method getMethod() {
        return method;
    }
    
    public boolean isMethodOneway() {
        return method != null && method.getReturnType().equals(Void.TYPE);
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<?> getProcessorClass() {
        return processorClass;
    }

    public void setProcessorClass(Class<?> processorClass) {
        this.processorClass = processorClass;
    }

}
