package com.huawei.octopus.jobstatusplugin.notify;

import com.huawei.octopus.jobstatusplugin.entity.JobStatusEntity;
import com.huawei.octopus.jobstatusplugin.model.JobEvent;

public interface NotifyService {
    
    void sendData(JobStatusEntity job, JobEvent event);
    
    void init();

}
