package com.utt.urouter.controller;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.utt.urouter.model.DhcpServerSettings;
import com.utt.urouter.model.GenericResponse;
import com.utt.urouter.service.DhcpServerSettingsService;
import com.utt.urouter.util.LinuxUtil;
import com.utt.urouter.util.RouterConstants;

@RestController
@RequestMapping(path = "/api")
public class DhcpServerSettingsController {
	private static final Logger LOGGER = LoggerFactory.getLogger(DhcpServerSettingsController.class);

	@Autowired
	DhcpServerSettingsService dhcpServerSettingsService;
	String ipAddressPattern = "^(([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)\\.){3}([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)$";
	String numberPattern = "^[0-9]*$";

	@GetMapping(value = "/getDhcpServerSettings")
	public ResponseEntity<GenericResponse> getDhcpServerSettingsData() {
		LOGGER.info("Calling get Dhcp Server Settings API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			DhcpServerSettings dhcpServerSettings = dhcpServerSettingsService.getDhcpServerSettings();
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage(RouterConstants.GET_SUCCESS_MESSAGE);
			genericResponse.setData(dhcpServerSettings);
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(RouterConstants.GET_FAIL_MESSAGE);
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "/saveDhcpServerSettings")
	public ResponseEntity<GenericResponse> saveDhcpServerSettings(
			@Validated @RequestBody DhcpServerSettings dhcpServerSettings) {
		LOGGER.info("Calling Save Dhcp Server Settings API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			if (dhcpServerSettings.isUseWanDns() && dhcpServerSettings.getDefaultGatewayIp().equals(RouterConstants.DEFAULT_IP_ADDRESS)
//					|| dhcpServerSettings.getDnsServer1().equals(RouterConstants.DEFAULT_IP_ADDRESS)
//					|| dhcpServerSettings.getDnsServer2().equals(RouterConstants.DEFAULT_IP_ADDRESS)
					|| dhcpServerSettings.getLan().getIpAddress().equals(RouterConstants.DEFAULT_IP_ADDRESS)
					|| dhcpServerSettings.getLan().getSubnetmask().equals(RouterConstants.DEFAULT_IP_ADDRESS)) {
				genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
				genericResponse.setMessage(RouterConstants.INVALID_IP_ADDRESS);
				LOGGER.error(RouterConstants.SAVE_FAIL_MESSAGE);
				return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				if(!dhcpServerSettings.isUseWanDns() && (dhcpServerSettings.getDnsServer1().isEmpty())) {
					genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
					genericResponse.setMessage(RouterConstants.PRIMARY_DNS_REQUIRED);
					LOGGER.error(RouterConstants.SAVE_FAIL_MESSAGE); 
					return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				if(!dhcpServerSettings.isUseWanDns() && ((!dhcpServerSettings.getDnsServer1().isEmpty() && !dhcpServerSettings.getDnsServer1().matches(ipAddressPattern)) ||
						(!dhcpServerSettings.getDnsServer2().isEmpty() && !dhcpServerSettings.getDnsServer2().matches(ipAddressPattern)))) {
					genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
					genericResponse.setMessage(RouterConstants.INVALID_IP_ADDRESS);
					LOGGER.error(RouterConstants.SAVE_FAIL_MESSAGE); return new
							ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				if((!dhcpServerSettings.getLan().getIpAddress().isEmpty() && !dhcpServerSettings.getLan().getIpAddress().matches(ipAddressPattern)) || 
						(!dhcpServerSettings.getLan().getSubnetmask().isEmpty() && !dhcpServerSettings.getLan().getSubnetmask().matches(ipAddressPattern)) || 
						(!dhcpServerSettings.getStartIp().isEmpty() && !dhcpServerSettings.getStartIp().matches(ipAddressPattern)) || 
						(!dhcpServerSettings.getEndIp().isEmpty() && !dhcpServerSettings.getEndIp().matches(ipAddressPattern))) {
					genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
					genericResponse.setMessage(RouterConstants.INVALID_IP_ADDRESS);
					LOGGER.error(RouterConstants.SAVE_FAIL_MESSAGE);
					return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				if(dhcpServerSettings.getDhcpLeaseTime().isEmpty() || !dhcpServerSettings.getDhcpLeaseTime().matches(numberPattern)) {
					genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
					genericResponse.setMessage(RouterConstants.DHCP_LEASE_TIME_INVALID);
					LOGGER.error(RouterConstants.SAVE_FAIL_MESSAGE);
					return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				if(Integer.parseInt(dhcpServerSettings.getDhcpLeaseTime()) > 65535) {
					genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
					genericResponse.setMessage(RouterConstants.DHCP_LEASE_TIME_EXCEED);
					LOGGER.error(RouterConstants.SAVE_FAIL_MESSAGE);
					return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				}

				dhcpServerSettingsService.saveDhcpServerSettings(dhcpServerSettings);
				CompletableFuture.runAsync(() -> LinuxUtil.writeLanSettingstoDevice(dhcpServerSettings));
				genericResponse.setStatus(HttpStatus.OK.name());
				genericResponse.setMessage(RouterConstants.SAVE_SUCCESS_MESSAGE);
				LOGGER.info("Successfully saved settings");
				return new ResponseEntity<>(genericResponse, HttpStatus.OK);
			}
		} catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(RouterConstants.SAVE_FAIL_MESSAGE);
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
