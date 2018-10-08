package com.huawei.octopus.jobstatusplugin.model;

import com.huawei.octopus.jobstatusplugin.type.EventType;
import hudson.model.Run;
import hudson.model.TaskListener;

public class JobEvent {
    
    private String jobId;

	private EventType eventType;
    
    private Run<?, ?> run;
    
    private TaskListener listener;
    
    private long durationTime;
    
    private long timestamp;
    
    public JobEvent(){
        eventType = EventType.Null;
    }
    
    public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
    
    public JobEvent(EventType eventType, Run<?, ?> run, TaskListener listener) {
        this.eventType = eventType;
        this.run = run;
        this.listener = listener;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Run<?, ?> getRun() {
        return run;
    }

    public void setRun(Run<?, ?> run) {
        this.run = run;
    }

    public TaskListener getListener() {
        return listener;
    }

    public void setListener(TaskListener listener) {
        this.listener = listener;
    }

    public long getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(long durationTime) {
        this.durationTime = durationTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
