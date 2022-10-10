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
import com.utt.urouter.model.RouterService;
import com.utt.urouter.model.RouterServiceDTO;
import com.utt.urouter.service.RouterSystemService;
import com.utt.urouter.util.LinuxUtil;
import com.utt.urouter.util.RouterConstants;
import io.jsonwebtoken.lang.Collections;

@RestController
@RequestMapping(path = "/api")
public class RouterServiceController {
	private static final Logger LOGGER = LoggerFactory.getLogger(RouterServiceController.class);

	@Autowired
	RouterSystemService routerSystemService;

	@GetMapping(value = "/getRouterService")
	public ResponseEntity<GenericResponse> getRouterServices() {
		LOGGER.info("Calling get Router Service API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			List<RouterService> routerSystemServices = routerSystemService.getAllRouterServices();
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage(RouterConstants.GET_SUCCESS_MESSAGE);
			genericResponse.setData(routerSystemServices == null ? new ArrayList<RouterService>() : routerSystemServices);
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(RouterConstants.GET_FAIL_MESSAGE);
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/saveRouterService")
	public ResponseEntity<GenericResponse> saveRouterService(@Validated @RequestBody List<RouterServiceDTO> routerServices) {
		LOGGER.info("Calling Save Router Service API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			Map<String, RouterService> routerServiceMap = routerSystemService.getRouterServiceMap();
			for(RouterServiceDTO routerService: routerServices) {
				if(routerService.getName().equals(RouterConstants.NA) || routerService.getProtocol().equals(RouterConstants.DEFAULT_IP_ADDRESS) || 
						routerService.getStartPort().equals(RouterConstants.NA) || routerService.getEndPort().equals(RouterConstants.NA) || routerService.getEnabled().equals(RouterConstants.NA) ||
						routerService.getName().isEmpty() || routerService.getProtocol().isEmpty() || routerService.getStartPort().isEmpty() || 
						routerService.getEndPort().isEmpty() || routerService.getEnabled().isEmpty()) {
					genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
					genericResponse.setMessage(RouterConstants.INVALID_PARAMTER);
					LOGGER.info("RouterService fields are not valid: {}",routerService);
					return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				} else if(Integer.parseInt(routerService.getEndPort()) < Integer.parseInt(routerService.getStartPort())) {
					genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
					genericResponse.setMessage(RouterConstants.INVALID_PORT_SERVICE);
					LOGGER.info("End port is lesser than start port: End port: {}, Start port: {}",routerService.getEndPort(), routerService.getStartPort());
					return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				}else {
					if(routerService.getSelectedAppName() != null && !routerService.getSelectedAppName().isEmpty() &&
							routerServiceMap.containsKey(routerService.getSelectedAppName())) {
						RouterService[] rServiceArray = new RouterService[routerServiceMap.values().size()];
						rServiceArray = routerServiceMap.values().toArray(rServiceArray);
						for(RouterService rService:rServiceArray) {
							if(!rService.getName().equals(routerService.getSelectedAppName()) && rService.getName().equals(routerService.getName())) {
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.APPLICATION_NAME_PROPERTY_EXISTS);
								LOGGER.info("Application name already exists. Name: {},End port: {}, Start port: {}",routerService.getName(), routerService.getEndPort(), routerService.getStartPort());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						for(RouterService rService:rServiceArray) {
							if(!rService.getName().equals(routerService.getSelectedAppName()) && ((Integer.parseInt(routerService.getStartPort()) <= RouterConstants.MIN_PORT_NUMBER || Integer.parseInt(routerService.getStartPort()) > RouterConstants.MAX_PORT_NUMBER) ||
									(Integer.parseInt(routerService.getEndPort()) <= RouterConstants.MIN_PORT_NUMBER || Integer.parseInt(routerService.getEndPort()) > RouterConstants.MAX_PORT_NUMBER))) {
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.INVALID_PORT_RANGE);
								LOGGER.info("Invalid port range for the service.: End port: {}, Start port: {}",routerService.getEndPort(), routerService.getStartPort());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						for(RouterService rService:rServiceArray) {
							if(!rService.getName().equals(routerService.getSelectedAppName()) && (Integer.parseInt(rService.getStartPort()) >= Integer.parseInt(routerService.getStartPort()) &&
									Integer.parseInt(rService.getEndPort()) <= Integer.parseInt(routerService.getEndPort()))) {
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.PORT_RANGE_EXISTS_SERVICE);
								LOGGER.info("Port range is already exists for another service.: End port: {}, Start port: {}",routerService.getEndPort(), routerService.getStartPort());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						
						RouterService service = new RouterService();
						service.setName(routerService.getName());
						service.setStartPort(routerService.getStartPort());
						service.setEndPort(routerService.getEndPort());
						service.setProtocol(routerService.getProtocol());
						service.setEnabled(routerService.getEnabled());
						
						routerServiceMap.remove(routerService.getSelectedAppName());
						routerServiceMap.put(routerService.getName(), service);
					} else {
						
						RouterService[] rServiceArray = new RouterService[routerServiceMap.values().size()];
						rServiceArray = routerServiceMap.values().toArray(rServiceArray);
						for(RouterService rService:rServiceArray) {
							if(!rService.getName().equals(routerService.getSelectedAppName()) && rService.getName().equals(routerService.getName())) {
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.APPLICATION_NAME_PROPERTY_EXISTS);
								LOGGER.info("Application name already exists. Name: {},End port: {}, Start port: {}",routerService.getName(), routerService.getEndPort(), routerService.getStartPort());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						for(RouterService rService:rServiceArray) {
							if(!rService.getName().equals(routerService.getSelectedAppName()) && ((Integer.parseInt(routerService.getStartPort()) <= RouterConstants.MIN_PORT_NUMBER || Integer.parseInt(routerService.getStartPort()) > RouterConstants.MAX_PORT_NUMBER) ||
									(Integer.parseInt(routerService.getEndPort()) <= RouterConstants.MIN_PORT_NUMBER || Integer.parseInt(routerService.getEndPort()) > RouterConstants.MAX_PORT_NUMBER))) {
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.INVALID_PORT_RANGE);
								LOGGER.info("Invalid port range for service.: End port: {}, Start port: {}",routerService.getEndPort(), routerService.getStartPort());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						for(RouterService rService:rServiceArray) {
							if(!rService.getName().equals(routerService.getSelectedAppName()) && (Integer.parseInt(rService.getStartPort()) >= Integer.parseInt(routerService.getStartPort()) &&
									Integer.parseInt(rService.getEndPort()) <= Integer.parseInt(routerService.getEndPort()))) {
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.PORT_RANGE_EXISTS_SERVICE);
								LOGGER.info("Port range is already exists for another service.: End port: {}, Start port: {}",routerService.getEndPort(), routerService.getStartPort());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						
						RouterService service = new RouterService();
						service.setName(routerService.getName());
						service.setStartPort(routerService.getStartPort());
						service.setEndPort(routerService.getEndPort());
						service.setProtocol(routerService.getProtocol());
						service.setEnabled(routerService.getEnabled());
						
						routerServiceMap.put(routerService.getName(), service);
					}
				}
			}
			routerSystemService.updateAllRouterService(Collections.arrayToList(routerServiceMap.values().toArray()));
			CompletableFuture.runAsync(() -> LinuxUtil.updateFireWallServices(Collections.arrayToList(routerServiceMap.values().toArray()),"services"));
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage(RouterConstants.SAVE_SUCCESS_MESSAGE);
			LOGGER.info("Successfully saved settings");
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		}catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(RouterConstants.SAVE_FAIL_MESSAGE);
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/deleteRouterService")
	public ResponseEntity<GenericResponse> deleteRouterSystemService(@Validated @RequestBody RouterService routerService) {
		LOGGER.info("Calling delete Static client Settings API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			Map<String, RouterService> routerServiceMap = routerSystemService.getRouterServiceMap();
			if(routerServiceMap.keySet().contains(routerService.getName())) {
				routerServiceMap.remove(routerService.getName());
			} else {
				genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
				genericResponse.setMessage(RouterConstants.DELETE_FAIL_MESSAGE);
				LOGGER.info("Router Service is not present: {}",routerService.getName());
				return new ResponseEntity<>(genericResponse, HttpStatus.OK);
			}
			routerSystemService.updateAllRouterService(Collections.arrayToList(routerServiceMap.values().toArray()));
			CompletableFuture.runAsync(() -> LinuxUtil.updateFireWallServices(Collections.arrayToList(routerServiceMap.values().toArray()),"services"));
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
