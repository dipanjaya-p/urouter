package com.utt.urouter.model;

public class DhcpClient {
	
	private String hostname;
	private String ipAddress;
	private String macAddress;
	private String expiredTime;
	public DhcpClient() {
		
	}
	public DhcpClient(String hostname, String ipAddress, String macAddress, String expiredTime) {
		this.hostname = hostname;
		this.ipAddress = ipAddress;
		this.macAddress = macAddress;
		this.expiredTime = expiredTime;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getExpiredTime() {
		return expiredTime;
	}

	public void setExpiredTime(String expiredTime) {
		this.expiredTime = expiredTime;
	}

	@Override
	public String toString() {
		return "DhcpClients [hostname=" + hostname + ", ipAddress=" + ipAddress + ", macAddress=" + macAddress
				+ ", expiredTime=" + expiredTime + "]";
	}

}
