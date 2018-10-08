package com.huawei.octopus.jobstatusplugin.notify.impl;

import com.google.gson.Gson;
import com.huawei.octopus.jobstatusplugin.config.JobStatusPluginConfig;
import com.huawei.octopus.jobstatusplugin.entity.JobStatusEntity;
import com.huawei.octopus.jobstatusplugin.model.JobEvent;
import com.huawei.octopus.jobstatusplugin.notify.NotifyService;
import com.huawei.octopus.jobstatusplugin.type.MessageTopic;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KafkaService implements NotifyService{  
      
    private static final Logger LOGGER = Logger.getLogger(KafkaService.class.getName()); 
    
    private static final KafkaService instance = new KafkaService();
    
    private static final int PARTITION_NUM = 8;
    
    private Producer<String, String> producer;
    
    private String kafkaServerUrl;
    
    private String jobStatusTopic;
  
    private KafkaService() {
        initKafkaService();
    }
    
    public static KafkaService getInstance() {
        return instance;
    }
  
    /** 
     * 生产者，注意kafka生产者不能够从代码上生成主题，只有在服务器上用命令生成 
     */  
    private void initKafkaService() {  
        closeKafkaProducer();
        
        jobStatusTopic = (new JobStatusPluginConfig.DescriptorImpl()).getJobStatusTopic();
        if(jobStatusTopic==null||jobStatusTopic.isEmpty()) {
            LOGGER.log(Level.SEVERE, "jobStatusTopic is empty");
            jobStatusTopic = MessageTopic.JOB_STATUS;
        }
        kafkaServerUrl = (new JobStatusPluginConfig.DescriptorImpl()).getKafkaServerUrl();
        if(kafkaServerUrl==null||kafkaServerUrl.isEmpty()) {
            return;
        }
        
        Properties props = new Properties();  
        // 指定要连接的 broker  
        props.put("metadata.broker.list", kafkaServerUrl); 
        // serializer.class为消息的序列化类  
        props.put("serializer.class", "kafka.serializer.StringEncoder");   
        // 这个是可选的，指定你的消息将要发送到哪个分区
        props.put("partitioner.class", "kafka.producer.DefaultPartitioner");  
        // 指定是否需要 broker反馈消息已经收到. ACK机制, 消息发送需要kafka服务端确认  
        props.put("request.required.acks", "1");  
        //props.put("num.partitions", kafkaConfig.getNumPartitions());  
        producer = new Producer<String, String>(new ProducerConfig(props)); 
    } 
  

    @Override
    public void sendData(JobStatusEntity job, JobEvent event) { 
    	if(job == null) {
    		return;
    	}
        List<JobStatusEntity> jobs = new ArrayList<JobStatusEntity>();
        jobs.add(job);
        String data = new Gson().toJson(jobs);
        if(producer==null) {
            LOGGER.log(Level.SEVERE, String.format("producer is null, kafkaServerUrl: %s", kafkaServerUrl));
            return;
        }
        LOGGER.info(String.format("send message to Kafka, kafkaServerUrl: %s, jobStatusTopic: %s", kafkaServerUrl, jobStatusTopic));
        int key = new SecureRandom().nextInt(10*PARTITION_NUM);
        KeyedMessage<String, String> msg = new KeyedMessage<String, String>(jobStatusTopic, key + "", data);
        try {
            producer.send(msg);
        }catch(Exception e) {
            LOGGER.log(Level.SEVERE, String.format("kafka producer send failed, kafkaServerUrl: %s, exception: %s", kafkaServerUrl, e.getMessage()));
        }
    }

    private void closeKafkaProducer() { 
        if(producer==null) {
            return;
        }
        try {
            producer.close();
        }catch(Exception e) {
            LOGGER.log(Level.SEVERE, String.format("close kafka producer failed, kafkaServerUrl: %s, exception: %s", kafkaServerUrl, e.getMessage()));
        }
        producer = null;
    }

    @Override
    public void init() {
        initKafkaService();
    } 
    
}  
