package com.delinea.platform.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/** Represents OAuth2 tokens returned from platform authentication. */
@Data
public class OAuthTokens {
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("refresh_token")
	private String refreshToken;
	@JsonProperty("id_token")
	private String idToken;
	@JsonProperty("token_type")
	private String tokenType;
	@JsonProperty("expires_in")
	private int expiresIn;
	@JsonProperty("session_expires_in")
	private int sessionExpiresIn;
	@JsonProperty("scope")
	private String scope;
	}