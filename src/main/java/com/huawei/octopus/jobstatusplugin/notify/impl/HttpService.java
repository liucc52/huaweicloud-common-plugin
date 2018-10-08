package com.huawei.octopus.jobstatusplugin.notify.impl;

import com.apimgt.sdk.http.HttpMethodName;
import com.google.gson.Gson;
import com.huawei.devcloud.adapter.CustomConfigProperty;
import com.huawei.octopus.jobstatusplugin.config.JobStatusPluginConfig.DescriptorImpl;
import com.huawei.octopus.jobstatusplugin.entity.JobStatusEntity;
import com.huawei.octopus.jobstatusplugin.entity.JobStatusEntityV2;
import com.huawei.octopus.jobstatusplugin.model.JobEvent;
import com.huawei.octopus.jobstatusplugin.notify.NotifyService;
import com.huawei.octopus.jobstatusplugin.token.TokenService;
import com.huawei.octopus.jobstatusplugin.token.impl.CddlTokenService;
import com.huawei.octopus.jobstatusplugin.token.impl.CdpInitTokenService;
import com.huawei.octopus.jobstatusplugin.util.*;
import hudson.ExtensionList;
import hudson.model.Run;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpService implements NotifyService{

    private static final Logger LOGGER = Logger.getLogger(HttpService.class.getName());
    
    private static final HttpService instance = new HttpService();
    
    private DescriptorImpl descriptorImpl = null;
	
    private HttpService() {
        initHttpService();
    }
    
    public static HttpService getInstance() {
        return instance;
    }

    @Override
    public void init() {
        initHttpService();
    }
    
    private void initHttpService() {
        ExtensionList<DescriptorImpl> descriptorImplList = ExtensionList.lookup(DescriptorImpl.class);
        descriptorImpl = descriptorImplList.getDynamic(DescriptorImpl.class.getName());
        if(descriptorImpl == null) {
        	LOGGER.log(Level.SEVERE, "descriptorImpl is null");
        }
    }

    @Override
    public void sendData(JobStatusEntity job, JobEvent event) {
    	LOGGER.log(Level.INFO, String.format("job status plugin begin send data, jobId: %s", job.getJobId()));
    	String ak = descriptorImpl.getAk();
    	String sk = descriptorImpl.getSk();
    	if(ak != null && !ak.isEmpty() && sk != null && !sk.isEmpty()) {
    		akskSend(job, event);
    		return;
    	}
    	LOGGER.log(Level.INFO, String.format("job status plugin not akskSend, jobId: %s", job.getJobId()));
        List<JobStatusEntity> jobs = new ArrayList<JobStatusEntity>();
        jobs.add(job);
        String data = new Gson().toJson(jobs);
        LOGGER.log(Level.INFO, String.format("job status plugin sendData, data: %s, jobId: %s", data, event.getJobId()));
        Run<?, ?> run = event.getRun();
    	String token = getUserToken(job, (WorkflowRun)run);
        if(token == null || token.isEmpty()) {
        	LOGGER.log(Level.SEVERE, String.format("job status plugin token is empty, jobId: %s", job.getJobId()));
            return;
        }
        String octopusApiGatewayUrl = descriptorImpl.getOctopusApiGatewayUrl();
        if(octopusApiGatewayUrl == null || octopusApiGatewayUrl.isEmpty()) {
            LOGGER.log(Level.SEVERE, 
            		String.format("job status plugin octopusApiGatewayUrl is empty, jobId: %s",
            		job.getJobId()));
            return;
        }
        String url = octopusApiGatewayUrl+"/octopus-jobstatus/v1/jobstatus/update";
        Map<String, String> headers = setHeader(event);
        if(headers == null) {
        	LOGGER.log(Level.SEVERE, String.format("%s headers is null, jobId: %s", Constants.LOG_PREFIX, job.getJobId()));
            return;
        }
        headers.put("x-auth-token", token);
        CloseableHttpResponse response=null;
        try {
            response = HttpClientUtil.doPost(url, headers, new StringEntity(data));
        } catch (Throwable t) {
        	LOGGER.log(Level.SEVERE, String.format("%s IOException: %s, jobId: %s", 
    				Constants.LOG_PREFIX,
    				t.toString(),
    				job.getJobId()));
            return;
        }
        
        HttpEntity entity = response.getEntity();
        String res = null;
        try {
            res = EntityUtils.toString(entity);
            LOGGER.log(Level.INFO, String.format("%s responseStr: %s, jobId: %s", 
    				Constants.LOG_PREFIX,
    				res,
    				job.getJobId()));
        } catch (Throwable t) {
        	LOGGER.log(Level.SEVERE, String.format("%s IOException: %s, jobId: %s", 
    				Constants.LOG_PREFIX,
    				t.toString(),
    				job.getJobId()));
        }
    }
    
    private String getUserToken(JobStatusEntity job, WorkflowRun run) {
    	TokenService tokenService = null;
		if(ContextUtil.isCddl(run)) {
			tokenService = new CddlTokenService();
		}else {
			tokenService = new CdpInitTokenService();
		}
		return tokenService.getToken(job, run);
    }
    
    /**
     * use ak sk token
     */
    private void akskSend(JobStatusEntity job, JobEvent event) {
    	LOGGER.log(Level.INFO, String.format("job status plugin akskSend, jobId: %s", job.getJobId()));
    	String octopusEdgeUrl = descriptorImpl.getOctopusEdgeUrl();
    	if(octopusEdgeUrl == null || octopusEdgeUrl.isEmpty()) {
    		LOGGER.log(Level.SEVERE, String.format("%s octopusEdgeUrl is empty, jobId: %s", 
    				Constants.LOG_PREFIX,
    				job.getJobId()));
    		return;
    	}
    	String url = String.format(UrlConstants.UPDATE_JOB_STATUS, octopusEdgeUrl, job.getJobId());
    	LOGGER.log(Level.INFO, String.format("job status plugin akskSend, url: %s, jobId: %s", null, event.getJobId()));
        JobStatusEntityV2 edgeJob = new JobStatusEntityV2(job);
        List<JobStatusEntityV2> edgeJobs = new ArrayList<JobStatusEntityV2>();
        edgeJobs.add(edgeJob);
        String data = new Gson().toJson(edgeJobs);
        LOGGER.log(Level.INFO, String.format("job status plugin akskSend, data: %s, jobId: %s", data, event.getJobId()));
    	String ak = descriptorImpl.getAk();
    	String sk = descriptorImpl.getSk();
    	Map<String, String> headers = null;
    	try {
    		headers = getAkskHeaders(url, data, ak, sk, event);
    	}catch(Throwable t) {
    		LOGGER.log(Level.SEVERE, String.format("%s getAkskHeaders Throwable: %s, jobId: %s", 
    				Constants.LOG_PREFIX,
    				t.toString(),
    				job.getJobId()));
    		return;
    	}
        if(headers == null) {
    		LOGGER.log(Level.SEVERE, String.format("%s headers is null, jobId: %s", 
    				Constants.LOG_PREFIX,
    				job.getJobId()));
    		return;
        }
        LOGGER.log(Level.INFO, String.format("job status plugin akskSend, headers: %s, jobId: %s", null, event.getJobId()));
        CloseableHttpResponse response = null;
        try {
            response = HttpClientUtil.doPost(url, headers, new StringEntity(data));
        } catch (Throwable t) {
        	LOGGER.log(Level.SEVERE, String.format("%s IOException: %s, jobId: %s", 
    				Constants.LOG_PREFIX,
    				t.toString(),
    				job.getJobId()));
            return;
        }
        
        HttpEntity entity = response.getEntity();
        String responseStr = null;
        try {
        	responseStr = EntityUtils.toString(entity);
        	LOGGER.log(Level.INFO, String.format("%s responseStr: %s, jobId: %s", 
    				Constants.LOG_PREFIX,
    				responseStr,
    				job.getJobId()));
        } catch (Throwable t) {
        	LOGGER.log(Level.SEVERE, String.format("%s IOException: %s, jobId: %s", 
    				Constants.LOG_PREFIX,
    				t.toString(),
    				job.getJobId()));
        }
    }
    
    public Map<String, String> getAkskHeaders(String url, String data, String ak, String sk, JobEvent event)
    {
    	Map<String, String> headers = setHeader(event);
    	if(headers == null) {
    		LOGGER.log(Level.SEVERE, String.format("%s getAkskHeaders headers is null, jobId: %s", 
    				Constants.LOG_PREFIX,
    				event.getJobId()));
    		return null;
    	}
    	headers.put("Content-Type","application/json");
    	return AkskUtil.getAkskHeaders(ak, sk, url, HttpMethodName.POST, data, headers);
    }
    
    private Map<String, String> setHeader(JobEvent event) {
    	Map<String, String> headers = new HashMap<String, String>();
    	Run<?, ?> run = event.getRun();
        CustomConfigProperty customConfigProperty = run.getParent().getProperty(CustomConfigProperty.class);
        if(customConfigProperty == null) {
        	LOGGER.log(Level.SEVERE, String.format("job status plugin customConfigProperty is null, jobId: %s", event.getJobId()));
            return null;
        }
        String userId = customConfigProperty.getUserId();
        String userName = customConfigProperty.getUserName();
        String domainId = customConfigProperty.getDomainId();
        String domainName = customConfigProperty.getDomainName();
        if(userId == null || userId.isEmpty()) {
        	LOGGER.log(Level.INFO, String.format("job status plugin userId is empty, jobId: %s", event.getJobId()));
        }else {
        	headers.put("user_id", userId);
        }
        if(userName == null || userName.isEmpty()) {
        	LOGGER.log(Level.INFO, String.format("job status plugin userName is empty, jobId: %s", event.getJobId()));
        }else {
        	headers.put("user", userName);
        }
        if(domainId == null || domainId.isEmpty()) {
        	LOGGER.log(Level.INFO, String.format("job status plugin domainId is empty, jobId: %s", event.getJobId()));
        }else {
        	headers.put("domain_id", domainId);
        }
        if(domainName == null || domainName.isEmpty()) {
        	LOGGER.log(Level.INFO, String.format("job status plugin domainName is empty, jobId: %s", event.getJobId()));
        }else {
        	headers.put("domain", domainName);
        }
        return headers;
	}
    
}
