package com.delinea.secrets.server.spring;

import java.time.ZonedDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthenticationModel {
	private String userName;
	private String password;
	private String serverURL;
	private String token;
	private ZonedDateTime tokenExpiration;
	private boolean isPlatformLogin;
	private String error;
	private String vaultURL;
	private String vaultType;

	public AuthenticationModel(String errorMessage, boolean isPlatformLogin) {
		this.error = errorMessage;
		this.isPlatformLogin = isPlatformLogin;
	}

	public AuthenticationModel(String userName, String password, String serverURL) {
		super();
		this.userName = userName;
		this.password = password;
		this.serverURL = serverURL;
	}
}
