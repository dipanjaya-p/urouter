package com.utt.urouter.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class PortForwardingDTO {
	
	@NotNull
	@NotEmpty
	private String name;
	@NotNull
	@NotEmpty
	private String externalPort;
	@NotNull
	@NotEmpty
	private String internalPort;
	@NotNull
	@NotEmpty
	private String protocol;
	@NotNull
	@NotEmpty
	private String toIpAddress;
	@NotNull
	@NotEmpty
	private String enabled;
	@NotNull
	@NotEmpty
	private String selectedAppName;
	
	public PortForwardingDTO(@NotNull @NotEmpty String name, @NotNull @NotEmpty String externalPort,
			@NotNull @NotEmpty String internalPort, @NotNull @NotEmpty String protocol,
			@NotNull @NotEmpty String toIpAddress, @NotNull @NotEmpty String enabled,
			@NotNull @NotEmpty String selectedAppName) {
		super();
		this.name = name;
		this.externalPort = externalPort;
		this.internalPort = internalPort;
		this.protocol = protocol;
		this.toIpAddress = toIpAddress;
		this.enabled = enabled;
		this.selectedAppName = selectedAppName;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getExternalPort() {
		return externalPort;
	}
	public void setExternalPort(String externalPort) {
		this.externalPort = externalPort;
	}
	public String getInternalPort() {
		return internalPort;
	}
	public void setInternalPort(String internalPort) {
		this.internalPort = internalPort;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getToIpAddress() {
		return toIpAddress;
	}
	public void setToIpAddress(String toIpAddress) {
		this.toIpAddress = toIpAddress;
	}
	public String getEnabled() {
		return enabled;
	}
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}
	public String getSelectedAppName() {
		return selectedAppName;
	}
	public void setSelectedAppName(String selectedAppName) {
		this.selectedAppName = selectedAppName;
	}
	
}
