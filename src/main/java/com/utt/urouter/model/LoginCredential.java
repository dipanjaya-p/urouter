package com.utt.urouter.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class LoginCredential implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8308355139565734947L;
	
	@NotNull
	private String username;
	
	@NotNull
	private String password;
	
	public LoginCredential(String userName, String password) {
		this.username = userName;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String userName) {
		this.username = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
