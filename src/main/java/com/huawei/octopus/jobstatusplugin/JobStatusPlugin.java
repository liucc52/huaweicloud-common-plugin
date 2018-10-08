//package com.huawei.octopus.jobstatusplugin;
//
//import hudson.Plugin;
//import hudson.model.Api;
//import hudson.model.Item;
//import hudson.model.Job;
//import jenkins.model.Jenkins;
//import org.kohsuke.stapler.export.Exported;
//import org.kohsuke.stapler.export.ExportedBean;
//
//import java.util.LinkedList;
//import java.util.List;
//
//@ExportedBean
//public class JobStatusPlugin extends Plugin {
//
//	public Api getApi() {
//		return new Api(this);
//	}
//
//	@Exported
//	public List<Job> getRunningJobs() {
//		List<Job> runningJobs = new LinkedList<Job>();
//		for (Item item : Jenkins.getInstance().getItems()) {
//			Job job = (Job) item;
//			if (job.isBuilding()) {
//				runningJobs.add(job);
//			}
//		}
//		return runningJobs;
//	}
//}
