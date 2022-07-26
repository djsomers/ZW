package com.zincworks.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * MySQL database connection helper
 */
public class MySQLConnection {

	/**
	 * Connect to a MySQL database
	 * 
	 * @param Host String host name or IP address
	 * @param port int port 
	 * @param Database String Database name
	 * @param Username String username
	 * @param Password String password
	 * 
	 * @return Connection MySQL database connection
	 * @throws DatabaseException
	 */
	public final Connection getConnection(final String Host, final int port, final String Database, final String Username, final String Password) throws DbException {
		try {

			final Properties props = new Properties();

			props.setProperty("rewriteBatchedStatements", "true");

			// following is based on the com.mysql.jdbc.configs
			// maxperformance.properties file
			props.setProperty("cachePrepStmts", "true");
			props.setProperty("cacheCallableStmts", "true");
			props.setProperty("cacheServerConfiguration", "true");
			props.setProperty("useLocalSessionState", "true");
			props.setProperty("elideSetAutoCommits", "true");
			props.setProperty("alwaysSendSetIsolation", "false");
			props.setProperty("enableQueryTimeouts", "false");
			
			props.setProperty("tcpKeepAlive","true");
			props.setProperty("MaxActive","20");
			props.setProperty("MaxWait","60000");
			props.setProperty("TestWhileIdle","true,");
			props.setProperty("TimeBetweenEvictionRunsMillis","300000");
			props.setProperty("MinEvictableIdleTimeMillis=","300000");
			props.setProperty("TestOnBorrow","true");
			props.setProperty("ValidationQuery","/* ping */SELECT 1");

			return DriverManager.getConnection(
					new String(new StringBuilder("jdbc:mysql://").append(Host)
							.append(":").append(port).append("/")
							.append(Database).append("?user=").append(Username)
							.append("&password=").append(Password).append("&useUnicode=true&characterEncoding=UTF-8").append("&autoReconnect=true")), props);
			
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}
}
