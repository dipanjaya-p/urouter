package com.utt.urouter.exception;

import org.springframework.http.HttpStatus;

public class GenericException extends RuntimeException {

  protected String error;
  protected String errMessage;
  protected HttpStatus httpStatus;

  public GenericException(String message, HttpStatus status) {
    super(message);
    this.errMessage = message;
    this.httpStatus = status;
  }

  public GenericException(String message, HttpStatus status, String err) {
    super(message);
    this.errMessage = message;
    this.httpStatus = status;
    this.error = err;
  }

  public String getErrMessage() {
    return errMessage;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public String getError() {
    return error;
  }
}
