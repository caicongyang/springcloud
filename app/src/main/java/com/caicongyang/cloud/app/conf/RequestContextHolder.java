package com.caicongyang.cloud.app.conf;

import java.util.HashMap;
import java.util.Map;

public class RequestContextHolder {
    /**
     * 用于保存线程相关信息
     */
    private transient static ThreadLocal<Map<String, String>> contextMap = new ThreadLocal<Map<String, String>>();


    /**
     * 从 ThreadLocal中获取名值Map(不包含appCode)
     *
     * @return 名值Map
     */
    public static Map<String, String> getContextMap() {
        return contextMap.get();
    }

    /**
     * 从 ThreadLocal 获取名值Map
     *
     * @param contextMap 名值Map
     */
    public static void setContextMap(Map<String, String> contextMap) {
        RequestContextHolder.contextMap.set(contextMap);
    }

    /**
     * （获取键下的值.如果不存在，返回null；如果名值Map未初始化，也返回null） Get the value of key. Would
     * return null if context map hasn't been initialized.
     *
     * @param key 键
     * @return 键下的值
     */
    public static String get(String key) {
        Map<String, String> contextMap = getContextMap();
        if (contextMap == null) {
            return null;
        }

        return contextMap.get(key);
    }

    /**
     * （设置名值对。如果Map之前为null，则会被初始化） Put the key-value into the context map;
     * <p/>
     * Initialize the map if the it doesn't exist.
     *
     * @param key   键
     * @param value 值
     * @return 之前的值
     */
    public static String put(String key, String value) {
        if (key == null || value == null) {
            throw new RuntimeException("key:" + key + " or value:" + value + " is null,i can't put it into the context map");
        }
        Map<String, String> contextMap = getContextMap();
        if (contextMap == null) {
            contextMap = new HashMap<String, String>();
            setContextMap(contextMap);
        }
        return contextMap.put(key, value);
    }

    /**
     * 清空所有线程变量
     */
    public static void clean() {
        contextMap.remove();
    }
}
