#!/bin/bash

######################GLOBAL VARIABLES######################################

# Custom input chain
#CUSTOM_INPUT=chain-incoming-services
# Custom forward chain
#CUSTOM_FORWARD=chain-forward-services
# Custom nat preroute chain
#CUSTOM_PREROUTING=chain-prerouting-services
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
APPLICATION_CONFIG_FILE=/opt/router/urouter/config/config.yaml
# DHCP client up hook file
DHCP_CLIENT_HOOK_FILE=/etc/dhcp/dhclient-enter-hooks.d/00urouter_resolve
# ifup hook file
IF_UP_HOOK_FILE=/etc/network/if-up.d/urouter_resolve
#WPA_SUPPLICANT Config file
WPA_SUPPLICANT_CONF=/etc/wpa_supplicant/wpa_supplicant.conf
#HOSTAPD Config File
HOSTAPD_CONF=/etc/hostapd/hostapd.conf
DATAFILE=/opt/router/urouter/config/usercred.csv
TEMPFILE=".tempcred.csv"
CREATE="create"
UPDATE="update"
DISABLE="disable"

###########################################################################
WIFI_PORT=$(/sbin/iw dev | grep Interface | grep -v ap0 | awk '{print $2}')
main() {
    param="$1"
    shift
    case "$param" in
    login)
        authenticate "$@"
        ;;
    wan)
        configure_wan "$@"
        ;;
    lan)
        configure_lan "$@"
        ;;
    reservation)
        address_reservation "$@"
        ;;
    services)
        configure_services "$@"
        ;;
    port_forward)
        configure_port_forward "$@"
        ;;
	access_point)
        configure_access_point "$@"
        ;;
	test_connecton)
        test_wireless_connecion "$@"
        ;;
    *) ;;

    esac

    exit 0
}

authenticate_customer(){
    UNAME=$1
    PWORD=$2
    
    ISENABLED=1
    
    if [ ! -f $DATAFILE ]
        then
            echo "Fail" #"data file not exist"
        else
            while IFS=, read -r field1 field2 field3
            do
                name=$field1
                upwd=$field2
                status=$field3
            
            done < $DATAFILE
            if [ "$status" = "DISABLE" ] #account disabled
            then
                ISENABLED=0                
            fi

            if [ $ISENABLED = 1 ]  
            then
                if [ "$UNAME" = "$name" ]
                then
                    if [ "$PWORD" = "$upwd" ]
                    then
                        echo "Success";
                    else
                        echo "Fail";
                    fi
                else
                    echo "Fail";
                fi
            else
                echo "Fail";
            fi
    fi
}


