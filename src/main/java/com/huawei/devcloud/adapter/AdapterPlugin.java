package com.huawei.devcloud.adapter;

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.Plugin;
import hudson.model.Api;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.User;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ExportedBean
public class AdapterPlugin extends Plugin {
	final static transient Logger logger = Logger.getLogger(AdapterPlugin.class.getName());

	public void doCreateCredential(StaplerRequest req, StaplerResponse rsp) throws ServletException {
		JSONObject data = req.getSubmittedForm();
		User user = User.get(data.getString("user"), true, null);
		CredentialsStore store = CredentialsProvider.lookupStores(user).iterator().next();
		Credentials credentials = req.bindJSON(Credentials.class, data.getJSONObject("credentials"));
		try {
			store.addCredentials(Domain.global(), credentials);
			user.save();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to add credential", e);
			throw new ServletException("Failed to add credential", e);
		}
		
	}
	
	public void doCreateSysCredentials(StaplerRequest req, StaplerResponse rsp) throws ServletException {
	    SystemCredentialsProvider.UserFacingAction userFacingAction = new SystemCredentialsProvider.UserFacingAction();
	    JSONObject data = req.getSubmittedForm();
        CredentialsStore store = userFacingAction.getStore();
        JSONArray  creds = data.getJSONArray("credentials");
        try {
            boolean needUpdate = false;
            for(Object obj:creds){
                JSONObject credential = (JSONObject)obj;
                
                BasicSSHUserPrivateKey.DirectEntryPrivateKeySource privateKeySource = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(((JSONObject)credential.get("privateKeySource")).getString("privateKey"));
                BasicSSHUserPrivateKey cred = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL, credential.getString("id"), credential.getString("username"), privateKeySource, "", credential.getString("description"));
                
                int index = store.getCredentials(Domain.global()).indexOf(cred);
                if(index != -1){
                    BasicSSHUserPrivateKey oldKey = (BasicSSHUserPrivateKey)store.getCredentials(Domain.global()).get(index);
                    if(cred.getPrivateKey().equals(oldKey.getPrivateKey())){
                        logger.log(Level.SEVERE, "--> key is the sa23me, skip...");
                        continue;
                    }else{
                        logger.log(Level.SEVERE, "--> need up23date...");
                        store.removeCredentials(Domain.global(), cred);
                        store.addCredentials(Domain.global(), cred);
                        needUpdate = true;
                    }
                }else{
                    logger.log(Level.SEVERE, "--> deric23t a23dd...");
                    store.addCredentials(Domain.global(), cred);
                    needUpdate = true;
                }
            }
            if(needUpdate){
                store.save();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to add credential", e);
            throw new ServletException("Failed to add credential", e);
        }
	}
	
	
	public void doCreateCredentials(StaplerRequest req, StaplerResponse rsp) throws ServletException {
		JSONObject data = req.getSubmittedForm();
		User user = User.get(data.getString("user"), true, null);
		CredentialsStore store = CredentialsProvider.lookupStores(user).iterator().next();
		JSONArray  creds = data.getJSONArray("credentials");
		
		
		try {
			boolean needUpdate = false;
			for(Object obj:creds){
			    JSONObject credential = (JSONObject)obj;
				
				BasicSSHUserPrivateKey.DirectEntryPrivateKeySource privateKeySource = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(((JSONObject)credential.get("privateKeySource")).getString("privateKey"));
				BasicSSHUserPrivateKey cred = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL, credential.getString("id"), credential.getString("username"), privateKeySource, "", credential.getString("description"));
				
				int index = store.getCredentials(Domain.global()).indexOf(cred);
				if(index != -1){
					BasicSSHUserPrivateKey oldKey = (BasicSSHUserPrivateKey)store.getCredentials(Domain.global()).get(index);
					if(cred.getPrivateKey().equals(oldKey.getPrivateKey())){
						logger.log(Level.SEVERE, "--> key is the same, skip...");
						continue;
					}else{
						logger.log(Level.SEVERE, "--> need update...");
						store.removeCredentials(Domain.global(), cred);
						store.addCredentials(Domain.global(), cred);
						needUpdate = true;
					}
				}else{
					logger.log(Level.SEVERE, "--> derict add...");
					store.addCredentials(Domain.global(), cred);
					needUpdate = true;
				}
			}
			if(needUpdate){
				user.save();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to add credential", e);
			throw new ServletException("Failed to add credential", e);
		}
	}
	
	public void doRemoveCredential(StaplerRequest req, StaplerResponse rsp) throws ServletException {
		JSONObject data = req.getSubmittedForm();
		String credentialId = data.getString("credentialId");
		if(StringUtils.isBlank(credentialId)){
			throw new ServletException("Empty credential id");
		}
		User user = User.get(data.getString("user"), true, null);
		CredentialsStore store = CredentialsProvider.lookupStores(user).iterator().next();
		List<Credentials> credentialsList = store.getCredentials(Domain.global());
		for(Credentials credentials:credentialsList){
			if(credentialId.equals(credentials.getDescriptor().getId())){
				try {
					store.removeCredentials(Domain.global(), credentials);
					user.save();
					return;
				}catch (IOException e) {
					logger.log(Level.SEVERE, "Failed to remove credential", e);
					throw new ServletException("Failed to remove credential", e);
				}
			}
		}
	}


	public Api getApi() {
		return new Api(this);
	}

	@Exported
	public List<Job> getRunningJobs() {
		List<Job> runningJobs = new LinkedList<Job>();
		for (Item item : Jenkins.getInstance().getItems()) {
			Job job = (Job) item;
			if (job.isBuilding()) {
				runningJobs.add(job);
			}
		}
		return runningJobs;
	}

}
