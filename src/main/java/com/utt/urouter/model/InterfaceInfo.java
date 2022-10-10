package com.utt.urouter.model;

public class InterfaceInfo {
	private final String name;
	private String macAddress;
	private String inetAddress;
	private String inetMask;
	private String inetBcast;
	private int mtu;
	private Boolean up;
	private boolean linkUp;
	private boolean multicast;
	public boolean isMulticast() {
		return multicast;
	}

	public void setMulticast(boolean multicast) {
		this.multicast = multicast;
	}

	public InterfaceInfo(String name) {
		this.name = name;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(String inetAddress) {
		this.inetAddress = inetAddress;
	}

	public String getInetMask() {
		return inetMask;
	}

	public void setInetMask(String inetMask) {
		this.inetMask = inetMask;
	}

	public String getInetBcast() {
		return inetBcast;
	}

	public void setInetBcast(String inetBcast) {
		this.inetBcast = inetBcast;
	}

	public int getMtu() {
		return mtu;
	}

	public void setMtu(int mtu) {
		this.mtu = mtu;
	}

	public Boolean isUp() {
		if (this.up != null) {
			return this.up;
		} else {
			// old code
			boolean ret = false;
			if (this.inetAddress != null && this.inetMask != null) {
				ret = true;
			}
			return ret;
		}
	}

	public void setUp(Boolean up) {
		this.up = up;
	}

	public boolean isLinkUp() {
		return linkUp;
	}

	public void setLinkUp(boolean linkUp) {
		this.linkUp = linkUp;
	}

	public String getName() {
		return name;
	}

	public byte[] getMacAddressBytes() {

		if (this.macAddress == null) {
			return new byte[] { 0, 0, 0, 0, 0, 0 };
		}
		String macAddr = this.macAddress.replace(":", "");
		byte[] mac = new byte[6];
		for (int i = 0; i < 6; i++) {
			mac[i] = (byte) ((Character.digit(macAddr.charAt(i * 2), 16) << 4)
					+ Character.digit(macAddr.charAt(i * 2 + 1), 16));
		}
		return mac;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.name).append(":-> type: ").append(", MAC: ").append(this.macAddress).append(", IP Address: ")
				.append(this.inetAddress).append(", Netmask: ").append(this.inetMask).append(", Broadcast: ")
				.append(this.inetBcast).append(", Peer IP Address: ").append(", MTU: ").append(this.mtu)
				.append(", multicast?: ").append(", up?: ").append(this.up).append(", link up?: ").append(this.linkUp);
		return sb.toString();
	}

}
