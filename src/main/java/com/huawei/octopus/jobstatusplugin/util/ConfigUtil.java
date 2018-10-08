package com.huawei.octopus.jobstatusplugin.util;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * 配置文件读取类，支持多个配置文件
 */
public class ConfigUtil
{
    private static final Logger logger = Logger.getLogger(ConfigUtil.class);
    private static String DEFAULT_CONFIG_FILE_PATH = "/config.properties";
    private static Properties proerties = new Properties();
    private static boolean loadStatus = false;
    static{
    	load();
    }
    
    private static void load(){
    	InputStream input = null;
    	try {
			input = ConfigUtil.class.getResourceAsStream(DEFAULT_CONFIG_FILE_PATH);
			if (input == null){
				logger.error("load file failed.");
				return;
			}
			proerties.load(input);
			loadStatus = true;
		} catch (IOException e) {
			logger.error("There are something wrong during load, IOException:" 
					+ e.getMessage());
		}finally{
            IOUtils.closeQuietly(input);
        }
    }
    
    public static String getProperty(String key){
    	if(!loadStatus){
    		load();
    	}
    	return proerties.getProperty(key);
    }
 	
    public static String getPropertyWithDefault(String key,String defaultValue){
    	if(!loadStatus){
    		load();
    	}
    	return proerties.getProperty(key,defaultValue);
    }
}
