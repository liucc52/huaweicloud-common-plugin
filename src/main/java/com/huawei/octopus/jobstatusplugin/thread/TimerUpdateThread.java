package com.huawei.octopus.jobstatusplugin.thread;

import com.huawei.octopus.jobstatusplugin.model.JobEvent;
import com.huawei.octopus.jobstatusplugin.service.EventService;
import com.huawei.octopus.jobstatusplugin.util.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class TimerUpdateThread{
	private static final Logger LOGGER = Logger.getLogger(TimerUpdateThread.class.getName());
	private static TimerUpdateThread sender = new TimerUpdateThread();
	private ExecutorService execService;
	private final int THREADCOUNT =100;
	
    private TimerUpdateThread(){
    	scheduler();
    }
    
    public static TimerUpdateThread getInstance(){
    	return sender;
    }
    
    public void sendNotify(final JobEvent jobEvent){
    	execService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					EventService.getInstance().handleEvent(jobEvent);
				}catch(Exception e) {
					LogUtil.log(jobEvent.getListener(), LOGGER, 
		        			String.format("handleEvent warn, jobId: %s, exception: %s", 
		        					jobEvent.getJobId(),
		        					e.getMessage()));
				}
			}
		});
    }
    
    private void scheduler(){
    	iniExecutors();
    }
    
    private void iniExecutors(){
    	if(execService == null){
    		execService = Executors.newFixedThreadPool(THREADCOUNT);
    	}
    }
    
}
