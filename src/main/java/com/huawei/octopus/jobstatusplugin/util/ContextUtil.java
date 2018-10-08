package com.huawei.octopus.jobstatusplugin.util;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.huawei.devcloud.adapter.CustomConfigProperty;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.security.ACL;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContextUtil {
    
    private static final Logger LOGGER = Logger.getLogger(ContextUtil.class.getName());
    
    public static String getParameter(Run<?, ?> run, String key){
        String value="";
        List<ParametersAction> params = run.getActions(ParametersAction.class);
        if(params != null && params.size() > 0){
            for(ParametersAction param:params){
                ParameterValue pvalue = param.getParameter(key);
                if(pvalue != null){
                    value = (String)pvalue.getValue();
                }
            }
        }
        return value;
    }
    
    public static boolean isCddl(Run<?, ?> run) {
    	LOGGER.info("isCddl start");
    	CustomConfigProperty customConfigProperty = run.getParent().getProperty(CustomConfigProperty.class);
    	LOGGER.info("isCddl get customConfigProperty");
        String rawConfig = customConfigProperty.getRawConfig();
        LOGGER.info("isCddl get rawConfig");
        JsonObject jo = new JsonParser().parse(rawConfig).getAsJsonObject();
        if(jo.has("flow") && jo.has("states") && jo.has("workflow")) {
        	LOGGER.info("isCddl true");
        	return true;
        }else {
        	LOGGER.info("isCddl false");
        	return false;
        }
    }
    
    public static String getRawConfig(WorkflowRun run) {
    	if(run == null) {
    		LOGGER.log(Level.SEVERE, "getRawConfig WorkflowRun is null");
    		return null;
    	}
    	CustomConfigProperty customConfigProperty = run.getParent().getProperty(CustomConfigProperty.class);
    	if(customConfigProperty == null) {
    		LOGGER.log(Level.SEVERE, "customConfigProperty is null");
    		return null;
    	}
    	return customConfigProperty.getRawConfig();
    }
    
    @SuppressWarnings("deprecation")
	public static String getCredentialByID(String jobName) {
    	LOGGER.info(String.format("getCredentialByID, jobName: %s", jobName));
    	List<StandardUsernamePasswordCredentials> list = CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class, ACL.SYSTEM);
    	LOGGER.info(String.format("getCredentialByID, jobName: %s, list size: %s", jobName, list.size()));
    	for(Credentials cre : list){
    		if(cre instanceof UsernamePasswordCredentialsImpl){
    			UsernamePasswordCredentialsImpl up = (UsernamePasswordCredentialsImpl)cre;
    			if(jobName.equalsIgnoreCase(up.getUsername())){
    				LOGGER.info(String.format("getCredentialByID, jobName: %s, return the password", jobName));
    				return up.getPassword().getPlainText();
    			}
    		}
    	}
    	LOGGER.info(String.format("getCredentialByID, jobName: %s, return empty", jobName));
    	return "";
    }
    
}
