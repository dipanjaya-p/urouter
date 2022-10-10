package com.utt.urouter.controller;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.utt.urouter.model.GenericResponse;
import com.utt.urouter.model.InterfaceInfo;
import com.utt.urouter.util.LinuxUtil;

@RestController
@RequestMapping(path = "/api")
public class NetWorkInterfaceController {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetWorkInterfaceController.class);

	@GetMapping(value = "/network/interfaces/names")
	public ResponseEntity<GenericResponse> getNetworkInterfaceNames() {
		LOGGER.info("Calling Network InterfacesNames API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage("Success");
			genericResponse.setData(LinuxUtil.getAllInterfaceNames());
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		} catch (Exception e) {
			genericResponse.setStatus(HttpStatus.NOT_FOUND.name());
			genericResponse.setMessage("Failed to fetch network interface names.");
			genericResponse.setData(new ArrayList<GenericResponse>());
			LOGGER.error("Exception getting Network InterfacesNames {}", e);
		}
		return new ResponseEntity<>(genericResponse, HttpStatus.NOT_FOUND);
	}

	@GetMapping(value = "/network/interfaces/list")
	public ResponseEntity<List<InterfaceInfo>> getNetworkInterfaces() {
		LOGGER.info("Calling Network Interfaces list API");
		try {
			return new ResponseEntity<>(LinuxUtil.getAllInterfaces(), HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Exception getting Network Interfaces {}", e);
		}
		return new ResponseEntity<>(new ArrayList<InterfaceInfo>(), HttpStatus.NOT_FOUND);
	}
	
	@GetMapping(value = "/network/interfaces/{ifaceName}")
	public ResponseEntity<InterfaceInfo> getNetworkInterfaceByName(@Validated @PathVariable String ifaceName) {
		LOGGER.info("Calling Network Interfaces API");
		try {
			return new ResponseEntity<>(LinuxUtil.getInterface(ifaceName), HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Exception getting Network Interfaces {}", e);
		}
		return new ResponseEntity<>(new InterfaceInfo(""), HttpStatus.NOT_FOUND);
	}
	
	@GetMapping(value = "/network/methods")
	public ResponseEntity<GenericResponse> getNetworkMethods() {
		LOGGER.info("Calling Network Method API");
		GenericResponse genericResponse = new GenericResponse();
		List<String> methods = new ArrayList<>();
		methods.add("DHCP");
		methods.add("STATIC");
		try {
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage("Success");
			genericResponse.setData(methods);
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		} catch (Exception e) {
			genericResponse.setStatus(HttpStatus.NOT_FOUND.name());
			genericResponse.setMessage("Failed to fetch network methods.");
			LOGGER.error("Exception in getting Network Methods {}", e);
		}
		return new ResponseEntity<>(genericResponse, HttpStatus.NOT_FOUND);
	}
}
