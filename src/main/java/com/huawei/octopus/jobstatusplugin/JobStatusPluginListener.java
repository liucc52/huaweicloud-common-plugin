package com.huawei.octopus.jobstatusplugin;

import com.huawei.octopus.jobstatusplugin.model.JobEvent;
import com.huawei.octopus.jobstatusplugin.thread.TimerUpdateThread;
import com.huawei.octopus.jobstatusplugin.type.EventType;
import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.List;
import java.util.logging.Logger;

@Extension
public class JobStatusPluginListener extends RunListener<Run<?, ?>> implements
		Describable<JobStatusPluginListener>, ModelObject {
	
	private static final Logger LOGGER = Logger.getLogger(JobStatusPluginListener.class.getName());
    
	@Override
	public void onStarted(Run<?, ?> run, TaskListener listener) {
		listener.getLogger().println("onStarted: " + run);
		
		String jobId = getParameter(run, "jobId");
		if(jobId==null||jobId.isEmpty()) {
			listener.getLogger().println("JobStatusPlugin WARN, jobId is empty");
		    return;
		}
		
		JobEvent jobEvent = new JobEvent(EventType.Started, run, listener);
		jobEvent.setTimestamp(System.currentTimeMillis());
		jobEvent.setJobId(jobId);
		
		long durationTime = run.getDuration();
		if(durationTime<=0) {
		    jobEvent.setDurationTime(System.currentTimeMillis()-run.getStartTimeInMillis());
		}else {
		    jobEvent.setDurationTime(durationTime);
		}
		
		TimerUpdateThread.getInstance().sendNotify(jobEvent);
	}
	
	@Override
	public void onCompleted(Run<?, ?> run, TaskListener listener) {
	    listener.getLogger().println("onCompleted: " + run);//WorkflowRun
	    
	    String jobId = getParameter(run, "jobId");
        if(jobId==null||jobId.isEmpty()) {
            return;
        }
        
        JobEvent jobEvent = new JobEvent(EventType.Completed, run, listener);
        jobEvent.setTimestamp(System.currentTimeMillis());
        jobEvent.setJobId(jobId);
        
        long durationTime = run.getDuration();
        if(durationTime<=0) {
            jobEvent.setDurationTime(System.currentTimeMillis()-run.getStartTimeInMillis());
        }else {
            jobEvent.setDurationTime(durationTime);
        }
        
        TimerUpdateThread.getInstance().sendNotify(jobEvent);
	}

	
    @Override
    public void onFinalized(Run<?, ?> run) {
        String jobId = getParameter(run, "jobId");
        if(jobId==null||jobId.isEmpty()) {
            return;
        }
        
        JobEvent jobEvent = new JobEvent(EventType.Finalized, run, null);
        jobEvent.setTimestamp(System.currentTimeMillis());
        jobEvent.setJobId(jobId);
        
        long durationTime = run.getDuration();
        if(durationTime<=0) {
            jobEvent.setDurationTime(System.currentTimeMillis()-run.getStartTimeInMillis());
        }else {
            jobEvent.setDurationTime(durationTime);
        }
        
        TimerUpdateThread.getInstance().sendNotify(jobEvent);
    }
    
	
    private String getParameter(Run<?, ?> run, String key){
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

	@Override
	public JobStatusPluginListenerDescriptor getDescriptor() {
		return (JobStatusPluginListenerDescriptor) Jenkins.getInstance().getDescriptorOrDie(
				getClass());
	}

	@Extension
	public static final class JobStatusPluginListenerDescriptor extends Descriptor<JobStatusPluginListener> {
		private String adapterUrl;

		public JobStatusPluginListenerDescriptor() {
			load();
		}

		public String getAdapterUrl() {
			return adapterUrl;
		}

		public void setAdapterUrl(String adapterUrl) {
			this.adapterUrl = adapterUrl;
		}

		@Override
		public String getDisplayName() {
			return "JobStatusPluginListener";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json)
				throws FormException {
			adapterUrl = json.getString("adapterUrl");
			save();
			return super.configure(req, json);
		}
	}

	@Override
	public String getDisplayName() {
		return "JobStatusPluginListener";
	}
}
