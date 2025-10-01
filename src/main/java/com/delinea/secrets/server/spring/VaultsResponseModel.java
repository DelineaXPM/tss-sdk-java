package com.delinea.secrets.server.spring;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class VaultsResponseModel {
	@JsonProperty("vaults")
	private List<Vault> vaults;

	@Data
	public static class Vault {
		@JsonProperty("vaultId")
		private String vaultId;
		@JsonProperty("name")
		private String name;
		@JsonProperty("type")
		private String type;
		@JsonProperty("isDefault")
		private boolean isDefault;
		@JsonProperty("isGlobalDefault")
		private boolean isGlobalDefault;
		@JsonProperty("isActive")
		private boolean isActive;
		@JsonProperty("connection")
		private Connection connection;
	}

	@Data
	public static class Connection {
		@JsonProperty("url")
		private String url;
		@JsonProperty("oAuthProfileId")
		private String oAuthProfileId;
	}
}
