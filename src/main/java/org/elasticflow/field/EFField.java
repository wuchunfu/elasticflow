package org.elasticflow.field;

import org.elasticflow.writerUnit.handler.Handler;

/**
 * Definition of data fields in data-flow 
 * 
 * @author chengwen
 * @version 1.0
 * @date 2018-11-20 09:48
 */
public class EFField { 
	/**read name*/
	private String name;
	/**write name*/
	private String alias;
	private String defaultvalue;
	private String analyzer="";
	private String stored = "false";
	private String separator;
	/**for data storetype*/
	private String indextype;
	private String indexed = "true";
	private float boost = 1.0f;
	private Handler handler;
	private boolean router=false;
	private String paramtype;
	private String dsl;
	 
	public boolean isValid(String value) { 
		return true;
	}  
	
	public String getAlias() {
		if(this.alias==null){
			this.alias = this.name;
		}
		return this.alias;
	} 

	public void setAlias(String alias) {
		this.alias = alias;
	} 
	
	public void setBoost(float boost) {
		this.boost = boost;
	}   

	public float getBoost() {
		return boost;
	}

	public void setBoost(String boost) {
		this.boost = Float.valueOf(boost);
	} 
	
	public String getParamtype() {
		if(this.paramtype==null){
			this.paramtype = "java.lang.String";
		}
		return this.paramtype;
	} 

	public void setParamtype(String paramtype) {
		this.paramtype = paramtype;
	} 

	
	public String getDefaultvalue() {
		return defaultvalue;
	}

	public void setDefaultvalue(String defaultvalue) {
		this.defaultvalue = defaultvalue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}

	public String getStored() {
		return stored;
	}

	public void setStored(String stored) {
		this.stored = stored;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getIndextype() {
		return indextype;
	}

	public void setIndextype(String indextype) {
		this.indextype = indextype;
	}

	public String getIndexed() {
		return indexed;
	}

	public void setIndexed(String indexed) {
		this.indexed = indexed;
	}

	public Handler getHandler() {
		return this.handler;
	}
	/**
	 * if use dynamic class loader,load class first and then reload instance configs
	 * @param handler
	 * @throws Exception
	 */
	public void setHandler(String handler) throws Exception {
		if(handler!=null && handler.length()>1){
			this.handler = (Handler) Class.forName(handler).newInstance();
		}
	}

	public boolean isRouter() {
		return router;
	}

	public void setRouter(String router) {
		this.router = router.toLowerCase().equals("true")?true:false;
	}

	public String getDsl() {
		return dsl;
	}

	public void setDsl(String dsl) {
		this.dsl = dsl;
	} 
}
