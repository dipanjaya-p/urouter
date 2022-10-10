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
import com.utt.urouter.model.GenericResponse;
import com.utt.urouter.model.WanSettings;
import com.utt.urouter.model.WirelessSettings;
import com.utt.urouter.service.WanService;
import com.utt.urouter.service.WirelessService;
import com.utt.urouter.util.CommadExecutionException;
import com.utt.urouter.util.LinuxUtil;
import com.utt.urouter.util.RouterConstants;

@RestController
@RequestMapping(path = "/api/wireless")
public class WirelessController {
	private static final Logger LOGGER = LoggerFactory.getLogger(WirelessController.class);
	
	@Autowired
	WirelessService wirelessService;
	@Autowired
    WanService wanService;
	@GetMapping(value = "/get")
	public ResponseEntity<GenericResponse> getWirelessSettingsData() {
		LOGGER.info("Calling Wireless Settings API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			WirelessSettings wirelessSettings = wirelessService.getWirelessSettingsData();
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage(RouterConstants.GET_SUCCESS_MESSAGE);
			genericResponse.setData(wirelessSettings);
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		}
		catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(RouterConstants.GET_FAIL_MESSAGE);
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(value = "/save")
	public ResponseEntity<GenericResponse> saveWirelessSettings(@Validated @RequestBody WirelessSettings wirelessSettings) {
		LOGGER.info("Calling Save Wireless Settings API");
		GenericResponse genericResponse = new GenericResponse();
		try {
		  
			wirelessService.saveWirelessSettings(wirelessSettings);
            CompletableFuture.runAsync(() -> LinuxUtil.writeWirelessSettingstoDevice(wirelessSettings));
           // CompletableFuture.runAsync(() ->  LinuxUtil.restartWIFI(wirelessSettings));
		  genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage(RouterConstants.SAVE_SUCCESS_MESSAGE);
			LOGGER.info("Successfully saved Wireless settings. {}", wirelessSettings);
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		} catch (CommadExecutionException e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(e.getMessage());
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(RouterConstants.SAVE_FAIL_MESSAGE);
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}	
	}
	
	@PostMapping(value = "/connectWifi")
    public ResponseEntity<GenericResponse> connectWifi(@Validated @RequestBody WirelessSettings wirelessSettings) {
        LOGGER.info("Calling Connect wifi API");
        GenericResponse genericResponse = new GenericResponse();
        try {
          if(wirelessSettings.getPassword().isEmpty()|| wirelessSettings.getPassword().length() > RouterConstants.MAX_CHAR || wirelessSettings.getPassword().length() < RouterConstants.MIN_CHAR) {
              genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
              genericResponse.setMessage(RouterConstants.PWD_RANGE_ERROR_MESSAGE);
              LOGGER.error(RouterConstants.PWD_ERROR_MESSAGE);
              return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
          } else {
              // LinuxUtil.restartWIFI(wirelessSettings);
              String result = LinuxUtil.testWIFIConnection(wirelessSettings);
               // wirelessSettings.setWifiConnectionStatus(LinuxUtil.isWifiConnected(wirelessSettings.getWirelessInterface()));
              genericResponse.setData(wirelessSettings);
              genericResponse.setStatus(result.contains("successfully")?HttpStatus.OK.name():HttpStatus.PRECONDITION_FAILED.name());
              genericResponse.setMessage(result);
              LOGGER.info("wifi connection status : {}", result);
              return new ResponseEntity<>(genericResponse, result.contains("successfully")?HttpStatus.OK:HttpStatus.PRECONDITION_FAILED);
          }
          
        } catch (Exception e) {
            LOGGER.error("{}", e);
            genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
            genericResponse.setMessage(RouterConstants.SAVE_FAIL_MESSAGE);
            return new ResponseEntity<>(genericResponse, HttpStatus.OK);
        }
    }
    
    @PostMapping(value = "/disconnectWifi")
    public ResponseEntity<GenericResponse> disconnectWifi(@Validated @RequestBody WirelessSettings wirelessSettings) {
        LOGGER.info("Calling disconnect wifi API");
        GenericResponse genericResponse = new GenericResponse();
        try {          
         wirelessSettings.setWifiConnectionStatus(LinuxUtil.stopWIFI(wirelessSettings));
         genericResponse.setStatus(HttpStatus.OK.name());
         genericResponse.setMessage(RouterConstants.DISCONNECTION_SUCCESS_MESSAGE);
         genericResponse.setData(wirelessSettings);
         LOGGER.info("Successfully disconnected to wifi: {}", wirelessSettings.getSsidName());
         return new ResponseEntity<>(genericResponse, HttpStatus.OK);
         
        } catch (Exception e) {
            LOGGER.error("{}", e);
            genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
            genericResponse.setMessage(RouterConstants.SAVE_FAIL_MESSAGE);
            return new ResponseEntity<>(genericResponse, HttpStatus.OK);
        }
    }
    @GetMapping(value = "/connectionStatus")
    public ResponseEntity<GenericResponse> isWIFIConnected() {
        LOGGER.info("Calling connectionStatus API");
        GenericResponse genericResponse = new GenericResponse();
        try {
            WirelessSettings wirelessSettings = wirelessService.getWirelessSettingsData();
            genericResponse.setStatus(HttpStatus.OK.name());
            genericResponse.setMessage(RouterConstants.GET_SUCCESS_MESSAGE);
            genericResponse.setData(wirelessSettings);
            return new ResponseEntity<>(genericResponse, HttpStatus.OK);
        }
        catch (Exception e) {
            LOGGER.error("{}", e);
            genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
            genericResponse.setMessage(RouterConstants.GET_FAIL_MESSAGE);
            return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(value = "/getSsids")
    public ResponseEntity<GenericResponse> getSsids() {
        LOGGER.info("Calling Wireless getSssids API");
        GenericResponse genericResponse = new GenericResponse();
        try {
            LinuxUtil.getAllSSIDNames();
            WirelessSettings wirelessSettings =  new WirelessSettings();
            wirelessSettings.setSsidList(LinuxUtil.SSID_LIST);
            genericResponse.setStatus(HttpStatus.OK.name());
            genericResponse.setMessage(RouterConstants.GET_SUCCESS_MESSAGE);
            genericResponse.setData(wirelessSettings);
            return new ResponseEntity<>(genericResponse, HttpStatus.OK);
        }
        catch (Exception e) {
            LOGGER.error("{}", e);
            genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
            genericResponse.setMessage(RouterConstants.GET_FAIL_MESSAGE);
            return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(value = "/stopAP")
    public ResponseEntity<GenericResponse> stopAP() {
        LOGGER.info("Calling STOP AP  API");
        GenericResponse genericResponse = new GenericResponse();
        try {
              LinuxUtil.stopAP();
           // WirelessSettings wirelessSettings = wirelessService.getWirelessSettingsData();
           // wirelessSettings.setSsidList(LinuxUtil.SSID_LIST);
            genericResponse.setStatus(HttpStatus.OK.name());
            genericResponse.setMessage(RouterConstants.GET_SUCCESS_MESSAGE);
          //  genericResponse.setData(wirelessSettings);
            return new ResponseEntity<>(genericResponse, HttpStatus.OK);
        }
        catch (Exception e) {
            LOGGER.error("{}", e);
            genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
            genericResponse.setMessage(RouterConstants.GET_FAIL_MESSAGE);
            return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(value = "/startAP")
    public ResponseEntity<GenericResponse> startAP() {
        LOGGER.info("Calling START AP  API");
        GenericResponse genericResponse = new GenericResponse();
        try {
              LinuxUtil.startAP();
           // WirelessSettings wirelessSettings = wirelessService.getWirelessSettingsData();
           // wirelessSettings.setSsidList(LinuxUtil.SSID_LIST);
            genericResponse.setStatus(HttpStatus.OK.name());
            genericResponse.setMessage(RouterConstants.GET_SUCCESS_MESSAGE);
          //  genericResponse.setData(wirelessSettings);
            return new ResponseEntity<>(genericResponse, HttpStatus.OK);
        }
        catch (Exception e) {
            LOGGER.error("{}", e);
            genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
            genericResponse.setMessage(RouterConstants.GET_FAIL_MESSAGE);
            return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(value = "/getWAN")
    public ResponseEntity<GenericResponse> getWANAdapter() {
        LOGGER.info("Calling getWANAdapter  API");
        GenericResponse genericResponse = new GenericResponse();
        try {
          WanSettings wanSettings = wanService.getWanSettingsData();
           // WirelessSettings wirelessSettings = wirelessService.getWirelessSettingsData();
           // wirelessSettings.setSsidList(LinuxUtil.SSID_LIST);
            genericResponse.setStatus(HttpStatus.OK.name());
            genericResponse.setMessage(RouterConstants.GET_SUCCESS_MESSAGE);
            genericResponse.setData(wanSettings.getWanInterface());
            return new ResponseEntity<>(genericResponse, HttpStatus.OK);
        }
        catch (Exception e) {
            LOGGER.error("{}", e);
            genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
            genericResponse.setMessage(RouterConstants.GET_FAIL_MESSAGE);
            return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
