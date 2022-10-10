package com.utt.urouter.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.utt.urouter.util.RouterConstants;

public class Reservation {
	private static final String DEFAULT_IP_ADDRESS= "0.0.0.0";
	@NotNull
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
	
	public Reservation() {
		this("NA",DEFAULT_IP_ADDRESS,"NA","0");
	}

	public Reservation(@NotNull @NotEmpty String hostname,
			@NotNull @NotEmpty @Pattern(regexp = "^(([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)\\.){3}([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)$") String ipAddress,
			@NotNull @NotEmpty String macAddress, @NotNull @NotEmpty String enabled) {
		super();
		this.hostname = hostname;
		this.ipAddress = ipAddress;
		this.macAddress = macAddress;
		this.enabled = enabled;
	}

	public String getHostname() {
		return hostname;
	}

	public String getIpAddress() {
		return ipAddress;
	}
	
	public String isEnabled() {
		return enabled;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
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

	@Override
	public String toString() {
		return "Reservation [hostname=" + hostname + ", ipAddress=" + ipAddress + ", macAddress=" + macAddress
				+ ", enabled=" + enabled + "]";
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation reservation = (Reservation) o;
        return hostname.equals(reservation.getHostname()) &&
                ipAddress.equals(reservation.getIpAddress()) &&
                macAddress.equals(reservation.getMacAddress()) &&
                enabled.equals(reservation.isEnabled());
    }

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
