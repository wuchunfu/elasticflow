/*
 * Copyright ElasticFlow B.V. and/or licensed to ElasticFlow B.V. under one
 * or more contributor license agreements. Licensed under the ElasticFlow License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the ElasticFlow License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticflow.node;

import java.util.HashSet;
import java.util.Map;

import org.elasticflow.config.GlobalParam;
import org.elasticflow.config.GlobalParam.STATUS;
import org.elasticflow.config.InstanceConfig;
import org.elasticflow.param.pipe.InstructionParam;
import org.elasticflow.piper.PipePump;
import org.elasticflow.task.FlowTask;
import org.elasticflow.task.InstructionTask;
import org.elasticflow.task.schedule.JobModel;
import org.elasticflow.util.Common;
import org.elasticflow.util.EFNodeUtil;
import org.elasticflow.yarn.Resource;
import org.quartz.SchedulerException;

/**
 * read to write flow build center
 * @author chengwen
 * @version 2.0 
 */
public class FlowCenter{ 
		
	private String default_cron = "0 PARAM 01 * * ?";
	
	private String not_run_cron = "0 0 0 1 1 ? 2099";
	
	private HashSet<String> cron_exists=new HashSet<String>();
 
	
	public void startInstructionsJob() {
		Map<String, InstructionParam> instructions = Resource.nodeConfig.getInstructions();
		for (Map.Entry<String,InstructionParam> entry : instructions.entrySet()){
			createInstructionScheduleJob(entry.getValue(),InstructionTask.createTask(entry.getKey()));
		}
	}
	
	/**
	 * 
	 * @param instance
	 * @param type
	 * @param asyn control job in master is running in asynchronous or not?
	 * @return
	 */
	public boolean runInstanceNow(String instance,String type,boolean asyn){ 
		InstanceConfig instanceConfig = Resource.nodeConfig.getInstanceConfigs().get(instance); 
		boolean state = true; 
		try {
			if (instanceConfig.openTrans() == false)
				return false;   
			String[] L1seqs = Common.getL1seqs(instanceConfig);  
			for (String L1seq : L1seqs) {
				if (L1seq == null)
					continue;
				
				if(GlobalParam.JOB_TYPE.FULL.name().equals(type.toUpperCase())) {
					if (GlobalParam.TASK_COORDER.checkFlowStatus(instance, L1seq,GlobalParam.JOB_TYPE.FULL,STATUS.Ready))
						state = jobAction(Common.getInstanceId(instance, L1seq), GlobalParam.JOB_TYPE.FULL.name(), "run") && state;
						if(state && !asyn) {
							Thread.sleep(1000);//waiting to start job
							while(GlobalParam.TASK_COORDER.checkFlowStatus(instance, L1seq,GlobalParam.JOB_TYPE.FULL,STATUS.Ready)==false)
								Thread.sleep(1000);
						} 
				}else {
					if (GlobalParam.TASK_COORDER.checkFlowStatus(instance, L1seq,GlobalParam.JOB_TYPE.INCREMENT,STATUS.Ready))
						state = jobAction(Common.getInstanceId(instance, L1seq), GlobalParam.JOB_TYPE.INCREMENT.name(), "run") && state;
						if(state && asyn) {
							Thread.sleep(1000);//waiting to start job
							while(GlobalParam.TASK_COORDER.checkFlowStatus(instance, L1seq,GlobalParam.JOB_TYPE.INCREMENT,STATUS.Ready)==false)
								Thread.sleep(1000);
						} 
				}
				
			}
		} catch (Exception e) {
			Common.LOG.error("runInstanceNow "+instance+" Exception", e);
			return false;
		}

		return state;
	}
 
	public boolean removeInstance(String instance,boolean removeTask,boolean removePipe){
		Map<String, InstanceConfig> configMap = Resource.nodeConfig.getInstanceConfigs();
		boolean state = true;
		if(configMap.containsKey(instance)){
			try{
				InstanceConfig instanceConfig = configMap.get(instance);
				String[] L1seqs = Common.getL1seqs(instanceConfig);
				for (String L1seq : L1seqs) {
					if (L1seq == null)
						continue;  
					if(removeTask && Resource.tasks.containsKey(Common.getInstanceId(instance, L1seq))) {
						Resource.tasks.remove(Common.getInstanceId(instance, L1seq));
						state = removeFlowScheduleJob(Common.getInstanceId(instance, L1seq),instanceConfig) && state;
					} 
					
					if(removePipe) {
						for(GlobalParam.FLOW_TAG tag:GlobalParam.FLOW_TAG.values()) {
							Resource.SOCKET_CENTER.clearPipePump(instance, L1seq, tag.name());
						} 
					}  
				}
			}catch(Exception e){
				Common.LOG.error("remove Instance "+instance+" Exception", e);
				return false;
			} 
		}
		return state;
	}
	
