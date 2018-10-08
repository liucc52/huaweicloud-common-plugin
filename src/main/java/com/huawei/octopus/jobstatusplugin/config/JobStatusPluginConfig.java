package com.huawei.octopus.jobstatusplugin.config;

import com.huawei.octopus.jobstatusplugin.notify.impl.NotifyServiceProxy;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.Secret;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class JobStatusPluginConfig extends Builder {
    
    @DataBoundConstructor
    public JobStatusPluginConfig() { }
    
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) {

    }
    
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        
        private String notifyMethod;
        
        private String octopusApiGatewayUrl;
        
        private String octopusApiGatewayToken;
        
        private String kafkaServerUrl;
        
        private String jobStatusTopic;
        
        private String iamUrl;
        
        private String encodeUserName;
        
        private String encodePwd;
        
        private String octopusEdgeUrl;
        
        private String ak;
        
        private String sk;
        
        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Get Kafka Server Url";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            notifyMethod = getEncryptedValue(formData, "notifyMethod");
            octopusApiGatewayUrl = getEncryptedValue(formData, "octopusApiGatewayUrl");
            octopusApiGatewayToken = getEncryptedValue(formData, "octopusApiGatewayToken");
            kafkaServerUrl = getEncryptedValue(formData, "kafkaServerUrl");
            jobStatusTopic = getEncryptedValue(formData, "jobStatusTopic");
            iamUrl = getEncryptedValue(formData, "iamUrl");
            encodeUserName = getEncryptedValue(formData, "encodeUserName");
            encodePwd = getEncryptedValue(formData, "encodePwd");
            octopusEdgeUrl = getEncryptedValue(formData, "octopusEdgeUrl");
            ak = getEncryptedValue(formData, "ak");
            sk = getEncryptedValue(formData, "sk");
            save();
            NotifyServiceProxy.getInstance().init();
            return super.configure(req, formData);
        }
        
        private String getEncryptedValue(JSONObject formData, String key) {
        	return Secret.fromString(formData.getString(key)).getEncryptedValue();
        }
        
        public String getNotifyMethod() {
            return Secret.fromString(notifyMethod).getPlainText();
        }
        
        public String getOctopusApiGatewayUrl() {
            return Secret.fromString(octopusApiGatewayUrl).getPlainText();
        }
        
        public String getOctopusApiGatewayToken() {
            return Secret.fromString(octopusApiGatewayToken).getPlainText();
        }
        
        public String getKafkaServerUrl() {
            return Secret.fromString(kafkaServerUrl).getPlainText();
        }
        
        public String getJobStatusTopic() {
            return Secret.fromString(jobStatusTopic).getPlainText();
        }
        
        public String getIamUrl() {
            return Secret.fromString(iamUrl).getPlainText();
        }
        
        public String getEncodeUserName() {
            return Secret.fromString(encodeUserName).getPlainText();
        }
        
        public String getEncodePwd() {
            return Secret.fromString(encodePwd).getPlainText();
        }
        
        public String getOctopusEdgeUrl() {
            return Secret.fromString(octopusEdgeUrl).getPlainText();
        }
        
        public String getAk() {
            return Secret.fromString(ak).getPlainText();
        }
        
        public String getSk() {
        	return Secret.fromString(sk).getPlainText();
        }
    }
}

