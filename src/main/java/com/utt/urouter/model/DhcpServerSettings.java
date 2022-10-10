package com.utt.urouter.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.utt.urouter.util.RouterConstants;

public class DhcpServerSettings {
	@NotNull
	private Lan lan;
	@NotNull
	private boolean dhcpEnabled;
	private String startEndIp;
	@NotNull
	@NotEmpty
	private String startIp;
	@NotNull
	@NotEmpty
	private String endIp;
	@NotNull
	@NotEmpty
	private String dhcpLeaseTime;
	@NotNull
	@NotEmpty
	@Pattern(regexp = RouterConstants.IP_ADDRESS_PATTERN)
	private String defaultGatewayIp;
	@NotNull
	private boolean useWanDns;
	@NotNull
	private String dnsServer1;
	@NotNull
	private String dnsServer2;
	
	public DhcpServerSettings() {
		this(new Lan(),true,"","0","0","0","",true,"","");
	}
	
	public DhcpServerSettings(@NotNull Lan lan, @NotNull boolean dhcpEnabled, String startEndIp,
			@NotNull @NotEmpty String startIp, @NotNull @NotEmpty String endIp, @NotNull @NotEmpty String dhcpLeaseTime,
			@NotNull @NotEmpty @Pattern(regexp = "^(([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)\\.){3}([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)$") String defaultGatewayIp,
			@NotNull boolean useWanDns, @NotNull String dnsServer1, @NotNull String dnsServer2) {
		super();
		this.lan = lan;
		this.dhcpEnabled = dhcpEnabled;
		this.startEndIp = startEndIp;
		this.startIp = startIp;
		this.endIp = endIp;
		this.dhcpLeaseTime = dhcpLeaseTime;
		this.defaultGatewayIp = defaultGatewayIp;
		this.useWanDns = useWanDns;
		this.dnsServer1 = dnsServer1;
		this.dnsServer2 = dnsServer2;
	}


	public Lan getLan() {
		return lan;
	}

	public void setLan(Lan lan) {
		this.lan = lan;
	}

	public boolean isDhcpEnabled() {
		return dhcpEnabled;
	}
	
	public String getStartEndIp() {
		return startEndIp;
	}

	public void setStartEndIp(String startEndIp) {
		this.startEndIp = startEndIp;
	}

	public String getStartIp() {
		return startIp;
	}

	public String getEndIp() {
		return endIp;
	}

	public String getDhcpLeaseTime() {
		return dhcpLeaseTime;
	}

	public boolean isUseWanDns() {
		return useWanDns;
	}

	public void setUseWanDns(boolean useWanDns) {
		this.useWanDns = useWanDns;
	}

	public String getDefaultGatewayIp() {
		return defaultGatewayIp;
	}

	public String getDnsServer1() {
		return dnsServer1;
	}

	public String getDnsServer2() {
		return dnsServer2;
	}

	public void setDhcpEnabled(boolean dhcpEnabled) {
		this.dhcpEnabled = dhcpEnabled;
	}

	public void setStartIp(String startIp) {
		this.startIp = startIp;
	}

	public void setEndIp(String endIp) {
		this.endIp = endIp;
	}

	public void setDhcpLeaseTime(String dhcpLeaseTime) {
		this.dhcpLeaseTime = dhcpLeaseTime;
	}

	public void setDefaultGatewayIp(String defaultGatewayIp) {
		this.defaultGatewayIp = defaultGatewayIp;
	}

	public void setDnsServer1(String dnsServer1) {
		this.dnsServer1 = dnsServer1;
	}

	public void setDnsServer2(String dnsServer2) {
		this.dnsServer2 = dnsServer2;
	}
	
	@Override
	public String toString() {
		return "DhcpServerSettings [lan=" + lan + ", dhcpEnabled=" + dhcpEnabled + ", startEndIp=" + startEndIp
				+ ", startIp=" + startIp + ", endIp=" + endIp + ", dhcpLeaseTime=" + dhcpLeaseTime
				+ ", defaultGatewayIp=" + defaultGatewayIp + ", userWanDns=" + useWanDns + ", dnsServer1=" + dnsServer1
				+ ", dnsServer2=" + dnsServer2 + "]";
	}
	
}
