package com.huawei.octopus.jobstatusplugin.token.impl;

import com.huawei.octopus.jobstatusplugin.entity.JobStatusEntity;
import com.huawei.octopus.jobstatusplugin.token.TokenService;
import com.huawei.octopus.jobstatusplugin.util.Constants;
import com.huawei.octopus.jobstatusplugin.util.ContextUtil;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DevCloudTokenService
 */
public class CddlTokenService implements TokenService{
	
	private static final Logger LOGGER = Logger.getLogger(CddlTokenService.class.getName());

	@Override
	public String getToken(JobStatusEntity job, WorkflowRun run) {
		try {
			return ContextUtil.getCredentialByID(job.getJobName());
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, String.format("%s getToken error, jobId: %s",Constants.LOG_PREFIX, job.getJobId()), t);
			return null;
		}
	}
	
}
