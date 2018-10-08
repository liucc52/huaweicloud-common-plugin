package com.huawei.octopus.jobstatusplugin.service;

import com.huawei.octopus.jobstatusplugin.entity.JobStatusEntity;
import com.huawei.octopus.jobstatusplugin.model.JobEvent;
import com.huawei.octopus.jobstatusplugin.notify.NotifyService;
import com.huawei.octopus.jobstatusplugin.notify.impl.NotifyServiceProxy;
import com.huawei.octopus.jobstatusplugin.type.EventType;
import com.huawei.octopus.jobstatusplugin.util.LogUtil;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EventService {
    
    private static final EventService instance=new EventService();
    
    private static final Logger LOGGER = Logger.getLogger(EventService.class.getName());
    
    private NotifyService notifyService = NotifyServiceProxy.getInstance();
    
    private EventService() {}
    
    public static EventService getInstance() {
        return instance;
    }

    public void handleEvent(JobEvent jobEvent) {
    	String message = String.format("handleEvent start, jobId: %s, eventType: %s", 
				jobEvent.getJobId(),
				jobEvent.getEventType());
    	LogUtil.log(jobEvent.getListener(), LOGGER, message);
    	LOGGER.log(Level.INFO, message);
        EventType eventType = jobEvent.getEventType();
        Run<?, ?> run = jobEvent.getRun();
        if(eventType.equals(EventType.Null)||run==null) {
        	LogUtil.log(jobEvent.getListener(), LOGGER, 
        			String.format("eventType or run is null, jobId: %s", jobEvent.getJobId()));
        	LOGGER.log(Level.SEVERE, String.format("eventType or run is null, jobId: %s", jobEvent.getJobId()));
            return;
        }
        JobStatusEntity jobStatusEntity = new JobStatusEntity();
        jobStatusEntity.setJobId(jobEvent.getJobId());
        jobStatusEntity.setJobName(run.getParent().getName());
        jobStatusEntity.setBuildNo(String.valueOf(run.getNumber()));
        
        Result result = run.getResult();//run.getResult()可能为空
        if(result!=null) {
            jobStatusEntity.setStatus(result.toString());
        }
        else {
            jobStatusEntity.setStatus("IN_PROGRESS");
        }
        
        int progress = 1;
        Executor executor = run.getExecutor();
        if(executor!=null) {
            progress = executor.getProgress();
            if(progress==-1||progress==0) {
                progress = 1;
            }
        }
        jobStatusEntity.setProgress(progress);
        if(EventType.Finalized.equals(jobEvent.getEventType())) {
            jobStatusEntity.setProgress(100);
        }
        
        jobStatusEntity.setStartTime(run.getStartTimeInMillis());
        jobStatusEntity.setDurationTime(jobEvent.getDurationTime());
        jobStatusEntity.setEstimatedTime(run.getEstimatedDuration());
        jobStatusEntity.setEndTime(run.getStartTimeInMillis()+jobEvent.getDurationTime());
        jobStatusEntity.setEventType(String.valueOf(eventType));
        Jenkins jenkins = Jenkins.getInstance();
        if(jenkins != null) {
        	jobStatusEntity.setJenkinsUrl(jenkins.getRootUrl());
        }else {
        	LOGGER.log(Level.SEVERE, String.format("jenkins is null, jobId: %s", jobEvent.getJobId()));
        }
        jobStatusEntity.setTimestamp(jobEvent.getTimestamp());

        LOGGER.log(Level.INFO, String.format("handleEvent sendData, jobId: %s", jobEvent.getJobId()));
        notifyService.sendData(jobStatusEntity, jobEvent);
    }
    
}
