package org.artomic.netty.demo.utils;

import java.lang.reflect.Type;

import com.fasterxml.jackson.core.type.TypeReference;

public class DefTypeReference<T> extends TypeReference<T> {

    private Type type;
    
    public DefTypeReference(Type type) {
        this.type = type;
    }
    public Type getType() { 
        return type; 
    }
    
}
