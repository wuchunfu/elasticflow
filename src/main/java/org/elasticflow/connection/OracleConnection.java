package org.elasticflow.connection;

import java.sql.Connection;
import java.sql.DriverManager;

import org.elasticflow.config.GlobalParam.END_TYPE;
import org.elasticflow.param.pipe.ConnectParams;
import org.elasticflow.param.warehouse.WarehouseParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oracle basic connection establishment management class
 * @author chengwen
 * @version 1.0
 * @date 2018-10-26 09:25
 */
public class OracleConnection extends EFConnectionSocket<Connection> {

	private final static Logger log = LoggerFactory.getLogger("Oracle Socket");

	static {
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (Exception e) {
			log.error("OracleConnection Exception,", e);
		}
	}

	public static EFConnectionSocket<?> getInstance(ConnectParams ConnectParams) {
		EFConnectionSocket<?> o = new OracleConnection();
		o.init(ConnectParams);
		return o;
	}

	@Override
	protected boolean connect(END_TYPE endType) {
		try {
			if (!status()) {
				WarehouseParam wsp = this.connectParams.getWhp();
				this.conn = DriverManager.getConnection(getConnectionUrl(),
						wsp.getUser(),
						wsp.getPassword());
				log.info("build connect to " + getConnectionUrl());
			}
			return true;
		} catch (Exception e) {
			log.error(this.connectParams.getWhp().getHost() + "connect Exception,", e);
			return false;
		}
	}

	@Override
	public boolean free() {
		try {
			this.conn.close();
			this.conn = null;
			this.connectParams = null;
		} catch (Exception e) {
			log.error("free connect Exception,", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean status() {
		try {
			if (this.conn != null && !this.conn.isClosed()) {
				return true;
			}
		} catch (Exception e) {
			log.warn("get status Exception,", e);
		}
		return false;
	}

	private String getConnectionUrl() {
		return "jdbc:oracle:thin:@" + this.connectParams.getWhp().getHost() + ":"
				+ this.connectParams.getWhp().getPort() + "/CURD";
	}

}
