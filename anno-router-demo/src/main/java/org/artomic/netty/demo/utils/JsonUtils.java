package org.artomic.netty.demo.utils;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public abstract class JsonUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper mapper = new ObjectMapper(); 
    private static ObjectReader reader ;
    private static ObjectWriter writer;
    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        reader = mapper.reader();
        writer = mapper.writer();
    }

    
    /**
     * 对象转换，用户将解析后的对象再次精细化解析
     * @param <T>
     * @param obj
     * @param c
     * @return
     */
    public static <T> T convertObject(Object obj, Class<T> c) {
        try {
            return mapper.convertValue(obj, c);
        } catch (Exception e) {
            LOGGER.error("convert obj fail. input obj: {}", obj);
            LOGGER.error("exception :", e);
            throw new IllegalArgumentException("json convertObject fail.", e);
        }
    }
    /**
     * 对象转换，用户将解析后的对象再次精细化解析
     * @param <T>
     * @param obj
     * @param c
     * @return
     */   
    public static <T> T convertObject(Object obj, DefTypeReference<T> ref) {
        return mapper.convertValue(obj, ref);
    }
    
    /**
     * jsonstr -> Object
     * @param <T>
     * @param jsonStr
     * @param ref
     * @return
     */
    public static <T> T parse(String jsonStr, DefTypeReference<T> ref) {
        try {
            return reader.createParser(jsonStr).readValueAs(ref);
        } catch (IOException e) {
            LOGGER.error("parse json io exception", e);
            LOGGER.error("input json = {}", jsonStr);
            throw new IllegalArgumentException("json parse fail.", e);
        } catch (Exception e) {
            LOGGER.error("input json = {}", jsonStr);
            throw new IllegalArgumentException("json parse fail.", e);
        }
    }
    /**
     * jsonstr -> Map
     * @param <T>
     * @param jsonStr
     * @param ref
     * @return
     */  
    public static Map<?, ?> parse(String jsonStr) {
        try {
            return reader.createParser(jsonStr).readValueAs(Map.class);
        } catch (IOException e) {
            LOGGER.error("parse json io exception", e);
            LOGGER.error("input json = {}", jsonStr);
            throw new IllegalArgumentException("json parse fail.", e);
        } catch (Exception e) {
            LOGGER.error("input json = {}", jsonStr);
            throw new IllegalArgumentException("json parse fail.", e);
        }
    }
    
    /**
     * 解析为数据。如非数组（"{"开头），转为数组
     * @param jsonStr
     * @return
     */
    public static Map<?, ?>[] parseArray(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr) ) {
            return new Map[] {};
        }
        try {
            if (jsonStr.startsWith("{")) {
                Map<?, ?> map = reader.createParser(jsonStr).readValueAs(Map.class);
                return new Map[] {map};
            } else {
                return reader.createParser(jsonStr).readValueAs(Map[].class);
            }
        } catch (IOException e) {
            LOGGER.error("parse json io exception", e);
            LOGGER.error("input json = {}", jsonStr);
            throw new IllegalArgumentException("json parseArray fail.", e);
        } catch (Exception e) {
            LOGGER.error("input json = {}", jsonStr);
            throw new IllegalArgumentException("json parseArray fail.", e);
        }
    }
    
    /**
     * 转为成压缩格式json
     * @param obj
     * @return
     */
    public static String valueAsString(Object obj) {
        try {
            return writer.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOGGER.error("serialize to json string fail", e);
            LOGGER.error("serialize obj = {}", obj);
            throw new IllegalArgumentException("json valueAsString fail.", e);
        }
    }
    
    /**
     * 输出为优雅格式
     * @param obj
     * @return
     */
    public static String valueAsPrettyStr(Object obj) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOGGER.error("serialize to json string fail", e);
            LOGGER.error("serialize obj = {}", obj);
            throw new IllegalArgumentException("json valueAsPrettyStr fail.", e);
        }
    }
    
    /**
     * 输出未字节数组，使用UTF8编码
     * @param obj
     * @return
     */
    public static byte[] valueAsBytes(Object obj) {
        try {
            return writer.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            LOGGER.error("serialize to json string fail", e);
            LOGGER.error("serialize obj = {}", obj);
            throw new IllegalArgumentException("json valueAsBytes fail.", e);
        }
    }
    
}
