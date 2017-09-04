package com.gem.bean;

public class CassandraCredentials {

	private String contactpoint;
	private String port;
	private String db;

	public CassandraCredentials(String contactpoint, String port, String db) {
		this.contactpoint = contactpoint;
		this.port = port;
		this.db = db;
	}

	public String getContactpoint() {
		return contactpoint;
	}

	public void setContactpoint(String contactpoint) {
		this.contactpoint = contactpoint;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}
}
