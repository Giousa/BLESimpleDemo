package com.fsrk.ble.service;

public class UUIDBean {
	
	private String uuid;
	private String ch;
	
	
	
	public UUIDBean(String uuid, String ch) {
		super();
		this.uuid = uuid;
		this.ch = ch;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getCh() {
		return ch;
	}
	public void setCh(String ch) {
		this.ch = ch;
	}
	

}
