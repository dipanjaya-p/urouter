package com.utt.urouter.model;

public class GenericResponse {
	
	private String status;
	private String message;
	private Object data;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "GenericResponse [status=" + status + ", message=" + message + ", data=" + data + "]";
	}
}
