package com.huawei.octopus.jobstatusplugin.entity;

public class JobStatusEntityV2 {

    private String job_id;
    
    private String job_name;
    
    private String build_no;
    
    private String status;
    
    private int progress;
    
    private long start_time;
    
    private long duration_time;
    
    private long estimated_time;
    
    private long end_time;
    
    private String event_type;
    
    private String jenkins_url;
    
    private long timestamp;
    
    public JobStatusEntityV2(JobStatusEntity job) {
    	if(job == null) {
    		return;
    	}
    	this.job_id = job.getJobId();
    	this.job_name = job.getJobName();
    	this.build_no = job.getBuildNo();
    	this.status = job.getStatus();
    	this.progress = job.getProgress();
    	this.start_time = job.getStartTime();
    	this.duration_time = job.getDurationTime();
    	this.estimated_time = job.getEstimatedTime();
    	this.end_time = job.getEndTime();
    	this.event_type = job.getEventType();
    	this.jenkins_url = job.getJenkinsUrl();
    	this.timestamp = job.getTimestamp();
    }

	public String getJob_id() {
		return job_id;
	}

	public void setJob_id(String job_id) {
		this.job_id = job_id;
	}

	public String getJob_name() {
		return job_name;
	}

	public void setJob_name(String job_name) {
		this.job_name = job_name;
	}

	public String getBuild_no() {
		return build_no;
	}

	public void setBuild_no(String build_no) {
		this.build_no = build_no;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public long getStart_time() {
		return start_time;
	}

	public void setStart_time(long start_time) {
		this.start_time = start_time;
	}

	public long getDuration_time() {
		return duration_time;
	}

	public void setDuration_time(long duration_time) {
		this.duration_time = duration_time;
	}

	public long getEstimated_time() {
		return estimated_time;
	}

	public void setEstimated_time(long estimated_time) {
		this.estimated_time = estimated_time;
	}

	public long getEnd_time() {
		return end_time;
	}

	public void setEnd_time(long end_time) {
		this.end_time = end_time;
	}

	public String getEvent_type() {
		return event_type;
	}

	public void setEvent_type(String event_type) {
		this.event_type = event_type;
	}

	public String getJenkins_url() {
		return jenkins_url;
	}

	public void setJenkins_url(String jenkins_url) {
		this.jenkins_url = jenkins_url;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
