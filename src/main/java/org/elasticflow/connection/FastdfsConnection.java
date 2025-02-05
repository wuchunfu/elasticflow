package org.elasticflow.connection;

import java.util.Properties;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.elasticflow.config.GlobalParam.END_TYPE;
import org.elasticflow.param.pipe.ConnectParams;
import org.elasticflow.param.warehouse.WarehouseParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fastdfs basic connection establishment management class
 * @author chengwen
 * @version 1.0
 * @date 2021-10-26 09:25
 */
public class FastdfsConnection extends EFConnectionSocket<StorageClient> { 
	
	private final static Logger log = LoggerFactory.getLogger(EsConnection.class);
	
	public static EFConnectionSocket<?> getInstance(ConnectParams connectParams){
		EFConnectionSocket<?> o = new FastdfsConnection();
		o.init(connectParams);  
		return o;
	}
	
	@Override
	protected boolean connect(END_TYPE endType) {
		WarehouseParam wnp = this.connectParams.getWhp();
		if (wnp.getHost() != null) {
			if (!status()) { 			        	
				Properties props = new Properties();
				props.put(ClientGlobal.PROP_KEY_TRACKER_SERVERS,wnp.getHost());
				try {
					ClientGlobal.initByProperties(props);  
					this.conn = new StorageClient(new TrackerClient().getTrackerServer(), null);
				} catch (Exception e) {
					log.error("Fastdfs connect Exception,", e);
					return false;
				}				
			}
		} else {
			return false;
		}
		return true;
	} 

	@Override
	public boolean status() {
		try {
			if (this.conn!=null && this.conn.isConnected()) {
				return true;
			}
		} catch (Exception e) {
			log.error("get status Exception", e);
		}
		this.conn = null;
		return false;
	}

	@Override
	public boolean free() {
		try {
			if(this.conn!=null)
				this.conn.close();
			this.conn = null;
			this.connectParams = null;
		} catch (Exception e) {
			log.warn("free connect Exception,", e);
			return false;
		}
		return true;
	}

}
