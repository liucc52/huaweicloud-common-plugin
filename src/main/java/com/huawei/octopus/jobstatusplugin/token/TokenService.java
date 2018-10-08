package com.huawei.octopus.jobstatusplugin.token;

import com.huawei.octopus.jobstatusplugin.entity.JobStatusEntity;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

public interface TokenService {
	
	String getToken(JobStatusEntity job, WorkflowRun run);

}
