package com.creants.graph.om;

/**
 * @author LamHM
 *
 */
public class LoginHistory {
	private long id;
	private long userId;
	private long loginTime;
	private int appId;
	private String serverName;
	private String domainName;


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public long getUserId() {
		return userId;
	}


	public void setUserId(long userId) {
		this.userId = userId;
	}


	public long getLoginTime() {
		return loginTime;
	}


	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}


	public int getAppId() {
		return appId;
	}


	public void setAppId(int appId) {
		this.appId = appId;
	}


	public String getServerName() {
		return serverName;
	}


	public void setServerName(String serverName) {
		this.serverName = serverName;
	}


	public String getDomainName() {
		return domainName;
	}


	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

}
