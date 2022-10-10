package com.utt.urouter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.utt.urouter.model.AuthMetaData;
import com.utt.urouter.model.GenericResponse;
import com.utt.urouter.model.LoginCredential;
import com.utt.urouter.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Mahantesh.M
 */
@RestController
@CrossOrigin
@RequestMapping(path = "/api")
public class AuthenticationController {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

  @Autowired private AuthenticationService authService;

  @PostMapping(value = "/login")
  public ResponseEntity<GenericResponse> login(
      @Validated @RequestBody LoginCredential loginCredential) {
    return authService.login(loginCredential);
  }

  @GetMapping(value = "/logout")
  public ResponseEntity<GenericResponse> logout(HttpServletRequest request) {
    return authService.logout(request);
  }

  @GetMapping(value = "/refreshToken")
  public ResponseEntity<String> refreshAndGetAuthenticationToken(HttpServletRequest request) {
    return authService.refreshToken(request);
  }

  @PostMapping(value = "/authenticate")
  public ResponseEntity<?> authenticatePiUser(
           @Validated @RequestBody AuthMetaData metaData) throws JsonProcessingException {
    return ResponseEntity.ok(authService.authenticate( metaData));
  }

  @PostMapping(value = "/logout")
  public ResponseEntity<?> logout(
          @RequestBody String session) throws JsonProcessingException {
    return ResponseEntity.ok(authService.logout(session));
  }
}
