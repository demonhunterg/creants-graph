package com.creants.graph.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author LamHa
 *
 */
public class Configs {
	private static final Logger LOG = LoggerFactory.getLogger(Configs.class);
	// SQL
	public static String sqlDriver;
	public static String sqlUrl;
	public static String sqlUsername;
	public static String sqlPassword;
	public static int sqlMaxActiveConnections;
	public static int sqlMaxIdleConnections;
	public static int sqlBlockTime;
	public static int sqlWriteMinPoolSize;
	public static int sqlWriteMaxPoolSize;
	public static int sqlMaxSize;
	public static int sqlConnectTimeout;

	// couchbase
	public static String couchbaseHosts;
	public static String couchbaseBucket;
	public static String couchbasePass;
	public static int couchbaseOpTimeout;
	public static int couchbaseConnectionTimeout;

	public static void init() {
		Properties prop = null;
		try (InputStream input = new FileInputStream(new File("configs/config.properties"));) {
			prop = new Properties();
			prop.load(input);
		} catch (Exception e) {
			LOG.error("[ERROR] Init config fail!", e);
			return;
		}

		// sql
		sqlDriver = prop.getProperty("sqlDriver", "com.mysql.jdbc.Driver");
		sqlUrl = prop.getProperty("sqlUrl", "jdbc:mysql://locahost:3306/account");
		sqlUsername = prop.getProperty("sqlUsername", "root");
		sqlPassword = prop.getProperty("sqlPassword", "root");
		sqlMaxActiveConnections = Integer.parseInt(prop.getProperty("sqlMaxActiveConnections", "10"));
		sqlMaxIdleConnections = Integer.parseInt(prop.getProperty("sqlMaxIdleConnections", "10"));
		sqlBlockTime = Integer.parseInt(prop.getProperty("sqlBlockTime", "1000"));
		sqlWriteMinPoolSize = Integer.parseInt(prop.getProperty("sqlWrite.minPoolSize", "5"));
		sqlWriteMaxPoolSize = Integer.parseInt(prop.getProperty("sqlWrite.maxPoolSize", "10"));
		sqlMaxSize = Integer.parseInt(prop.getProperty("sqlWrite.maxSize", "10"));
		sqlConnectTimeout = Integer.parseInt(prop.getProperty("sqlWrite.connectionTimeOut", "3600"));

		// couchbase
		couchbaseHosts = prop.getProperty("cache.hosts", "http://10.8.36.7:8091/pools");
		couchbaseBucket = prop.getProperty("cache.bucket", "default");
		couchbasePass = prop.getProperty("cache.pass", "");
		couchbaseOpTimeout = Integer.parseInt(prop.getProperty("cache.couchbaseOpTimeout", "5000"));
		couchbaseConnectionTimeout = Integer.parseInt(prop.getProperty("cache.couchbaseConnectionTimeout", "5000"));

	}

}
