package com.utt.urouter.controller;

import java.io.FileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.utt.urouter.model.GenericResponse;
import com.utt.urouter.model.RouterHome;
import com.utt.urouter.model.RouterStatus;
import com.utt.urouter.service.RouterHomeService;
import com.utt.urouter.util.LinuxUtil;
import com.utt.urouter.util.RouterConstants;

@RestController
@RequestMapping(path = "/api")
public class RouterHomeController {
	private static final Logger LOGGER = LoggerFactory.getLogger(RouterHomeController.class);
	
	@Autowired
	RouterHomeService routerHomeService;

	@GetMapping(value = "/getRouterHomeData")
	public ResponseEntity<GenericResponse>  getRouterHomeData(){
		LOGGER.info("Calling Home Router Data API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			RouterHome routerHome = routerHomeService.getRouterHomeData();
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage(RouterConstants.GET_SUCCESS_MESSAGE);
			genericResponse.setData(routerHome);
			return new ResponseEntity<>(genericResponse,HttpStatus.OK);
		}catch (FileNotFoundException e) {
			LOGGER.error("{}",e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage("Could not find configutation file: config/config.yaml");
			return new ResponseEntity<>(genericResponse,HttpStatus.OK);
		}catch (Exception e) {
			LOGGER.error("{}",e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage("Failed to get Router Home details");
			genericResponse.setData(e.toString());
			return new ResponseEntity<>(genericResponse,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping(value = "/getRouterStatus")
	public ResponseEntity<GenericResponse>  getRouterStatus(){
		LOGGER.info("Calling Router Status Data API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			RouterStatus routerStatus = routerHomeService.getRouterStatus();
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage("Success");
			genericResponse.setData(routerStatus);
			return new ResponseEntity<>(genericResponse,HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("{}",e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage("Failed to get Router Status");
			genericResponse.setData(e.toString());
			return new ResponseEntity<>(genericResponse,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping(value = "/getDhcpClients")
	public ResponseEntity<GenericResponse> getDhcpClients(){
		LOGGER.info("Calling Router Status Data API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage("Success");
			genericResponse.setData(LinuxUtil.getAllDhcpClients());
			return new ResponseEntity<>(genericResponse,HttpStatus.OK);
		}catch (Exception e) {
			LOGGER.error("{}",e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage("Failed to get Dhcp clients.");
			genericResponse.setData(e.toString());
			return new ResponseEntity<>(genericResponse,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}