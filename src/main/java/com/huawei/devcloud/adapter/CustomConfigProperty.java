package com.huawei.devcloud.adapter;

import hudson.Extension;
import hudson.model.Descriptor.FormException;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.Serializable;

public class CustomConfigProperty extends JobProperty<Job<?, ?>> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3847128439382685360L;
	/** 原始配置 */
	private String rawConfig;
	/** 租户ID */
	private String domainId;
	/** 租户名称 */
	private String domainName;
	
	private String token;
	
	private String userId;
	
	private String userName;
	
	private String iamToken;
	
	private String jsonEx;
	
	private String octopusJobName;
	
	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	/** 创建者ID */
	private String creatorId;
	/** 任务ID */
	private String jobId;

	// 任务类型
	private String jobType;

	private String context;

	private String domainTag;

	@DataBoundConstructor
	public CustomConfigProperty(String rawConfig, String domainId, String domainName, String creatorId, String jobId,
			String token, String userId, String userName, String iamToken,
			String jsonEx, String jobType, String context, String domainTag,String octopusJobName) {
		this.rawConfig = rawConfig;
		this.domainId = domainId;
		this.domainName = domainName;
		this.creatorId = creatorId;
		this.jobId = jobId;
		this.token = token;
		this.userId = userId;
		this.userName = userName;
		this.iamToken = iamToken;
		this.jsonEx = jsonEx;
		this.jobType = jobType;
		this.context = context;
		this.domainTag = domainTag;
		this.octopusJobName = octopusJobName;
	}
	
	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getDomainTag() {
		return domainTag;
	}

	public void setDomainTag(String domainTag) {
		this.domainTag = domainTag;
	}

	public String getRawConfig() {
		return rawConfig;
	}

	public void setRawConfig(String rawConfig) {
		this.rawConfig = rawConfig;
	}
	
	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	
	
	public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getIamToken() {
    	return iamToken;
	}

	public void setIamToken(String iamToken) {
		this.iamToken = iamToken;
	}

	@Override
	public JobProperty<?> reconfigure(StaplerRequest req, JSONObject form) throws FormException {
		return this;
	}

	public String getJsonEx() {
		return jsonEx;
	}

	public void setJsonEx(String jsonEx) {
		this.jsonEx = jsonEx;
	}

	public String getOctopusJobName() {
		return octopusJobName;
	}

	public void setOctopusJobName(String octopusJobName) {
		this.octopusJobName = octopusJobName;
	}

	@Extension
	public static class DescriptorImpl extends JobPropertyDescriptor {

		@Override
		public String getDisplayName() {
			return null;
		}
	}
}
