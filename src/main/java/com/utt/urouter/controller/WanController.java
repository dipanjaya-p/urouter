package com.utt.urouter.controller;

import java.io.FileNotFoundException;
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
import com.utt.urouter.model.GenericResponse;
import com.utt.urouter.model.WanSettings;
import com.utt.urouter.service.WanService;
import com.utt.urouter.util.LinuxUtil;
import com.utt.urouter.util.RouterConstants;

@RestController
@RequestMapping(path = "/api")
public class WanController {
	private static final Logger LOGGER = LoggerFactory.getLogger(WanController.class);

	@Autowired
	WanService wanService;

	String ipAddressPattern = "^(([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)\\.){3}([1-9]?\\d|1\\d\\d|2[0-5][0-5]|2[0-4]\\d)$";

	@GetMapping(value = "/getWanSettings")
	public ResponseEntity<GenericResponse> getWanSettingsData() {
		LOGGER.info("Calling Wan Settings API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			WanSettings wanSettings = wanService.getWanSettingsData();
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage(RouterConstants.GET_SUCCESS_MESSAGE);
			genericResponse.setData(wanSettings);
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		} catch (FileNotFoundException e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(RouterConstants.FILE_NOT_FOUND_MESSAGE);
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(RouterConstants.GET_FAIL_MESSAGE);
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		}
	}

	@PostMapping(value = "/saveWanSettings")
	public ResponseEntity<GenericResponse> saveWanSettings(@Validated @RequestBody WanSettings wanSettings) {
		LOGGER.info("Calling Save Wan Settings API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			if(wanSettings.getWanMethod().equals(RouterConstants.STATIC) && wanSettings.getDnsServer1().isEmpty()) {
				genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
				genericResponse.setMessage(RouterConstants.PRIMARY_DNS_REQUIRED);
				LOGGER.error(RouterConstants.SAVE_FAIL_WAN_MESSAGE);
				return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
			} else if(wanSettings.getWanMethod().equals(RouterConstants.STATIC) && (wanSettings.getIpAddress().equals(RouterConstants.DEFAULT_IP_ADDRESS)
					|| wanSettings.getSubnetMask().equals(RouterConstants.DEFAULT_IP_ADDRESS)
					|| wanSettings.getDefaultGateway().equals(RouterConstants.DEFAULT_IP_ADDRESS)
					|| wanSettings.getDnsServer1().equals(RouterConstants.DEFAULT_IP_ADDRESS)
					|| wanSettings.getDnsServer2().equals(RouterConstants.DEFAULT_IP_ADDRESS))) {
				genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
				genericResponse.setMessage(RouterConstants.INVALID_IP_ADDRESS);
				LOGGER.error(RouterConstants.SAVE_FAIL_WAN_MESSAGE);
				return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
//				if((!wanSettings.getIpAddress().isEmpty() && !wanSettings.getIpAddress().matches(ipAddressPattern)) ||
//						(!wanSettings.getSubnetMask().isEmpty() && !wanSettings.getSubnetMask().matches(ipAddressPattern)) ||
//						(!wanSettings.getDefaultGateway().isEmpty() && !wanSettings.getDefaultGateway().matches(ipAddressPattern)) ||
//						(!wanSettings.getDnsServer1().isEmpty() && !wanSettings.getDnsServer1().matches(ipAddressPattern)) || 
//						(!wanSettings.getDnsServer2().isEmpty() && !wanSettings.getDnsServer2().matches(ipAddressPattern))) {
//					genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
//					genericResponse.setMessage(RouterConstants.INVALID_IP_ADDRESS);
//					LOGGER.error(RouterConstants.SAVE_FAIL_WAN_MESSAGE);
//					return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
////				} else if(wanSettings.getWifiPassword().isEmpty() || wanSettings.getWifiPassword().length() > RouterConstants.MAX_CHAR) {
////					genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
////					genericResponse.setMessage(RouterConstants.PWD_ERROR_MESSAGE);
////					LOGGER.error(RouterConstants.SAVE_FAIL_WAN_MESSAGE);
////					return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
////				}
//				}else {
					wanService.saveWanSettings(wanSettings);
					CompletableFuture.runAsync(() -> LinuxUtil.writeWanSettingstoDevice(wanSettings));
					genericResponse.setStatus(HttpStatus.OK.name());
					genericResponse.setMessage(RouterConstants.SAVE_SUCCESS_MESSAGE);
					LOGGER.info("Successfully saved Wan settings");
					return new ResponseEntity<>(genericResponse, HttpStatus.OK);
				//}
			}

		} catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(RouterConstants.SAVE_FAIL_MESSAGE);
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		}
	}
	
	
}
