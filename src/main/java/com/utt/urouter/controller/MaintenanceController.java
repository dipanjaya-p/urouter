package com.utt.urouter.controller;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.utt.urouter.model.GenericResponse;
import com.utt.urouter.service.FileUploadDownloadService;
import com.utt.urouter.util.LinuxUtil;

@RestController
@RequestMapping(path = "/api")
public class MaintenanceController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceController.class);
	
	@Autowired
	private FileUploadDownloadService fileUploadDownloadService;
	
	@GetMapping(value = "/getBackup")
	public ResponseEntity<Resource>  getBackup(){
		LOGGER.info("Calling getBackup API");
		GenericResponse genericResponse = new GenericResponse();
		try { 
			Resource backupFile = fileUploadDownloadService.getBackUp();
			return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(backupFile);
		}catch (MalformedURLException e) {
			LOGGER.error("{}",e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage("Malformed Uri.");
			genericResponse.setData(e.toString());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (FileNotFoundException e) {
			LOGGER.error("{}",e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage("Configuration file not found.");
			genericResponse.setData(e.toString());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			LOGGER.error("{}",e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage("Failed to get backup.");
			genericResponse.setData(e.toString());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(value = "/restore")
	public ResponseEntity<GenericResponse> restoreSettings(@RequestParam("file") MultipartFile file) {
		LOGGER.info("Calling restore API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			fileUploadDownloadService.restoreSettings(file);
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage("Successfully restored settings.");
			return new ResponseEntity<>(genericResponse,HttpStatus.OK);
		}catch (Exception e) {
			LOGGER.error("{}",e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(e.getMessage());
			genericResponse.setData(e.toString());
			return new ResponseEntity<>(genericResponse,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping(value = "/resetToFactory")
	public ResponseEntity<GenericResponse>  resetToFactorySettings(){
		LOGGER.info("Calling reset API");
		GenericResponse genericResponse = new GenericResponse();
		try { 
			fileUploadDownloadService.resetToFactorySettings();
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage("Successully restored factory default settings.");
			return new ResponseEntity<>(genericResponse,HttpStatus.OK);
		}catch (Exception e) {
			LOGGER.error("{}",e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage("Failed to restore factory default settings.");
			genericResponse.setData(e.toString());
			return new ResponseEntity<>(genericResponse,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping(value = "/applySettings")
	public ResponseEntity<GenericResponse>  restoreByCommandLine(){
		LOGGER.info("Calling Apply Settings API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			fileUploadDownloadService.applySettings();
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage("Successully applied settings.");
			return new ResponseEntity<>(genericResponse,HttpStatus.OK);
		}
		catch (Exception e) {
			LOGGER.error("{}",e);
			genericResponse.setStatus("Yaml parse error in config file.");
			genericResponse.setMessage(e.getMessage());
			genericResponse.setData(e.toString());
			return new ResponseEntity<>(genericResponse,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@GetMapping(value = "/rebootSystem")
    public ResponseEntity<GenericResponse>  rebootSystem(){
        LOGGER.info("Calling rebootSystem API");
        GenericResponse genericResponse = new GenericResponse();
        try {
          CompletableFuture.runAsync(() -> LinuxUtil.rebootSystem());
            genericResponse.setStatus(HttpStatus.OK.name());
            genericResponse.setMessage("Successully reboot called.");
            return new ResponseEntity<>(genericResponse,HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("{}",e);
            genericResponse.setStatus("reboot failed.");
            genericResponse.setMessage(e.getMessage());
            genericResponse.setData(e.toString());
            return new ResponseEntity<>(genericResponse,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
