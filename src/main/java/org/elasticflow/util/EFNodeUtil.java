/*
 * Copyright ElasticFlow B.V. and/or licensed to ElasticFlow B.V. under one
 * or more contributor license agreements. Licensed under the ElasticFlow License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the ElasticFlow License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticflow.util;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.elasticflow.config.GlobalParam;
import org.elasticflow.config.GlobalParam.NODE_TYPE;
import org.elasticflow.config.InstanceConfig;
import org.elasticflow.model.reader.ScanPosition;
import org.elasticflow.yarn.EFRPCService;
import org.elasticflow.yarn.Resource;
import org.elasticflow.yarn.coord.DiscoveryCoord;
import org.elasticflow.yarn.coord.TaskStateCoord;

/**
 * system running control
 * @author chengwen
 * @version 1.0
 * @date 2019-01-15 11:07
 * @modify 2019-01-15 11:07
 */

public final class EFNodeUtil {

	/**
	 * init node start parameters
	 * 
	 * @param instanceConfig
	 * @throws EFException 
	 */
	public static void loadInstanceDatas(InstanceConfig instanceConfig) throws EFException {
		String instance = instanceConfig.getInstanceID();
		String[] L1seqs = Common.getL1seqs(instanceConfig);
		ScanPosition sp = new ScanPosition(instance, "");
		for (String L1seq : L1seqs) {
			GlobalParam.TASK_COORDER.setFlowStatus(instance, L1seq, GlobalParam.JOB_TYPE.FULL.name(), new AtomicInteger(1));
			GlobalParam.TASK_COORDER.setFlowStatus(instance, L1seq, GlobalParam.JOB_TYPE.INCREMENT.name(), new AtomicInteger(1));
			GlobalParam.TASK_COORDER.setFlowStatus(instance, L1seq, GlobalParam.JOB_TYPE.VIRTUAL.name(), new AtomicInteger(1));			
		}
		try {
			sp.loadInfos(Common.getStoreTaskInfo(instance,false),false);
			sp.loadInfos(Common.getStoreTaskInfo(instance,true),true);
		} catch (Exception e) {
			Common.LOG.error("instance {} load Instance Datas exception.",instance,e);
		}
		GlobalParam.TASK_COORDER.initTaskDatas(instance,sp);
	}

	public static void runShell(String path) {
		Process pc = null;
		try {
			Common.LOG.info("Start Run Script " + path);
			pc = Runtime.getRuntime().exec(path);
			pc.waitFor();
		} catch (InterruptedException e) {
			Common.LOG.warn("progress is killed!");
		} catch (Exception e) {
			Common.LOG.error("restartNode Exception", e);
		} finally {
			if (pc != null) {
				pc.destroy();
			}
		}
	} 
	
	/**
	 * Non distributed default master mode startup
	 * @return
	 */
	public static boolean isMaster() {
		if(GlobalParam.DISTRIBUTE_RUN==false ||(GlobalParam.DISTRIBUTE_RUN==true && GlobalParam.node_type==NODE_TYPE.master)) {
			return true;
		}
		return false;
	}
	
	public static boolean isSlave() {
		if(GlobalParam.DISTRIBUTE_RUN==true && GlobalParam.node_type==NODE_TYPE.slave) {
			return true;
		}
		return false;
	}
	
	/**
	 * init slave Coorder
	 * Communication from slave node to master node
	 */
	public static void initSlaveCoorder() {
		Resource.threadPools.execute(() -> {
			boolean redo = true;
			while (redo) {
				try {
					GlobalParam.TASK_COORDER = EFRPCService.getRemoteProxyObj(TaskStateCoord.class, 
							new InetSocketAddress(GlobalParam.StartConfig.getProperty("master_host"), GlobalParam.MASTER_SYN_PORT));			
					GlobalParam.DISCOVERY_COORDER = EFRPCService.getRemoteProxyObj(DiscoveryCoord.class, 
							new InetSocketAddress(GlobalParam.StartConfig.getProperty("master_host"), GlobalParam.MASTER_SYN_PORT));
					redo = false;
				} catch (Exception e) { 
					GlobalParam.TASK_COORDER = null;
					GlobalParam.DISCOVERY_COORDER = null;
				}
			}
		});
	} 
}
