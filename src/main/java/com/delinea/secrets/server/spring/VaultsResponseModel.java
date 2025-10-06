package com.delinea.secrets.server.spring;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class VaultsResponseModel {

    @JsonProperty("vaults")
    private List<Vault> vaults = new ArrayList<>();

    public List<Vault> getVaults() {
        return new ArrayList<>(vaults);
    }

    public void setVaults(List<Vault> vaults) {
        this.vaults = (vaults == null) ? new ArrayList<>() : new ArrayList<>(vaults);
    }

    @Setter
    @Getter
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

        public Connection getConnection() {
            return (connection == null) ? null : new Connection(connection);
        }

        public void setConnection(Connection connection) {
            this.connection = (connection == null) ? null : new Connection(connection);
        }
    }

    @Data
    public static class Connection {
        @JsonProperty("url")
        private String url;
        @JsonProperty("oAuthProfileId")
        private String oAuthProfileId;

        public Connection() {}

        public Connection(Connection other) {
            if (other != null) {
                this.url = other.url;
                this.oAuthProfileId = other.oAuthProfileId;
            }
        }
    }
}
