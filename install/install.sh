#!/bin/bash

######################GLOBAL VARIABLES######################################
# dpkg version
THISVERSION="${deb.package.version}"
# install directory path
INSTALL_DIR=/opt/router
# currect script execution dir path
#DIR="$(cd "$(dirname "$0")" && pwd)"
# install log file
LOG=${1}
#CurrentVersion
INSTALLING_VERSION="${3}"
INSTALLED_VERSION="${4}"
# System Network Interface File
NETWORK_INTERFACE_FILE=/etc/network/interfaces
# DHCP Custom Config Path
DHCP_CONFIG_PATH=/etc/dhcp/config
# DHCP Custom Subnet Config File
DHCP_SUBNET_FILE=${DHCP_CONFIG_PATH}/subnet.conf
# DHCP Reservation Config File
DHCP_RESERVATION_FILE=${DHCP_CONFIG_PATH}/reservation.conf
# DHCP DHCPD Config File
DHCPD_CONFIG_FILE=/etc/dhcp/dhcpd.conf
# IPTABLES COMMAND FILE
RC_FILE=/etc/rc.local
# urouter Config File
APPLICATION_CONFIG_FILE=${INSTALL_DIR}/urouter/config/config.yaml
# application properties file
APPLICATION_PROPERTIES_FILE=${INSTALL_DIR}/urouter/config/application.properties
# DHCP client up hook file
DHCP_CLIENT_HOOK_FILE=/etc/dhcp/dhclient-enter-hooks.d/00urouter_resolve
# ifup hook file
IF_UP_HOOK_FILE=/etc/network/if-up.d/urouter_resolve
#WPA_SUPPLICANT Config file
WPA_SUPPLICANT_CONF=/etc/wpa_supplicant/wpa_supplicant.conf
#HOSTAPD Config File
HOSTAPD_CONF=/etc/hostapd/hostapd.conf
###########################################################################
# get the saved information user given at the time of installation
# shellcheck disable=SC1091
. /usr/share/debconf/confmodule
# get how the urouter application is installing as router or dhcp
db_get urouter/install_type
SELECTED_SERVICE=$RET
# idenetify the wan port of the current system 
WAN_PORT=$(ip route show | grep default | cut -d" " -f5 | xargs | cut -d" " -f1)
NS=$(grep </run/systemd/resolve/resolv.conf nameserver | cut -d" " -f2 | xargs | sed -e 's/ /,/g')
WIFI_PORT=$(/sbin/iw dev | grep Interface | grep -v ap0 | awk '{print $2}')
SUB=${WIFI_PORT}
#MAC_ADDRESS="$(cat /sys/class/net/$WIFI_PORT/address)"
install(){
if [ ! -d ${INSTALL_DIR}/urouter/.data ]; then
  mkdir ${INSTALL_DIR}/urouter/.data
fi
# config WAN and LAN(br0) interfaces
config_interfaces
# config DHCP Server
config_dhcp_server
if [ ! -z "$WIFI_PORT" ]; then
  db_get urouter/ssid
  SSID=$RET
  db_get urouter/ssid_password
  SSID_PWD=$RET
  create_ap
fi
configure "install"
# Copy factory_settings.yaml
sed -i "/WIRELESS:/{!b;n;n;s/enabled:.*/enabled: false/}" ${APPLICATION_CONFIG_FILE}
sed -i "/WIRELESS:/{!b;n;n;n;n;s/ssid:.*/ssid: /}" ${APPLICATION_CONFIG_FILE}
sed -i "/WIRELESS:/{!b;n;n;n;n;n;s/wifiPassword:.*/wifiPassword: /}" ${APPLICATION_CONFIG_FILE}
sed -i "/WIRELESS:/{!b;n;n;n;n;n;n;s/apSsid:.*/apSsid: /}" ${APPLICATION_CONFIG_FILE}
sed -i "/WIRELESS:/{!b;n;n;n;n;n;n;n;s/apPassword:.*/apPassword: /}" ${APPLICATION_CONFIG_FILE}
cp ${APPLICATION_CONFIG_FILE} ${INSTALL_DIR}/urouter/.data/factory_settings.yaml

systemctl enable urouter.service
if [ "$SELECTED_SERVICE" == "DHCP" ]; then
   rm -rf  ${RC_FILE}
   rm -rf  ${DHCP_CLIENT_HOOK_FILE}
   rm -rf  ${IF_UP_HOOK_FILE}
else
    if [ ! -f "${INSTALL_DIR}/urouter/.data/iprules.txt" ]; then
        # TAKE BACKUP of Current IP Rules of the  System
        iptables -L -n -v > /dev/null
        iptables -t nat -L -n -v > /dev/null
        bash -c "iptables-save > ${INSTALL_DIR}/urouter/.data/iprules.txt"
    fi
  bash ${RC_FILE} >>"${LOG}" 2>&1
fi

{
  
  systemctl stop systemd-networkd.socket systemd-networkd networkd-dispatcher systemd-networkd-wait-online NetworkManager
  systemctl disable systemd-networkd.socket systemd-networkd networkd-dispatcher systemd-networkd-wait-online NetworkManager
  systemctl mask systemd-networkd.socket systemd-networkd networkd-dispatcher systemd-networkd-wait-online NetworkManager
  systemctl disable wpa_supplicant
  systemctl disable hostapd 
  rm -rf /usr/share/netplan 
  rm -rf /etc/netplan
  
  if [ -d /etc/cloud ]; then
    echo "network: {config: disabled}" > /etc/cloud/cloud.cfg.d/99-disable-network-config.cfg
  fi
  #if [ -d /etc/NetworkManager ]; then
  # sed -i "s/managed=.*$/managed=true/g" /etc/NetworkManager/NetworkManager.conf
  # touch /etc/NetworkManager/conf.d/10-globally-managed-devices.conf
  #fi
  if [ ! -d /etc/systemd/system/networking.service.d ]; then
        mkdir /etc/systemd/system/networking.service.d
    fi
    {
        echo "[Service]"
        echo "TimeoutStartSec=30sec"
    } > /etc/systemd/system/networking.service.d/override.conf
   systemctl daemon-reload
  #pkill -f "ifup .*br0" ; ifdown --force br0 && ifup br0
  #sleep 6s
  #systemctl start urouter.service
  #systemctl restart isc-dhcp-server.service
} >>"${LOG}" 2>&1
sed -i '/hostapd/,+1 d' ${NETWORK_INTERFACE_FILE}
sed -i '/^#WIFI_START/,/^\#WIFI_END/{/^#WIFI_START/!{/^\#WIFI_END/!d}}' $NETWORK_INTERFACE_FILE

exit 0
}
config_interfaces(){
if [[ -z "$WAN_PORT" ]]; then
      db_get urouter/wan_port
      WAN_PORT=$RET
fi
if [[ -z "$WAN_PORT" || "$SELECTED_SERVICE" == "DHCP" ]]; then
    BRIDGE_PORTS=$(find /sys/class/net -type l -not \( -lname '*ap0*' -or -lname '*virtual*' \)  -printf '%f\n'| grep -v "$WIFI_PORT" | xargs)
else
    BRIDGE_PORTS=$(find /sys/class/net -type l -not \( -lname '*ap0*' -or -lname '*virtual*' \)  -printf '%f\n' | grep -v "$WAN_PORT"  | xargs)
fi
if [ -z "$BRIDGE_PORTS" ]; then
	BRIDGE_PORTS="none"
fi
if [ "$SELECTED_SERVICE" == "ROUTER" ]; then
    WAN_INTERFACE=$(cat <<-EOF
    iface ${WAN_PORT} inet dhcp
EOF
)
else
WAN_PORT="";
fi

if [[ ! -z "$WAN_PORT" && "$WAN_PORT" == *"$SUB"* ]]; then
    WIFI_AUTH=$(cat <<-EOF
	pre-up wpa_supplicant -B -i${WAN_PORT} -c${WPA_SUPPLICANT_CONF}
	post-down killall -q wpa_supplicant
EOF
)
fi
if [[ -z "$WIFI_AUTH" && ! -z "$WIFI_PORT" &&  "$SELECTED_SERVICE" == "ROUTER" ]]; then
	WIFI_ADAPTER="$WIFI_PORT";
	WIFIIFACE=$(cat <<-EOF
   	post-up /usr/sbin/hostapd -B ${HOSTAPD_CONF}
	post-down killall hostapd
EOF
)
fi
    cat <<EOF >${NETWORK_INTERFACE_FILE}
    auto lo br0 ${WAN_PORT} 

    iface lo inet loopback

 
    iface br0 inet static
	 ${WIFIIFACE}
      address 192.168.10.1
      netmask 255.255.255.0
      bridge_ports ${BRIDGE_PORTS}
      bridge_stp off
      bridge_waitport 5
      offload-gro off
      offload-gso off
      offload-tso off
	  
    ${WAN_INTERFACE}

$(if [[ -z "$WIFI_AUTH" ]]; then
    echo ""
else
    echo "#WIFI_START"
fi)
		${WIFI_AUTH} 
$(if [[ -z "$WIFI_AUTH" ]]; then
    echo ""
else
    echo "#WIFI_END"
fi)	
$(if [[ -z "$WIFI_AUTH" ]]; then
    echo "#WIFI_START"
else
    echo ""
fi)
$(if [[ -z "$WIFI_AUTH" ]]; then
    echo "#WIFI_END"
else
    echo ""
fi)	
EOF
sed -i "s/ilist:.*$/ilist: $BRIDGE_PORTS/g" ${APPLICATION_CONFIG_FILE}
sed -i "/WAN:/{!b;n;s/interface:.*/interface: $WAN_PORT/}" ${APPLICATION_CONFIG_FILE}
sed -i "/WIRELESS:/{!b;n;s/interface:.*/interface: $WIFI_PORT/}" ${APPLICATION_CONFIG_FILE}
#if [ ! -f "$WPA_SUPPLICANT_CONF" ]; then
if [[ -z "$WIFI_AUTH" ]]; then
sed -i "/WIRELESS:/{!b;n;n;n;s/wirelessMode:.*/wirelessMode: AP/}" ${APPLICATION_CONFIG_FILE}
else
sed -i "/WIRELESS:/{!b;n;n;n;s/wirelessMode:.*/wirelessMode: Client/}" ${APPLICATION_CONFIG_FILE}
fi
}

