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
import com.utt.urouter.model.Reservation;
import com.utt.urouter.model.ReservationDTO;
import com.utt.urouter.service.AddressReservationService;
import com.utt.urouter.util.LinuxUtil;
import com.utt.urouter.util.RouterConstants;
import io.jsonwebtoken.lang.Collections;

@RestController
@RequestMapping(path = "/api")
public class AddressReservationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(AddressReservationController.class);

	@Autowired
	AddressReservationService addressReservationService;
	String hostNamePattern = "^[a-zA-Z0-9_-]*$";

	@GetMapping(value = "/getStaticClients")
	public ResponseEntity<GenericResponse> getStaticClients() {
		LOGGER.info("Calling get Static client API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			List<Reservation> staticClientList = addressReservationService.getStaticClients();
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage(RouterConstants.GET_SUCCESS_MESSAGE);
			genericResponse.setData(staticClientList == null ? new ArrayList<Reservation>() : staticClientList);
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage(RouterConstants.GET_FAIL_MESSAGE);
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/saveStaticClients")
	public ResponseEntity<GenericResponse> saveStaticClients(
			@Validated @RequestBody List<ReservationDTO> addressReservations) {
		LOGGER.info("Calling Save Static client Settings API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			Map<String, Reservation> reservationMap = addressReservationService.getReservationMap();
			for(ReservationDTO reservation: addressReservations) {
				if((reservation.getIpAddress().isEmpty() || reservation.getIpAddress().equals(RouterConstants.DEFAULT_IP_ADDRESS)) || 
						!reservation.getIpAddress().matches(RouterConstants.IP_ADDRESS_PATTERN) || reservation.getHostname().isEmpty() || reservation.getMacAddress().isEmpty()) {
					genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
					genericResponse.setMessage(RouterConstants.INVALID_IP_ADDRESS);
					LOGGER.info("Address Reservation fields are not valid: {}",reservation);
					return new ResponseEntity<>(genericResponse, HttpStatus.OK);
				} else {
					if(reservation.getSelectedHostName() != null && !reservation.getSelectedHostName().isEmpty()
							&& reservationMap.containsKey(reservation.getSelectedHostName())) {
						if(!reservation.getHostname().matches(hostNamePattern)) {
							genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
							genericResponse.setMessage(RouterConstants.SPECIAL_CHARACTERS_NOT_ALLOWED);
							LOGGER.info("Special characters exists. HostName: {}",reservation.getHostname());
							return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
						}
						Reservation[] addReserArray = new Reservation[reservationMap.values().size()];
						addReserArray = reservationMap.values().toArray(addReserArray);
						for(Reservation addRes:addReserArray) {
							if(!addRes.getHostname().equals(reservation.getSelectedHostName()) && addRes.getHostname().equals(reservation.getHostname())){
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.HOST_NAME_PROPERTY_EXISTS);
								LOGGER.info("Host Name  already exists. HostName: {}, IP Address:{}, MAC: {}",reservation.getHostname(),reservation.getIpAddress(),reservation.getMacAddress());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						for(Reservation addRes:addReserArray) {
							if(!addRes.getHostname().equals(reservation.getSelectedHostName()) && addRes.getMacAddress().equals(reservation.getMacAddress())){
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.MAC_ADDRESS_PROPERTY_EXISTS);
								LOGGER.info("MAC Address already exists. HostName: {}, IP Address:{}, MAC: {}",reservation.getHostname(),reservation.getIpAddress(),reservation.getMacAddress());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						for(Reservation addRes:addReserArray) {
							if(!addRes.getHostname().equals(reservation.getSelectedHostName()) && addRes.getIpAddress().equals(reservation.getIpAddress())){
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.ADDRESS_RESERVATION_PROPERTY_EXISTS);
								LOGGER.info("IP Address already exists. HostName: {}, IP Address:{}, MAC: {}",reservation.getHostname(),reservation.getIpAddress(),reservation.getMacAddress());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						Reservation addRes = new Reservation();
						addRes.setHostname(reservation.getHostname());
						addRes.setMacAddress(reservation.getMacAddress());
						addRes.setIpAddress(reservation.getIpAddress());
						addRes.setEnabled(reservation.getEnabled());
						
						reservationMap.remove(reservation.getSelectedHostName());
						reservationMap.put(reservation.getHostname(), addRes);
					} else {
						if(!reservation.getHostname().matches(hostNamePattern)) {
							genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
							genericResponse.setMessage(RouterConstants.SPECIAL_CHARACTERS_NOT_ALLOWED);
							LOGGER.info("Special characters exists. HostName: {}",reservation.getHostname());
							return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
						}

						Reservation[] addReserArray = new Reservation[reservationMap.values().size()];
						addReserArray = reservationMap.values().toArray(addReserArray);
						for(Reservation addRes:addReserArray) {
							if(!addRes.getHostname().equals(reservation.getSelectedHostName()) && addRes.getHostname().equals(reservation.getHostname())){
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.HOST_NAME_PROPERTY_EXISTS);
								LOGGER.info("Host Name  already exists. HostName: {}, IP Address:{}, MAC: {}",reservation.getHostname(),reservation.getIpAddress(),reservation.getMacAddress());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						for(Reservation addRes:addReserArray) {
							if(!addRes.getHostname().equals(reservation.getSelectedHostName()) && addRes.getMacAddress().equals(reservation.getMacAddress())){
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.MAC_ADDRESS_PROPERTY_EXISTS);
								LOGGER.info("MAC Address already exists. HostName: {}, IP Address:{}, MAC: {}",reservation.getHostname(),reservation.getIpAddress(),reservation.getMacAddress());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						for(Reservation addRes:addReserArray) {
							if(!addRes.getHostname().equals(reservation.getSelectedHostName()) && addRes.getIpAddress().equals(reservation.getIpAddress())){
								genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
								genericResponse.setMessage(RouterConstants.ADDRESS_RESERVATION_PROPERTY_EXISTS);
								LOGGER.info("IP Address already exists. HostName: {}, IP Address:{}, MAC: {}",reservation.getHostname(),reservation.getIpAddress(),reservation.getMacAddress());
								return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						Reservation addRes = new Reservation();
						addRes.setHostname(reservation.getHostname());
						addRes.setMacAddress(reservation.getMacAddress());
						addRes.setIpAddress(reservation.getIpAddress());
						addRes.setEnabled(reservation.getEnabled());
						
						reservationMap.put(reservation.getHostname(), addRes);
					}
				}
			}
			addressReservationService.updateAllStaticClients(Collections.arrayToList(reservationMap.values().toArray()));
			CompletableFuture.runAsync(() -> LinuxUtil.updateAddressReservationClients(Collections.arrayToList(reservationMap.values().toArray()),false));
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
	@PostMapping(value = "/deleteStaticClient")
	public ResponseEntity<GenericResponse> deleteStaticClient(@Validated @RequestBody Reservation addressReservation) {
		LOGGER.info("Calling delete Static client Settings API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			Map<String, Reservation> reservationMap = addressReservationService.getReservationMap();
			if(reservationMap.keySet().contains(addressReservation.getHostname())) {
				reservationMap.remove(addressReservation.getHostname());
			} else {
				genericResponse.setStatus(HttpStatus.BAD_REQUEST.name());
				genericResponse.setMessage(RouterConstants.DELETE_FAIL_MESSAGE);
				LOGGER.info("Static client is not present.");
				return new ResponseEntity<>(genericResponse, HttpStatus.OK);
			}
			addressReservationService.updateAllStaticClients(Collections.arrayToList(reservationMap.values().toArray()));
			CompletableFuture.runAsync(() -> LinuxUtil.updateAddressReservationClients(Collections.arrayToList(reservationMap.values().toArray()),false));
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

	@GetMapping(value = "/enableAllStaticClients")
	public ResponseEntity<GenericResponse> enableAllStaticClients() {
		LOGGER.info("Calling enable Static client API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			List<Reservation> staticClientList = addressReservationService.getStaticClients();
			for(Reservation reservation: staticClientList) {
				reservation.setEnabled("1");
			}
			addressReservationService.updateAllStaticClients(staticClientList);
			CompletableFuture.runAsync(() -> LinuxUtil.updateAddressReservationClients(staticClientList,false));

			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage("Successfully enabled.");
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		}catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage("Failed to enable the clients.");
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/disableAllStaticClients")
	public ResponseEntity<GenericResponse> disableAllStaticClients() {
		LOGGER.info("Calling disable Static client API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			List<Reservation> staticClientList = addressReservationService.getStaticClients();
			for(Reservation reservation: staticClientList) {
				reservation.setEnabled("0");
			}
			addressReservationService.updateAllStaticClients(staticClientList);
			CompletableFuture.runAsync(() -> LinuxUtil.updateAddressReservationClients(staticClientList,true));
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage("Successfully disabled.");
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		}catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage("Failed to disable the clients.");
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/deleteAllStaticClients")
	public ResponseEntity<GenericResponse> deleteAllStaticClients() {
		LOGGER.info("Calling delete Static client API");
		GenericResponse genericResponse = new GenericResponse();
		try {
			addressReservationService.deleteAllStaticClients();
			genericResponse.setStatus(HttpStatus.OK.name());
			genericResponse.setMessage(RouterConstants.DELETE_SUCCESS_MESSAGE);
			CompletableFuture.runAsync(() -> LinuxUtil.updateAddressReservationClients(null,true));
			return new ResponseEntity<>(genericResponse, HttpStatus.OK);
		}catch (Exception e) {
			LOGGER.error("{}", e);
			genericResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
			genericResponse.setMessage("Failed to delete static clients.");
			return new ResponseEntity<>(genericResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