authenticate() {
    USERNAME=$1
    PASSWD=$2
    
    retval=$( authenticate_customer "$1" "$2")
            
    if [ "$retval" = "Success" ]
    then
        echo true:"Customer"
    else
    
	    #id -u "$USERNAME" >/dev/null
	    if id -u "$USERNAME"; then
	        export PASSWD
	        ORIGPASS=$(grep -w "$USERNAME" /etc/shadow | cut -d: -f2)
	        if [[ -z "$ORIGPASS" ]]; then
	            echo false
	        else
	            ALGO=$(echo "$ORIGPASS" | cut -d'$' -f2)
	            export ALGO
	            SALT=$(echo "$ORIGPASS" | cut -d'$' -f3)
	            export SALT
	            GENPASS=$(perl -le 'print crypt("$ENV{PASSWD}","\$$ENV{ALGO}\$$ENV{SALT}\$")')
	            if [[ "$GENPASS" == "$ORIGPASS" ]]; then
	                echo true:"Admin"
	            else
	                echo false
	            fi
	        fi
	    else
	        echo "User $USERNAME is not valid"
	        echo $retval
	    fi
    fi
}
configure_wan() {
    WAN_PORT=$1
    CONNECTION_TYPE=$2
    BRIDGE_PORTS=$(find /sys/class/net -type l -not \( -lname '*ap0*' -or -lname '*virtual*' \)  -printf '%f\n' | grep -v "$WAN_PORT" | xargs)
    ROUTER_IPADDRESS=$(ip -4 addr show br0 | grep -oP '(?<=inet\s)\d+(\.\d+){3}')
    ROUTER_SUBNET_MASK_NUMBER=$(ip addr show br0 | grep 'br0:' -A2 | tail -n1 | awk '{print $2}' | cut -f2 -d'/')
    ROUTER_SUBNET_MASK=$(cdr2mask "$ROUTER_SUBNET_MASK_NUMBER")
    oldstate="$(mktemp)"
    md5sum $RC_FILE >"$oldstate" 2>/dev/null
    if [[ "$CONNECTION_TYPE" == "STATIC" ]]; then
        if [[ -n "$6" && -z "$7" ]]; then
            NS="${6}"
        fi
        if [[ -n "$6" && -n "$7" ]]; then
            NS="${6} ${7}"
        fi
        STATIC=$(
            cat <<-EOF
      address ${3}
      netmask ${4}
      gateway ${5}
      dns-nameservers ${NS}
EOF
        )
    fi
    if [[ "${WAN_PORT}" == *"$WIFI_PORT"* ]]; then
    WIFI_AUTH=$(cat <<-EOF
	pre-up wpa_supplicant -B -i${WAN_PORT} -c${WPA_SUPPLICANT_CONF}
	post-down killall -q wpa_supplicant

EOF
)
    fi
	if [[ -z "$WIFI_AUTH" && ! -z "$WIFI_PORT" ]]; then
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
      address ${ROUTER_IPADDRESS}
      netmask ${ROUTER_SUBNET_MASK}
      bridge_ports ${BRIDGE_PORTS}
      bridge_stp off
      bridge_waitport 5
      offload-gro off
      offload-gso off
      offload-tso off

$(if [[ -z "$WIFI_AUTH" ]]; then
    echo ""
else
    echo "#WIFI_START"
fi)
       iface ${WAN_PORT} inet ${CONNECTION_TYPE,,}
		${WIFI_AUTH} 
		${STATIC}
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


    #sed -i "s/IFACE=.*$/IFACE=\"$WAN_PORT\"/g" /etc/ifplugd/action.d/urouter_interface
    #sed -i "s+^INTERFACES=.*+INTERFACES=\"$WAN_PORT\"+g" /etc/default/ifplugd
    sed -i "s/WAN_PORT=.*$/WAN_PORT=$WAN_PORT/g" ${RC_FILE}
    sed -i "s/ilist:.*$/ilist: $BRIDGE_PORTS/g" ${APPLICATION_CONFIG_FILE}
    sed -i "s/WAN_PORT=.*$/WAN_PORT=$WAN_PORT/g" ${DHCP_CLIENT_HOOK_FILE}
    sed -i "s/WAN_PORT=.*$/WAN_PORT=$WAN_PORT/g" ${IF_UP_HOOK_FILE}
    if [[ -z "$WIFI_AUTH" ]]; then
		sed -i "/WIRELESS:/{!b;n;n;n;s/wirelessMode:.*/wirelessMode: AP/}" ${APPLICATION_CONFIG_FILE}
	else
		sed -i "/WIRELESS:/{!b;n;n;n;s/wirelessMode:.*/wirelessMode: Client/}" ${APPLICATION_CONFIG_FILE}
	fi
    newstate="$(mktemp)"
    md5sum $RC_FILE >"$newstate" 2>/dev/null
    if ! cmp "$oldstate" "$newstate"; then
        bash ${RC_FILE}
        if [[ "${WAN_PORT}" == *"$WIFI_PORT"* ]]; then
	        echo "$WAN_PORT_1" > /etc/netplug/netplugd.conf
	     else
	        echo "$WAN_PORT" > /etc/netplug/netplugd.conf
	     fi
		#echo "$WAN_PORT" > /etc/netplug/netplugd.conf
        systemctl restart netplug
    else
      systemctl restart networking
    fi
    rm "$oldstate"
    rm "$newstate"  
}
cdr2mask() {
    # Number of args to shift, 255..255, first non-255 byte, zeroes
    set -- $((5 - ($1 / 8))) 255 255 255 255 $(((255 << (8 - ($1 % 8))) & 255)) 0 0 0
    # shellcheck disable=SC2015
    [ "$1" -gt 1 ] && shift "$1" || shift
    echo "${1-0}"."${2-0}"."${3-0}"."${4-0}"
}
tonum() {
    if [[ $1 =~ ([[:digit:]]+)\.([[:digit:]]+)\.([[:digit:]]+)\.([[:digit:]]+) ]]; then
        addr=$(((BASH_REMATCH[1] << 24) + (BASH_REMATCH[2] << 16) + (BASH_REMATCH[3] << 8) + BASH_REMATCH[4]))
        # eval "$2=\$addr"
        eval "$2=$addr"
    fi
}
toaddr() {
    b1=$((($1 & 0xFF000000) >> 24))
    b2=$((($1 & 0xFF0000) >> 16))
    b3=$((($1 & 0xFF00) >> 8))
    b4=$(($1 & 0xFF))
    # eval "$2=\$b1.\$b2.\$b3.\$b4"
    eval "$2=$b1.$b2.$b3.$b4"
}
calculate() {
    if [[ $1 =~ ^([0-9\.]+)/([0-9]+)$ ]]; then
        # CIDR notation
        IPADDR=${BASH_REMATCH[1]}
        NETMASKLEN=${BASH_REMATCH[2]}
        zeros=$((32 - NETMASKLEN))
        NETMASKNUM=0
        for ((i = 0; i < zeros; i++)); do
            NETMASKNUM=$(((NETMASKNUM << 1) ^ 1))
        done
        NETMASKNUM=$((NETMASKNUM ^ 0xFFFFFFFF))
        toaddr $NETMASKNUM NETMASK
    else
        IPADDR=${1:-192.168.10.1}
        NETMASK=${2:-255.255.255.0}
    fi

    tonum "$IPADDR" IPADDRNUM
    tonum "$NETMASK" NETMASKNUM

    #printf "IPADDRNUM: %x\n" $IPADDRNUM
    #printf "NETMASKNUM: %x\n" $NETMASKNUM

    # The logic to calculate network and broadcast
    INVNETMASKNUM=$((0xFFFFFFFF ^ NETMASKNUM))
    NETWORKNUM=$((IPADDRNUM & NETMASKNUM))
    BROADCASTNUM=$((INVNETMASKNUM | NETWORKNUM))

    #IPADDRBIN=$(   python -c "print(bin(${IPADDRNUM}   )[2:].zfill(32))")
    #NETMASKBIN=$(  python -c "print(bin(${NETMASKNUM}  )[2:].zfill(32))")
    #NETWORKBIN=$(  python -c "print(bin(${NETWORKNUM}  )[2:].zfill(32))")
    #BROADCASTBIN=$(python -c "print(bin(${BROADCASTNUM})[2:].zfill(32))")

    toaddr $NETWORKNUM NETWORK
    toaddr $BROADCASTNUM BROADCAST

    #printf "%-25s %s\n" "IPADDR=$IPADDR"       #$IPADDRBIN
    #printf "%-25s %s\n" "NETMASK=$NETMASK"     #$NETMASKBIN
    #printf "%-25s %s\n" "NETWORK=$NETWORK"     #$NETWORKBIN
    #printf "%-25s %s\n" "BROADCAST=$BROADCAST" #$BROADCASTBIN
    eval "$3=\$NETWORK"
    eval "$4=\$BROADCAST"
}
configure_lan() {
    ROUTER_IPADDRESS=${1}
    ROUTER_SUBNET_MASK=${2}
    ROUTER_STAR_IP=${3}
    ROUTER_END_IP=${4}
    DHCP_LEASE_TIME=${5}
    DNS_SERVER1=${6}
    DNS_SERVER2=${7}
    DHCP_SERVER_ISUP=${8}
    GET_DNS_FROM_WAN=${9}
    calculate "$ROUTER_IPADDRESS" "$ROUTER_SUBNET_MASK" NETWORK BROADCAST
    sed -i "s/GET_DNS_FROM_WAN=.*$/GET_DNS_FROM_WAN=$GET_DNS_FROM_WAN/g" ${DHCP_CLIENT_HOOK_FILE}
    sed -i "s/GET_DNS_FROM_WAN=.*$/GET_DNS_FROM_WAN=$GET_DNS_FROM_WAN/g" ${IF_UP_HOOK_FILE}
    oldstate="$(mktemp)"
    md5sum $NETWORK_INTERFACE_FILE >"$oldstate" 2>/dev/null
    cat <<EOF >${DHCP_SUBNET_FILE}
    subnet $NETWORK netmask $ROUTER_SUBNET_MASK  {
        option routers $ROUTER_IPADDRESS;
        option broadcast-address $BROADCAST;
	    
        pool { 
		next-server $ROUTER_IPADDRESS;		
		range $ROUTER_STAR_IP $ROUTER_END_IP;
	    }
    }
EOF
    sed -i "s/default-lease-time.*$/default-lease-time $((DHCP_LEASE_TIME * 60));/g" ${DHCPD_CONFIG_FILE}
    if [[ "$GET_DNS_FROM_WAN" == "0" ]]; then
        if [[ -n "$DNS_SERVER1" && -z "$DNS_SERVER2" ]]; then
            NS="${DNS_SERVER1}"
        fi
        if [[ -n "$DNS_SERVER1" && -n "$DNS_SERVER2" ]]; then
            NS="${DNS_SERVER1},${DNS_SERVER2}"
        fi
        sed -i "s/option domain-name-servers.*$/option domain-name-servers $NS;/g" ${DHCPD_CONFIG_FILE}
    fi
    #sed -i "/iface br0 inet static/{!b;n;s/address.*/address $ROUTER_IPADDRESS/}" ${NETWORK_INTERFACE_FILE}
    #sed -i "/iface br0 inet static/{!b;n;n;s/netmask.*/netmask $ROUTER_SUBNET_MASK/}" ${NETWORK_INTERFACE_FILE}
    #sed -i "/iface br0 inet static/{!b;n;n;n;s/address.*/address $ROUTER_IPADDRESS/}" ${NETWORK_INTERFACE_FILE}
    #sed -i "/iface br0 inet static/{!b;n;n;n;n;s/netmask.*/netmask $ROUTER_SUBNET_MASK/}" ${NETWORK_INTERFACE_FILE}
    
    sed -i "/iface br0 inet static /n;s/address.*/address $ROUTER_IPADDRESS/" ${NETWORK_INTERFACE_FILE}
    sed -i "/iface br0 inet static /n;n;s/netmask.*/netmask $ROUTER_SUBNET_MASK/" ${NETWORK_INTERFACE_FILE}
    sed -i "/iface br0 inet static /n;n;n;s/address.*/address $ROUTER_IPADDRESS/" ${NETWORK_INTERFACE_FILE}
    sed -i "/iface br0 inet static /n;n;n;n;s/netmask.*/netmask $ROUTER_SUBNET_MASK/" ${NETWORK_INTERFACE_FILE}
 
   newstate="$(mktemp)"
    md5sum $NETWORK_INTERFACE_FILE >"$newstate" 2>/dev/null
    if ! cmp "$oldstate" "$newstate"; then
        pkill -f "ifup .*br0" ; ifdown --force br0 && ifup br0
        #sleep 1s
        #ifup br0
        sleep 2s
    fi
    rm "$oldstate"
    rm "$newstate"
    if [[ "$GET_DNS_FROM_WAN" == "1" ]]; then
        systemctl daemon-reload
        systemctl restart networking
    fi
    if [[ "$DHCP_SERVER_ISUP" == "1" ]]; then
        systemctl restart isc-dhcp-server
    else
        systemctl stop isc-dhcp-server
    fi
}
address_reservation() {
    cat <<EOF >${DHCP_RESERVATION_FILE}
EOF
    if [[ ${2} -eq 1 ]]; then
        exit 0
    fi
    JSON=${1}
    mapfile -t HOSTNAMES < <(echo "$JSON" | json_pp | grep '"hostname"' | sed 's/.* : "\(.*\)".*/\1/')
    mapfile -t MACADDRESS < <(echo "$JSON" | json_pp | grep '"macAddress"' | sed 's/.* : "\(.*\)".*/\1/')
    mapfile -t IPADDRESS < <(echo "$JSON" | json_pp | grep '"ipAddress"' | sed 's/.* : "\(.*\)".*/\1/')
    mapfile -t IENABLED < <(echo "$JSON" | json_pp | grep '"enabled"' | sed 's/.* : "\(.*\)".*/\1/')
    for i in "${!HOSTNAMES[@]}"; do
        if [[ "${IENABLED[i]}" == "1" ]]; then
            cat <<EOF >>${DHCP_RESERVATION_FILE}
	        host ${HOSTNAMES[i]} {
	             hardware ethernet ${MACADDRESS[i]};
                 fixed-address ${IPADDRESS[i]};
 	}
EOF
        fi
    done
    systemctl -q is-active isc-dhcp-server && systemctl restart isc-dhcp-server
}
configure_services() {
    JSON=${1}
    mapfile -t PROTOCOL < <(echo "$JSON" | json_pp | grep '"protocol"' | sed 's/.* : "\(.*\)".*/\1/')
    mapfile -t START_PORT < <(echo "$JSON" | json_pp | grep '"startPort"' | sed 's/.* : "\(.*\)".*/\1/')
    mapfile -t END_PORT < <(echo "$JSON" | json_pp | grep '"endPort"' | sed 's/.* : "\(.*\)".*/\1/')
    mapfile -t ENABLED < <(echo "$JSON" | json_pp | grep '"enabled"' | sed 's/.* : "\(.*\)".*/\1/')
    sed -i '/^#CUSTOM_RULE_START/,/^\#CUSTOM_RULE_END/{/^#CUSTOM_RULE_START/!{/^\#CUSTOM_RULE_END/!d}}' $RC_FILE
    for i in "${!PROTOCOL[@]}"; do
        if [[ "${ENABLED[i]}" == "1" ]]; then
            if [[ "${PROTOCOL[i],,}" == "both" ]]; then
                sed -i "/#CUSTOM_RULE_START/aiptables -C \${CUSTOM_INPUT} -p tcp --dport ${START_PORT[i]}:${END_PORT[i]} -j ACCEPT &>/dev/null || iptables -A \${CUSTOM_INPUT} -p tcp --dport ${START_PORT[i]}:${END_PORT[i]} -j ACCEPT" ${RC_FILE}
                sed -i "/#CUSTOM_RULE_START/aiptables -C \${CUSTOM_INPUT} -p udp --dport ${START_PORT[i]}:${END_PORT[i]} -j ACCEPT &>/dev/null || iptables -A \${CUSTOM_INPUT} -p udp --dport ${START_PORT[i]}:${END_PORT[i]} -j ACCEPT" ${RC_FILE}
            else
                sed -i "/#CUSTOM_RULE_START/aiptables -C \${CUSTOM_INPUT} -p ${PROTOCOL[i],,} --dport ${START_PORT[i]}:${END_PORT[i]} -j ACCEPT &>/dev/null || iptables -A \${CUSTOM_INPUT} -p ${PROTOCOL[i],,} --dport ${START_PORT[i]}:${END_PORT[i]} -j ACCEPT" ${RC_FILE}
            fi
        fi
    done
    bash ${RC_FILE}
}

