package com.utt.urouter.model;

public class JwtResponse {
	
	private final String jwtToken;
	private final String role;

	
	public JwtResponse(String jwtToken,String role) {
		this.jwtToken = jwtToken;
		this.role = role;

	}

	public String getJwtToken() {
		return jwtToken;
	}
	public String getRole() {
		return role;
	}

	@Override
	public String toString() {
		return "JwtResponse [jwtToken=" + jwtToken + ", role=" + role + "]";
	}
}
