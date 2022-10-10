package com.utt.urouter.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.utt.urouter.util.RouterConstants;

public class ReservationDTO {
	
	@NotEmpty
	private String hostname;
	@NotNull
	@NotEmpty
	@Pattern(regexp = RouterConstants.IP_ADDRESS_PATTERN)
	private String ipAddress;
	@NotNull
	@NotEmpty
	private String macAddress;
	@NotNull
	@NotEmpty
	private String enabled;
	@NotNull
	@NotEmpty
	private String selectedHostName;
	
	public ReservationDTO(@NotEmpty String hostname,
			@NotNull @NotEmpty @Pattern(regexp = "^(([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)\\.){3}([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)$") String ipAddress,
			@NotNull @NotEmpty String macAddress, @NotNull @NotEmpty String enabled,
			@NotNull @NotEmpty String selectedHostName) {
		this.hostname = hostname;
		this.ipAddress = ipAddress;
		this.macAddress = macAddress;
		this.enabled = enabled;
		this.selectedHostName = selectedHostName;
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

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public String getSelectedHostName() {
		return selectedHostName;
	}

	public void setSelectedHostName(String selectedHostName) {
		this.selectedHostName = selectedHostName;
	}
}