config_dhcp_server(){
    mkdir -p ${DHCP_CONFIG_PATH}
    cat <<EOF >${DHCP_SUBNET_FILE}
    subnet 192.168.10.0 netmask 255.255.255.0 {
        option routers 192.168.10.1;
        option broadcast-address 192.168.10.255;
    pool {
        next-server 192.168.10.1;
        range 192.168.10.100 192.168.10.200; 	
     }
    }
EOF
   cat <<EOF >${DHCP_RESERVATION_FILE}
EOF
     if [[ -z "$NS" ]]; then
        NS="1.1.1.1,1.0.0.1"
     fi
   cat <<EOF >${DHCPD_CONFIG_FILE}
   option domain-name "urouter";
   option domain-name-servers $NS;
   default-lease-time 600;
   max-lease-time 7200;
   ddns-update-style none;
   authoritative;
   log-facility local7;
   shared-network br0 {

   include  "${DHCP_SUBNET_FILE}";
   group {

   include  "${DHCP_RESERVATION_FILE}";	

  }
 }
EOF
sed -i "/DHCP:/{!b;n;n;n;n;n;n;s/dnsServers:.*/dnsServers: ${NS},/}" ${APPLICATION_CONFIG_FILE}
# Enable DHCP for bridge interface only
sed -i 's+INTERFACESv4=.*+INTERFACESv4="br0"+g' /etc/default/isc-dhcp-server
#sed -i '/^[ \t]*'"$var"'=/{h;s/=.*/='"$val"'/};${x;/^$/{s//c='"$val"'/;H};x}'
sed -i '/^[ \t]*INTERFACES=/{h;s/=.*/="br0"/};${x;/^$/{s//INTERFACES="br0"/;H};x}' /etc/default/isc-dhcp-server
sed -i "0,/^timeout\s/ s/^timeout\s\+[0-9]\+/timeout 30/" /etc/dhcp/dhclient.conf
}
upgrade(){
	IV="${INSTALLING_VERSION:0:1}"
	IDV="${INSTALLED_VERSION:0:1}"
	if [ "$IDV" -le 1 ]; then
	  if [ "$IV" -gt 1 ]; then
	    # Copy backedup config file to latest config
		mv /tmp/urouter_config ${APPLICATION_CONFIG_FILE}
	    sed -i  '$aWIRELESS:' ${APPLICATION_CONFIG_FILE}
	    sed -i  '$a\  \interface: '$WIFI_PORT'' ${APPLICATION_CONFIG_FILE}
	    sed -i  '$a\  \enabled: false' ${APPLICATION_CONFIG_FILE}
	    sed -i  '$a\  \wirelessMode: AP' ${APPLICATION_CONFIG_FILE}
	    sed -i  '$a\  \ssid: ' ${APPLICATION_CONFIG_FILE}
	    sed -i  '$a\  \wifiPassword: ' ${APPLICATION_CONFIG_FILE}
	    sed -i  '$a\ \ apSsid: ' ${APPLICATION_CONFIG_FILE}
	    sed -i  '$a\ \ apPassword: ' ${APPLICATION_CONFIG_FILE}
	    sed -i  '$a\  \mode: 11bn mixed' ${APPLICATION_CONFIG_FILE}
		sed -i  '$a\ \ authType: Auto' ${APPLICATION_CONFIG_FILE}
		sed -i  '$a\  \encryption: AES' ${APPLICATION_CONFIG_FILE}

	    sed -i "/iface br0 inet static/apost-up /usr/sbin/hostapd -B ${HOSTAPD_CONF} \n     post-down killall hostapd" ${NETWORK_INTERFACE_FILE}
		sed -i  '$a#WIFI_START' ${NETWORK_INTERFACE_FILE}
		sed -i  '$a#WIFI_END' ${NETWORK_INTERFACE_FILE}
		cp ${APPLICATION_CONFIG_FILE} ${INSTALL_DIR}/urouter/.data/factory_settings.yaml
      fi
	fi
	sed -i '/^[ \t]*INTERFACES=/{h;s/=.*/="br0"/};${x;/^$/{s//INTERFACES="br0"/;H};x}' /etc/default/isc-dhcp-server
    sed -i "0,/^timeout\s/ s/^timeout\s\+[0-9]\+/timeout 30/" /etc/dhcp/dhclient.conf
    configure "upgrade"
    systemctl daemon-reload

 
    systemctl start urouter.service
    sleep 5s
    if [ "$SELECTED_SERVICE" == "DHCP" ]; then
        rm -rf  ${RC_FILE}
        rm -rf  ${DHCP_CLIENT_HOOK_FILE}
        rm -rf  ${IF_UP_HOOK_FILE}
    else 
        sleep 10s
        urouter apply >> "${LOG}" 2>&1
        bash ${RC_FILE}
    fi

}
create_ap(){
#cat > /etc/udev/rules.d/70-persistent-net.rules << EOF
#SUBSYSTEM=="ieee80211", ACTION=="add|change", ATTR{macaddress}=="${MAC_ADDRESS}", KERNEL=="phy0", \
#  RUN+="/sbin/iw phy phy0 interface add ap0 type __ap", \
#  RUN+="/bin/ip link set ap0 address ${MAC_ADDRESS}"
#EOF

cat <<EOF >${HOSTAPD_CONF}
channel=11
ssid=
wpa_passphrase=
#wpa_psk=aei12345
#country_code=IN
interface=${WIFI_PORT}
bridge=br0
# Use the 2.4GHz band (I think you can use in ag mode to get the 5GHz band as well, but I have not tested this
# yet)
hw_mode=g
# Accept all MAC addresses
macaddr_acl=0
# Use WPA authentication # 1=wpa, 2=wep, 3=both
auth_algs=1
# Require clients to know the network name
ignore_broadcast_ssid=0
# Use WPA2 
wpa=2
# Use a pre-shared key
wpa_key_mgmt=WPA-PSK
wpa_pairwise=TKIP
rsn_pairwise=CCMP
#driver=nl80211
# I commented out the lines below in my implementation, but I kept them here for reference. Enable WMM
#wmm_enabled=1
# Enable 40MHz channels with 20ns guard interval ht_capab=[HT40][SHORT-GI-20][DSSS_CCK-40]
EOF
sed -i 's/^#DAEMON_CONF=.*$/DAEMON_CONF="\/etc\/hostapd\/hostapd.conf"/' /etc/default/hostapd

echo -e "ctrl_interface=/run/wpa_supplicant
update_config=1

#NETWORK_CONFIG_START
network={
\tssid=\"$SSID\"
\tpsk=\"$SSID_PWD\"
\tproto=RSN
\tkey_mgmt=WPA-PSK
\tpairwise=CCMP
\tauth_alg=OPEN

}
#NETWORK_CONFIG_END
" > ${WPA_SUPPLICANT_CONF}
}
configure(){
    db_get urouter/port
    PORT=$RET
    if [[ -z "$WAN_PORT" ]]; then
      db_get urouter/wan_port
      WAN_PORT=$RET
    fi
    #ARGS="-q -f -u0 -d10 -w -I -r /etc/ifplugd/action.d/urouter_interface"
    sed -i "s+server.port:.*+server.port:$PORT+g" ${APPLICATION_PROPERTIES_FILE}
    sed -i "s+spring.application.type:.*+spring.application.type:${SELECTED_SERVICE,,}+g" ${APPLICATION_PROPERTIES_FILE}
    sed -i "s+spring.application.software.version:.*+spring.application.software.version:${THISVERSION}+g" ${APPLICATION_PROPERTIES_FILE}
    if [ ! -z "$WIFI_PORT" ]; then
     sed -i "s+spring.application.router.wireless:.*+spring.application.router.wireless:true+g" ${APPLICATION_PROPERTIES_FILE}
     else
     sed -i "s+spring.application.router.wireless:.*+spring.application.router.wireless:false+g" ${APPLICATION_PROPERTIES_FILE}
    fi

    sed -i "s/WEB_PORT=.*$/WEB_PORT=$PORT/g" ${RC_FILE}
    sed -i "s/WAN_PORT=.*$/WAN_PORT=$WAN_PORT/g" ${RC_FILE}
    sed -i "s/WAN_PORT=.*$/WAN_PORT=$WAN_PORT/g" ${DHCP_CLIENT_HOOK_FILE}
    sed -i "s/WAN_PORT=.*$/WAN_PORT=$WAN_PORT/g" ${IF_UP_HOOK_FILE}
    sed -i "/WAN:/{!b;n;s/interface:.*/interface: $WAN_PORT/}" ${APPLICATION_CONFIG_FILE}
    if [[ "$1" == "install" ]]; then
     if [[ "${WAN_PORT}" == *"$SUB"* ]]; then
        db_get urouter/ssid
        sed -i "/WIRELESS:/{!b;n;n;n;n;s/ssid:.*/ssid: $RET/}" ${APPLICATION_CONFIG_FILE}
        db_get urouter/ssid_password
        sed -i "/WIRELESS:/{!b;n;n;n;n;n;s/wifiPassword:.*/wifiPassword: $RET/}" ${APPLICATION_CONFIG_FILE}
     fi
    fi

    #sed -i "s+^INTERFACES=.*+INTERFACES=\"$WAN_PORT\"+g" /etc/default/ifplugd
    #sed -i "s/ARGS=.*$/ARGS=\"$ARGS\"/g" /etc/default/ifplugd
	if [[ "${WAN_PORT}" == *"$SUB"* ]]; then
      echo "$WAN_PORT"1 > /etc/netplug/netplugd.conf
	 else
	  echo "$WAN_PORT" > /etc/netplug/netplugd.conf
	fi
    if [ ! -d /etc/systemd/system/networking.service.d ]; then
        mkdir /etc/systemd/system/networking.service.d
    fi
    {
        echo "[Service]"
        echo "TimeoutStartSec=30sec"
    } > /etc/systemd/system/networking.service.d/override.conf
    #systemctl daemon-reload
    #systemctl restart netplug
}
case "$2" in
    install)
        echo "   installing urouter"
        install
        exit 0
        ;;
    upgrade)
        echo "   upgrading urouter"
        upgrade
		exit 0
        ;;
    *)
        echo "   $2 not supported by this script."
        ;;
esac
