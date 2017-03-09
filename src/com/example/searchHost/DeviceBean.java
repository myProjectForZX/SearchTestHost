package com.example.searchHost;

public class DeviceBean {
	int id;
	String ip;
	int port;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	@Override
	public String toString() {
		return "DeviceBean [id=" + id + ", ip=" + ip + ", port=" + port + "]";
	}
	
}
