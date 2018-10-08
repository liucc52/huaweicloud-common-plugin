package com.huawei.octopus.jobstatusplugin.util;

import hudson.model.TaskListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogUtil {
	
	private static final Logger LOGGER = Logger.getLogger(LogUtil.class.getName());
	
	public static void log(TaskListener listener, Logger javaLog, String message) {
		message = String.format("[JobStatusPlugin INFO] %s", message);
		if(listener != null && listener.getLogger() != null) {
			listener.getLogger().println(message);
			return;
		}
		if(javaLog != null) {
			javaLog.log(Level.SEVERE, message);
			return;
		}
		LOGGER.log(Level.SEVERE, message);
	}

}
