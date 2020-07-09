package com.sujia.rpc.util;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
    private static Properties property = new Properties();
    static {
        try (InputStream in = PropertiesUtil.class
                .getResourceAsStream("config.properties")
        ) {
            property.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return property.getProperty(key);
    }

    public static Integer getInteger(String key) {
        String value = get(key);
        return null == value ? null : Integer.valueOf(value);
    }

    public static Boolean getBoolean(String key) {
        String value = get(key);
        return null == value ? null : Boolean.valueOf(value);
    }

}