	/**
	 * Add flow pipeline management
	 * @param instanceName
	 * @param instanceConfig
	 * @param needClear
	 * @param createSchedule
	 * @param contextId
	 */
	public void addFlowGovern(String instanceName, InstanceConfig instanceConfig,boolean needClear,boolean createSchedule) { 
		if (instanceConfig.checkStatus()==false || instanceConfig.openTrans() == false)
			return;
		String[] L1seqs = Common.getL1seqs(instanceConfig);  
		synchronized(Resource.tasks) {
			try {
				for (String L1seq : L1seqs) {
					if (L1seq == null)
						continue; 
					if(!Resource.tasks.containsKey(Common.getInstanceId(instanceName, L1seq)) || needClear){
						PipePump pipePump = Resource.SOCKET_CENTER.getPipePump(instanceName, L1seq,needClear,GlobalParam.FLOW_TAG._DEFAULT.name());
						if(EFNodeUtil.isSlave()) {
							String newRunId = GlobalParam.TASK_COORDER.getContextId(instanceName, L1seq,GlobalParam.FLOW_TAG._DEFAULT.name());
							CPU.reIndexContexts(pipePump.getID(), newRunId);
							pipePump.setID(newRunId);						
						}
						Resource.tasks.put(Common.getInstanceId(instanceName, L1seq), FlowTask.createTask(instanceName,
								pipePump, L1seq));
					}  
					if(createSchedule)
						createFlowScheduleJob(Common.getInstanceId(instanceName, L1seq), Resource.tasks.get(Common.getInstanceId(instanceName, L1seq)),
							instanceConfig,needClear);
				}
			} catch (Exception e) {
				Common.LOG.error("Add Flow Govern "+instanceName+" Exception", e);
			}	
		}
		
	}

	public boolean jobAction(String mainName, String type, String actype) {
		String jobname = getJobName(mainName, type);
		boolean state = false;
		switch (actype) {
		case "stop":
			state = Resource.taskJobCenter.stopJob(jobname);
			break;
		case "run":
			state = Resource.taskJobCenter.startNow(jobname);
			break;
		case "resume":
			state = Resource.taskJobCenter.restartJob(jobname);
			break;
		case "remove":
			state = Resource.taskJobCenter.deleteJob(jobname);
			break;
		}
		if(state){
			Common.LOG.info("Success " + actype + " Job " + jobname);
		}else{
			Common.LOG.info("Fail " + actype + " Job " + jobname);
		} 
		return state;
	} 
 
	private boolean removeFlowScheduleJob(String instance,InstanceConfig instanceConfig)throws SchedulerException {
		boolean state = true;
		if (instanceConfig.getPipeParams().getFullCron() != null) { 
			state= jobAction(instance, GlobalParam.JOB_TYPE.FULL.name(), "remove") && state;
		}
		if(instanceConfig.getPipeParams().getFullCron() == null || instanceConfig.getPipeParams().getOptimizeCron()!=null){
			state = jobAction(instance, GlobalParam.JOB_TYPE.OPTIMIZE.name(), "remove") && state;
		}
		if(instanceConfig.getPipeParams().getDeltaCron() != null){
			state = jobAction(instance, GlobalParam.JOB_TYPE.INCREMENT.name(), "remove") && state;
		}
		return state;
	}
	
	private void createInstructionScheduleJob(InstructionParam param, InstructionTask task) {
		JobModel _sj = new JobModel(
				getJobName(param.getId(), GlobalParam.JOB_TYPE.INSTRUCTION.name()), param.getCron(),
				"org.elasticflow.task.InstructionTask", "runInstructions", task); 
		try {
			Resource.taskJobCenter.addJob(_sj); 
		}catch (Exception e) {
			Common.LOG.error("create Instruction Job "+param.getId()+" Exception", e);
		} 
	}

