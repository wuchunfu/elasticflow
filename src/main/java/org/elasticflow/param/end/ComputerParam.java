/*
 * Copyright ElasticFlow B.V. and/or licensed to ElasticFlow B.V. under one
 * or more contributor license agreements. Licensed under the ElasticFlow License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the ElasticFlow License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticflow.param.end;

import java.util.concurrent.CopyOnWriteArrayList;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author chengwen
 * @version 1.0
 * @date 2018-07-22 09:08
 */
public class ComputerParam {
	
	/**The characteristic fields are separated by ","**/
	private String features;
	private String value;
	private String algorithm;
	private volatile CopyOnWriteArrayList<String> api = new CopyOnWriteArrayList<>();
	/**reader and request fields map*/
	private JSONObject apiRequest = new JSONObject();
	/** api max send data nums per request**/
	private int apiRequestMaxDatas = 30;
	/**writer and response fields map*/
	private JSONObject apiResponse = new JSONObject();
	/**User defined JSON parameters can be used to extend the plugin*/
	private JSONObject customParams = new JSONObject();
	protected String keyField;
	/** value= int or string */
	protected String keyFieldType;
	protected String scanField;
	
	private String preprocessing;
	private String postprocessing;
	private double learn_rate = 0.1;
	private double threshold = 0.001;
	/** model|rest,rest api calculation or load model by python calculation */
	private String computeMode = "rest";
	/** train,test,predict **/
	private String stage = "train";
	private String handler;
	
	
	public String getKeyField() {
		return keyField;
	}

	public void setKeyField(String keyField) {
		this.keyField = keyField;
	}

	public String getScanField() {
		return scanField;
	}

	public void setScanField(String scanField) {
		this.scanField = scanField;
	} 
	
	public String getKeyFieldType() {
		return keyFieldType;
	}

	public void setKeyFieldType(String keyFieldType) {
		this.keyFieldType = keyFieldType;
	}
	
	public String getFeatures() {
		return features;
	}

	public String getValue() {
		return value;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public String getStage() {
		return stage;
	}

	public double getLearn_rate() {
		return learn_rate;
	}

	public double getThreshold() {
		return threshold;
	}

	public String getPreprocessing() {
		return preprocessing;
	}

	public String getPostprocessing() {
		return postprocessing;
	}

	public String getComputeMode() {
		return computeMode;
	}

	public void setFeatures(String features) {
		this.features = features;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public void setLearn_rate(String learn_rate) {
		this.learn_rate = Double.parseDouble(learn_rate);
	}

	public void setThreshold(String threshold) {
		this.threshold = Double.parseDouble(threshold);
	}

	public void setStage(String stage) {
		this.stage = stage.trim().toUpperCase();
	}

	public void setPreprocessing(String preprocessing) {
		this.preprocessing = preprocessing;
	}

	public void setPostprocessing(String postprocessing) {
		this.postprocessing = postprocessing;
	}

	public void setComputeType(String computeMode) {
		this.computeMode = computeMode.toLowerCase();
	}

	public CopyOnWriteArrayList<String> getApi() {
		return api;
	}

	public void setApi(String api) {
		if(this.api.isEmpty()) 
			this.api.clear();
		if(api!=null) {
			for(String url:api.split(","))
				this.api.add(url);
		}		
	}
	
	public int apiRequestMaxDatas() {
		return this.apiRequestMaxDatas;
	}

	public JSONObject getApiRequest() {
		return this.apiRequest;
	}

	public void setApiRequest(String apiRequest) {
		if(apiRequest!=null) {
			this.apiRequest = JSONObject.parseObject(apiRequest);
		}		
	}
	
	public void setApiRequestMaxDatas(String apiRequestMaxDatas) {
		if(apiRequestMaxDatas!=null)
			this.apiRequestMaxDatas = Integer.parseInt(apiRequestMaxDatas);
	}

	public JSONObject getApiResponse() {
		return apiResponse;
	}

	public void setApiResponse(String apiResponse) {
		if(apiResponse!=null) {
			this.apiResponse = JSONObject.parseObject(apiResponse);
		}
	}

	public JSONObject getCustomParams() {
		return customParams;
	}

	public void setCustomParams(String customParams) {
		if(customParams!=null) {
			this.customParams = JSONObject.parseObject(customParams);
		}	
	}
	
	public String getHandler() {
		return handler;
	} 
	
	public void setHandler(String handler) {
		this.handler = handler;
	} 

}
