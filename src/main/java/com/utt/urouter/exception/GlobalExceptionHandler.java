package com.utt.urouter.exception;

import com.utt.urouter.model.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(GenericException.class)
  public ResponseEntity<ExceptionResponse> handleGenericException(
      GenericException genericException, WebRequest request) {
    return ResponseEntity.status(genericException.getHttpStatus())
        .body(
            ExceptionResponse.builder()
                .message(genericException.getErrMessage())
                .status(genericException.getHttpStatus().value())
                .path(request.getDescription(false).replace("uri=/", ""))
                .error(
                    genericException.getError() == null
                        ? genericException.getHttpStatus().name()
                        : genericException.getError())
                .timestamp(LocalDateTime.now())
                .build());
  }

  @ExceptionHandler(ResourceAccessException.class)
  public ResponseEntity<ExceptionResponse> handleResourceAccessException(
          ResourceAccessException resourceAccessException, WebRequest webRequest) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                    ExceptionResponse.builder()
                            .message("FAILED_TO_ACCESS_REMOTE_RESOURCE")
                            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                            .path(webRequest.getDescription(false).replace("uri=/", ""))
                            .error("Failed to access remote resource")
                            .timestamp(LocalDateTime.now())
                            .build());
  }
}