	private void createFlowScheduleJob(String instance, FlowTask task,
			InstanceConfig instanceConfig,boolean needclear)
			throws SchedulerException {
		String fullFun="runFull";
		String incrementFun="runIncrement";
		if(instanceConfig.getPipeParams().isMaster()) {
			fullFun="runMasterFull";
			incrementFun="runMasterIncrement";
		} 
		
		if (instanceConfig.getPipeParams().getFullCron() != null) { 
			if(needclear)
				jobAction(instance, GlobalParam.JOB_TYPE.FULL.name(), "remove"); 
			JobModel _sj = new JobModel(
					getJobName(instance, GlobalParam.JOB_TYPE.FULL.name()), instanceConfig.getPipeParams().getFullCron(),
					"org.elasticflow.task.FlowTask", fullFun, task); 
			Resource.taskJobCenter.addJob(_sj); 
		}else if(instanceConfig.getPipeParams().getReadFrom()!= null && instanceConfig.getPipeParams().getWriteTo()!=null) { 
			instanceConfig.setHasFullJob(false);
			if(needclear)
				jobAction(instance, GlobalParam.JOB_TYPE.FULL.name(), "remove");
			//Add valid full tasks to prevent other program errors
			JobModel _sj = new JobModel(
					getJobName(instance,GlobalParam.JOB_TYPE.FULL.name()), not_run_cron,
					"org.elasticflow.task.FlowTask", fullFun, task); 
			Resource.taskJobCenter.addJob(_sj); 
		}  
		
		if (instanceConfig.getPipeParams().getDeltaCron() != null) { 
			if(needclear)
				jobAction(instance, GlobalParam.JOB_TYPE.INCREMENT.name(), "remove");
			
			String cron = instanceConfig.getPipeParams().getDeltaCron();
			if(this.cron_exists.contains(cron)){
				String[] strs = cron.trim().split(" ");
				strs[0] = String.valueOf((int)(Math.random()*60));
				String _s="";
				for(String s:strs){
					_s+=s+" ";
				}
				cron = _s.trim();
			}else{
				this.cron_exists.add(cron);
			}
			JobModel _sj = new JobModel(
					getJobName(instance, GlobalParam.JOB_TYPE.INCREMENT.name()),
					instanceConfig.getPipeParams().getDeltaCron(), "org.elasticflow.task.FlowTask",
					incrementFun, task); 
			Resource.taskJobCenter.addJob(_sj);
		}else if(instanceConfig.getPipeParams().getReadFrom()!= null && instanceConfig.getPipeParams().getWriteTo()!=null) {
			if(needclear)
				jobAction(instance, GlobalParam.JOB_TYPE.INCREMENT.name(), "remove");
			//Add valid full tasks to prevent other program errors
			JobModel _sj = new JobModel(
					getJobName(instance,GlobalParam.JOB_TYPE.INCREMENT.name()),
					not_run_cron, "org.elasticflow.task.FlowTask",
					incrementFun, task); 
			Resource.taskJobCenter.addJob(_sj);
		}
		
		if(instanceConfig.getPipeParams().getFullCron() == null || instanceConfig.getPipeParams().getOptimizeCron()!=null){
			if(needclear)
				jobAction(instance,GlobalParam.JOB_TYPE.OPTIMIZE.name(), "remove");
		
			String cron = instanceConfig.getPipeParams().getOptimizeCron()==null?default_cron.replace("PARAM",String.valueOf((int)(Math.random()*60))):instanceConfig.getPipeParams().getOptimizeCron();
			instanceConfig.getPipeParams().setOptimizeCron(cron);
			if(instanceConfig.getPipeParams().getInstanceName()==null) {
				createOptimizeJob(instance, task,cron); 
			} 
		}
	}
	
	private void createOptimizeJob(String indexName, FlowTask batch,String cron) throws SchedulerException{
		JobModel _sj = new JobModel(
				getJobName(indexName, GlobalParam.JOB_TYPE.OPTIMIZE.name()),cron,
				"org.elasticflow.manager.Task", "optimizeInstance", batch); 
		Resource.taskJobCenter.addJob(_sj); 
	}

	private String getJobName(String instance, String type) { 
		return instance+"_"+type;
	}  
}
