package com.huawei.octopus.jobstatusplugin.notify.impl;

import com.huawei.octopus.jobstatusplugin.config.JobStatusPluginConfig;
import com.huawei.octopus.jobstatusplugin.entity.JobStatusEntity;
import com.huawei.octopus.jobstatusplugin.model.JobEvent;
import com.huawei.octopus.jobstatusplugin.notify.NotifyService;
import com.huawei.octopus.jobstatusplugin.type.NotifyMethod;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NotifyServiceProxy implements NotifyService{
    
    private static final Logger LOGGER = Logger.getLogger(NotifyServiceProxy.class.getName()); 
    
    private static final NotifyServiceProxy instance = new NotifyServiceProxy();
    
    NotifyService proxy;
    
    private String notifyMethod;
    
    private NotifyServiceProxy() {
        initNotifyServiceProxy();
    }
    
    public static NotifyServiceProxy getInstance() {
        return instance;
    }

    @Override
    public void sendData(JobStatusEntity job, JobEvent event) {
        if(proxy == null) {
            LOGGER.log(Level.SEVERE, String.format("proxy is null, notifyMethod: %s, jobId: %s", 
            		notifyMethod, 
            		event.getJobId()));
            return;
        }
        proxy.sendData(job, event);
    }

    @Override
    public void init() {
        initNotifyServiceProxy();
    }
    
    private void initNotifyServiceProxy() {
        notifyMethod = (new JobStatusPluginConfig.DescriptorImpl()).getNotifyMethod();
        if(notifyMethod==null||notifyMethod.isEmpty()) {
            LOGGER.info("notifyMethod is empty, HTTP is default");
            proxy=HttpService.getInstance();
            proxy.init();
            return;
        }
        
        if(NotifyMethod.KAFKA.equalsIgnoreCase(notifyMethod)) {
            proxy=KafkaService.getInstance();
            proxy.init();
            return;
        }
        
        if(NotifyMethod.HTTP.equalsIgnoreCase(notifyMethod)) {
            proxy=HttpService.getInstance();
            proxy.init();
            return;
        }
        
        LOGGER.log(Level.SEVERE, String.format("notifyMethod is unknown, notifyMethod: %s", notifyMethod));
    }
    
}
