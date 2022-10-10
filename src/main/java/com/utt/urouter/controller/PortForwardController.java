package com.utt.urouter.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.utt.urouter.model.PortForwarding;
import com.utt.urouter.model.PortForwardingDTO;
import com.utt.urouter.service.PortForwardService;
import com.utt.urouter.util.LinuxUtil;
import com.utt.urouter.util.RouterConstants;
import io.jsonwebtoken.lang.Collections;

@RestController
@RequestMapping(path = "/api")
public class PortForwardController {
	private static final Logger LOGGER = LoggerFactory.getLogger(PortForwardController.class);

	@Autowired
	PortForwardService portForwardService; 

	@GetMapping(value = "/getAllPortForwarding")
	public ResponseEntity<GenericResponse> getAllForwarding() {
		LOGGER.info("Calling get port Forward API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			List<PortForwarding> portForwardlist = portForwardService.getAllportForwards();
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage(RouterConstants.GET_SUCCESS_MESSAGE);
			genericResponse.setData(portForwardlist == null ? new ArrayList<PortForwarding>() : portForwardlist);
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(RouterConstants.GET_FAIL_MESSAGE);
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/savePortForwarding")
	public ResponseEntity<GenericResponse> savePortForwad(@Validated @RequestBody List<PortForwardingDTO> portForwads) {
		LOGGER.info("Calling Save Port Forward API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			Map<String, PortForwarding> portForwadMap = portForwardService.getPortForwardMap();
			for(PortForwardingDTO portForwarding: portForwads) {
				if(portForwarding.getName().equals(RouterConstants.NA) || portForwarding.getExternalPort().equals(RouterConstants.NA) || 
						portForwarding.getInternalPort().equals(RouterConstants.NA) ||  portForwarding.getProtocol().equals(RouterConstants.NA) ||
						portForwarding.getToIpAddress().equals(RouterConstants.NA) || portForwarding.getName().isEmpty() || portForwarding.getExternalPort().isEmpty() || 
						portForwarding.getInternalPort().isEmpty() || portForwarding.getProtocol().isEmpty() || portForwarding.getToIpAddress().isEmpty() ||  
						portForwarding.getEnabled().isEmpty()) {
					genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
					genericResponse.setMessage(RouterConstants.INVALID_PARAMTER);
					LOGGER.info("PortForwarding fields are not valid: {}",portForwarding);
					return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
				} else if(portForwarding.getToIpAddress().isEmpty() || !portForwarding.getToIpAddress().matches(RouterConstants.IP_ADDRESS_PATTERN)) {
					genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
					genericResponse.setMessage(RouterConstants.INVALID_IP_ADDRESS);
					LOGGER.error(RouterConstants.SAVE_FAIL_MESSAGE);
					return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				} else {
					if(portForwarding.getSelectedAppName() != null && !portForwarding.getSelectedAppName().isEmpty() &&
							portForwadMap.containsKey(portForwarding.getSelectedAppName())) {
						PortForwarding[] portForwardingArray = new PortForwarding[portForwadMap.values().size()];
						portForwardingArray = portForwadMap.values().toArray(portForwardingArray);
						for(PortForwarding portFwd:portForwardingArray) {
							if(!portFwd.getName().equals(portForwarding.getSelectedAppName()) && portFwd.getName().equals(portForwarding.getName())) {
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.APPLICATION_NAME_PROPERTY_EXISTS);
								LOGGER.info("Application name already exists. Name: {}, Internal Port:{}, External Port: {}",portForwarding.getName(),portForwarding.getInternalPort(),portForwarding.getExternalPort());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						for(PortForwarding portFwd:portForwardingArray) {
							if(!portFwd.getName().equals(portForwarding.getSelectedAppName()) && ((Integer.parseInt(portForwarding.getExternalPort()) <= RouterConstants.MIN_PORT_NUMBER || Integer.parseInt(portForwarding.getExternalPort()) > RouterConstants.MAX_PORT_NUMBER) || 
									(Integer.parseInt(portForwarding.getInternalPort()) <= RouterConstants.MIN_PORT_NUMBER || Integer.parseInt(portForwarding.getInternalPort()) > RouterConstants.MAX_PORT_NUMBER))){
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.INVALID_PORT_RANGE);
								LOGGER.info("Invalid port range. Name: {}, Internal Port:{}, External Port: {}",portForwarding.getName(),portForwarding.getInternalPort(),portForwarding.getExternalPort());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						for(PortForwarding portFwd:portForwardingArray) {
							if(!portFwd.getName().equals(portForwarding.getSelectedAppName()) && (portFwd.getExternalPort().equals(portForwarding.getExternalPort()))){
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.PORT_FORWARDING_EXISTS);
								LOGGER.info("Internal Port or External Port already exists. Name: {}, Internal Port:{}, External Port: {}",portForwarding.getName(),portForwarding.getInternalPort(),portForwarding.getExternalPort());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						PortForwarding portForward = new PortForwarding();
						portForward.setName(portForwarding.getName());
						portForward.setExternalPort(portForwarding.getExternalPort());
						portForward.setInternalPort(portForwarding.getInternalPort());
						portForward.setProtocol(portForwarding.getProtocol());
						portForward.setToIpAddress(portForwarding.getToIpAddress());
						portForward.setEnabled(portForwarding.getEnabled());

						portForwadMap.remove(portForwarding.getSelectedAppName());
						portForwadMap.put(portForward.getName(), portForward);
					} else {
						PortForwarding[] portForwardingArray = new PortForwarding[portForwadMap.values().size()];
						portForwardingArray = portForwadMap.values().toArray(portForwardingArray);
						for(PortForwarding portFwd:portForwardingArray) {
							if(!portFwd.getName().equals(portForwarding.getSelectedAppName()) && portFwd.getName().equals(portForwarding.getName())) {
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.APPLICATION_NAME_PROPERTY_EXISTS);
								LOGGER.info("Application name already exists. Name: {}, Internal Port:{}, External Port: {}",portForwarding.getName(),portForwarding.getInternalPort(),portForwarding.getExternalPort());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						for(PortForwarding portFwd:portForwardingArray) {
							if(!portFwd.getName().equals(portForwarding.getSelectedAppName()) && ((Integer.parseInt(portForwarding.getExternalPort()) <= RouterConstants.MIN_PORT_NUMBER || Integer.parseInt(portForwarding.getExternalPort()) > RouterConstants.MAX_PORT_NUMBER) || 
									(Integer.parseInt(portForwarding.getInternalPort()) <= RouterConstants.MIN_PORT_NUMBER || Integer.parseInt(portForwarding.getInternalPort()) > RouterConstants.MAX_PORT_NUMBER))){
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.INVALID_PORT_RANGE);
								LOGGER.info("Invalid port range. Name: {}, Internal Port:{}, External Port: {}",portForwarding.getName(),portForwarding.getInternalPort(),portForwarding.getExternalPort());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						for(PortForwarding portFwd:portForwardingArray) {
							if(!portFwd.getName().equals(portForwarding.getSelectedAppName()) && portFwd.getExternalPort().equals(portForwarding.getExternalPort())){
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.PORT_FORWARDING_EXISTS);
								LOGGER.info("External Port already exists. Name: {}, Internal Port:{}, External Port: {}",portForwarding.getName(),portForwarding.getInternalPort(),portForwarding.getExternalPort());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}

						PortForwarding portForward = new PortForwarding();
						portForward.setName(portForwarding.getName());
						portForward.setExternalPort(portForwarding.getExternalPort());
						portForward.setInternalPort(portForwarding.getInternalPort());
						portForward.setProtocol(portForwarding.getProtocol());
						portForward.setToIpAddress(portForwarding.getToIpAddress());
						portForward.setEnabled(portForwarding.getEnabled());

						portForwadMap.put(portForwarding.getName(), portForward);
					}
				}
			}
			portForwardService.updatePortForwards(Collections.arrayToList(portForwadMap.values().toArray()));
			CompletableFuture.runAsync(() -> LinuxUtil.updateFireWallServices(Collections.arrayToList(portForwadMap.values().toArray()),"port_forward"));
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage(RouterConstants.SAVE_SUCCESS_MESSAGE);
			LOGGER.info("Successfully saved settings");
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(RouterConstants.SAVE_FAIL_MESSAGE);
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/deletePortForwarding")
	public ResponseEntity<GenericResponse> deletePortForwad(@Validated @RequestBody PortForwarding portForwarding) {
		LOGGER.info("Calling delete Port Forward Settings API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			Map<String, PortForwarding> portForwadMap = portForwardService.getPortForwardMap();
			if(portForwadMap.keySet().contains(portForwarding.getName())) {
				portForwadMap.remove(portForwarding.getName());
			} else {
				genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
				genericResponse.setMessage(RouterConstants.DELETE_FAIL_MESSAGE);
				LOGGER.info("Port Forward is not present: {}",portForwarding.getName());
				return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
			}
			portForwardService.updatePortForwards(Collections.arrayToList(portForwadMap.values().toArray()));
			CompletableFuture.runAsync(() -> LinuxUtil.updateFireWallServices(Collections.arrayToList(portForwadMap.values().toArray()),"port_forward"));
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage(RouterConstants.DELETE_SUCCESS_MESSAGE);
			LOGGER.info("Successfully deleted settings.");
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		}catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(RouterConstants.DELETE_FAIL_MESSAGE);
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
