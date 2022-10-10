package com.utt.urouter.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.utt.urouter.util.RouterConstants;

public class Lan {
	private static final String DEFAULT_IP_ADDRESS= "0.0.0.0";
	@NotNull
	@NotEmpty
	private String macAddress;
	@NotNull
	@NotEmpty
	@Pattern(regexp = RouterConstants.IP_ADDRESS_PATTERN)
	private String ipAddress;
	@NotNull
	@NotEmpty
	@Pattern(regexp = RouterConstants.IP_ADDRESS_PATTERN)
	private String subnetmask;
	@NotNull
	@NotEmpty
	private String dhcpServer;
	
	public Lan() {
		this("NA",DEFAULT_IP_ADDRESS,DEFAULT_IP_ADDRESS,"NA");
	}

	public Lan(@NotNull @NotEmpty String macAddress,
			@NotNull @NotEmpty @Pattern(regexp = "^(([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)\\.){3}([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)$") String ipAddress,
			@NotNull @NotEmpty @Pattern(regexp = "^(([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)\\.){3}([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)$") String subnetmask,
			@NotNull @NotEmpty String dhcpServer) {
		super();
		this.macAddress = macAddress;
		this.ipAddress = ipAddress;
		this.subnetmask = subnetmask;
		this.dhcpServer = dhcpServer;
	}


	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getSubnetmask() {
		return subnetmask;
	}

	public void setSubnetmask(String subnetmask) {
		this.subnetmask = subnetmask;
	}

	public String getDhcpServer() {
		return dhcpServer;
	}

	public void setDhcpServer(String dhcpServer) {
		this.dhcpServer = dhcpServer;
	}

	@Override
	public String toString() {
		return "Lan [macAddress=" + macAddress + ", ipAddress=" + ipAddress + ", subnetmask=" + subnetmask
				+ ", dhcpServer=" + dhcpServer + "]";
	}
	
}