configure_port_forward() {
    JSON=${1}
    mapfile -t PROTOCOL < <(echo "$JSON" | json_pp | grep '"protocol"' | sed 's/.* : "\(.*\)".*/\1/')
    mapfile -t EXTERNAL_PORT < <(echo "$JSON" | json_pp | grep '"externalPort"' | sed 's/.* : "\(.*\)".*/\1/')
    mapfile -t INTERNAL_PORT < <(echo "$JSON" | json_pp | grep '"internalPort"' | sed 's/.* : "\(.*\)".*/\1/')
    mapfile -t IP_ADDRESS < <(echo "$JSON" | json_pp | grep '"toIpAddress"' | sed 's/.* : "\(.*\)".*/\1/')
    mapfile -t ENABLED < <(echo "$JSON" | json_pp | grep '"enabled"' | sed 's/.* : "\(.*\)".*/\1/')

    sed -i '/^#CUSTOM_RULE_PORT_FORWARD_START/,/^\#CUSTOM_RULE_PORT_FORWARD_END/{/^#CUSTOM_RULE_PORT_FORWARD_START/!{/^\#CUSTOM_RULE_PORT_FORWARD_END/!d}}' $RC_FILE
    for i in "${!PROTOCOL[@]}"; do
        if [[ "${ENABLED[i]}" == "1" ]]; then
            if [[ "${PROTOCOL[i],,}" == "both" ]]; then
                sed -i "/#CUSTOM_RULE_PORT_FORWARD_START/aiptables -t nat -i \${WAN_PORT} -C \${CUSTOM_PREROUTING}  -p tcp --dport ${EXTERNAL_PORT[i]}  -j DNAT  --to ${IP_ADDRESS[i]}:${INTERNAL_PORT[i]} &>/dev/null || iptables -t nat -i \${WAN_PORT} -A \${CUSTOM_PREROUTING}   -p tcp --dport ${EXTERNAL_PORT[i]} -j DNAT  --to ${IP_ADDRESS[i]}:${INTERNAL_PORT[i]}\niptables -C \${CUSTOM_FORWARD} -p  tcp -d  ${IP_ADDRESS[i]} --dport ${INTERNAL_PORT[i]} -j ACCEPT &>/dev/null || iptables -A \${CUSTOM_FORWARD} -p tcp  -d ${IP_ADDRESS[i]} --dport ${INTERNAL_PORT[i]} -j ACCEPT " ${RC_FILE}

                sed -i "/#CUSTOM_RULE_PORT_FORWARD_START/aiptables -t nat -i \${WAN_PORT} -C \${CUSTOM_PREROUTING}  -p udp --dport ${EXTERNAL_PORT[i]}  -j DNAT  --to ${IP_ADDRESS[i]}:${INTERNAL_PORT[i]} &>/dev/null || iptables -t nat -i \${WAN_PORT} -A \${CUSTOM_PREROUTING}   -p udp --dport ${EXTERNAL_PORT[i]} -j DNAT  --to ${IP_ADDRESS[i]}:${INTERNAL_PORT[i]}\niptables -C \${CUSTOM_FORWARD} -p  udp -d  ${IP_ADDRESS[i]} --dport ${INTERNAL_PORT[i]} -j ACCEPT &>/dev/null || iptables -A \${CUSTOM_FORWARD} -p udp  -d ${IP_ADDRESS[i]} --dport ${INTERNAL_PORT[i]} -j ACCEPT " ${RC_FILE}

            else
                sed -i "/#CUSTOM_RULE_PORT_FORWARD_START/aiptables -t nat -i \${WAN_PORT} -C \${CUSTOM_PREROUTING}  -p ${PROTOCOL[i],,} --dport ${EXTERNAL_PORT[i]}  -j DNAT  --to ${IP_ADDRESS[i]}:${INTERNAL_PORT[i]} &>/dev/null || iptables -t nat -i \${WAN_PORT} -A \${CUSTOM_PREROUTING}   -p ${PROTOCOL[i],,} --dport ${EXTERNAL_PORT[i]} -j DNAT  --to ${IP_ADDRESS[i]}:${INTERNAL_PORT[i]}\niptables -C \${CUSTOM_FORWARD} -p  ${PROTOCOL[i],,} -d  ${IP_ADDRESS[i]} --dport ${INTERNAL_PORT[i]} -j ACCEPT &>/dev/null || iptables -A \${CUSTOM_FORWARD} -p ${PROTOCOL[i],,}  -d ${IP_ADDRESS[i]} --dport ${INTERNAL_PORT[i]} -j ACCEPT " ${RC_FILE}
            fi
        fi
    done
    bash ${RC_FILE}
}
configure_access_point(){
if [[ "${9}" == "0" ]]; then
	sed -i '/hostapd/,+1 d' ${NETWORK_INTERFACE_FILE}
	sed -i '/^#WIFI_START/,/^\#WIFI_END/{/^#WIFI_START/!{/^\#WIFI_END/!d}}' $NETWORK_INTERFACE_FILE
	killall hostapd
	killall -q wpa_supplicant
	 exit 0
fi
WIRELESS_MODE="${1}"
#sed -i '/wpa_supplicant/,+1 d' ${NETWORK_INTERFACE_FILE}
sed -i '/hostapd/,+1 d' ${NETWORK_INTERFACE_FILE}
sed -i '/^#WIFI_START/,/^\#WIFI_END/{/^#WIFI_START/!{/^\#WIFI_END/!d}}' $NETWORK_INTERFACE_FILE

WAN_PORT="${10}"
if [[ "${1}" == "AP" ]]; then
    sed -i "/iface br0 inet static/apost-up /usr/sbin/hostapd -B ${HOSTAPD_CONF} \n     post-down killall hostapd" ${NETWORK_INTERFACE_FILE}
	SSID="${4}"
	SSID_PSK="${5}"
	#MODE="${6}"
	#AUTH_TYPE="${7}"
	#ENCRYPTION="${8}"
	sed -i "s/ssid=.*$/ssid=$SSID/g" ${HOSTAPD_CONF}
	sed -i "s/wpa_passphrase=.*$/wpa_passphrase=$SSID_PSK/g" ${HOSTAPD_CONF}
	#sed -i "s/hw_mode=.*$/hw_mode=$MODE/g" ${HOSTAPD_CONF}
	#sed -i "s/wpa_key_mgmt=.*$/wpa_key_mgmt=$AUTH_TYPE/g" ${HOSTAPD_CONF}
	#sed -i "s/ssid=.*$/ssid=$SSID/g" ${HOSTAPD_CONF}
	sed  -i '/'"iface ${WAN_PORT} inet dhcp"'/d' ${NETWORK_INTERFACE_FILE}
	#sed -i '/${WAN_PORT}/,+1 d' ${NETWORK_INTERFACE_FILE}
	pkill -f "ifup .*br0"; ifdown --force br0 && ifup br0 && systemctl restart isc-dhcp-server
fi
if [[ "${1}" == "Client" ]]; then
    sed -i "/#WIFI_START/a\   \post-down killall -q wpa_supplicant" ${NETWORK_INTERFACE_FILE}
	sed -i "/#WIFI_START/a\   \pre-up wpa_supplicant -B -i ${WAN_PORT} -c ${WPA_SUPPLICANT_CONF}" ${NETWORK_INTERFACE_FILE}
    sed -i "/#WIFI_START/aiface ${WAN_PORT} inet dhcp" ${NETWORK_INTERFACE_FILE}
    
     #   sed -i '$a\'"iface ${WAN_PORT} inet dhcp"'' ${NETWORK_INTERFACE_FILE}
     #   sed -i '$a\'"   pre-up wpa_supplicant -B -i ${WAN_PORT} -c ${WPA_SUPPLICANT_CONF} "'' ${NETWORK_INTERFACE_FILE}
     #   sed -i '$a\'"   post-down killall -q wpa_supplicant"'' ${NETWORK_INTERFACE_FILE}

		sed -i  's/ssid=.*$/ssid="'"${2}"'"/g' ${WPA_SUPPLICANT_CONF}
        sed -i  's/psk=.*$/psk="'"${3}"'"/g' ${WPA_SUPPLICANT_CONF}
	pkill -f "ifup .*${WAN_PORT}"; ifdown --force ${WAN_PORT} && ifup ${WAN_PORT}
fi

exit 0
 
}
test_wireless_connecion(){
killall -q wpa_supplicant
killall -q wpa_supplicant	
LOG_FILE="/tmp/wpalog"
PIDFILE="/tmp/wpapid"
dev="$1"
ssid="$2"
pass="$3"

wpa_supplicant -Dnl80211 -c <(wpa_passphrase "${ssid}" ${pass}) -B -P ${PIDFILE} -f ${LOG_FILE} -i ${dev} 2>/dev/null
STATUS=$?
if test $STATUS -ne 0 ; then
echo "Unable to join the network $ssid."
exit 1
fi
string_success="WPA: Key negotiation completed"
string_Failure="pre-shared key may be incorrect"
string_Failure_1="auth_failures=1"
string_Failure_2="Failed to initiate sched scan"
SECONDS=0
RESULT=1
tail -n0 -F "$LOG_FILE" | \
while read -t 60  LINE
do
  if echo "$LINE" | grep "$string_success" 1>/dev/null 2>&1
  then
    echo "Password Verified successfully for $ssid."
    RESULT=0
    break;
  fi
  if echo "$LINE" | grep "$string_Failure" 1>/dev/null 2>&1
  then
    echo "Incorrect Password for $ssid."
    RESULT=0
    break;
  fi
  if echo "$LINE" | grep "$string_Failure_1" 1>/dev/null 2>&1
  then
    echo "Incorrect Password for $ssid."
    RESULT=0
    break;
  fi
  if echo "$LINE" | grep "$string_Failure_2" 1>/dev/null 2>&1
  then
    echo "Unable to join the network $ssid."
    RESULT=0
    break;
  fi
  if [[ $SECONDS == 30 ]] ; then
    echo "Timeout Unable to join the network $ssid."
    RESULT=0
    break;
  fi
done
if [[ $RESULT == 1 ]] ; then
 echo "Timeout Unable to join the network $ssid."
fi
if [[ -f "$PIDFILE" ]]; then
  var=$(cat $PIDFILE)
  kill -9 $var
  rm -rf $LOG_FILE
  rm -rf $PIDFILE
fi	
ifdown --force "${dev}"
}

main "$@"
