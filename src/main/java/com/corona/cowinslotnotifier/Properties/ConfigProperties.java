package com.corona.cowinslotnotifier.Properties;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties {
    private static Properties properties;
    public static final String DISTRICT_ID = "district_id";
    
    static {
    	try (InputStream input = new FileInputStream("config.properties")) {
    		properties = new Properties();
    		properties.load(input);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    public static String getProperty(String key) {
    	return properties.getProperty(key);
    }
}
