package com.delinea.platform.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the response from Secret Server containing a list of Vaults.
 */
public class VaultsResponseModel {

    @JsonProperty("vaults")
    private List<Vault> vaults = new ArrayList<>();

    /**
     * Returns a copy of the vaults list.
     *
     * @return a list of {@link Vault} objects
     */
    public List<Vault> getVaults() {
        return new ArrayList<>(vaults);
    }

    /**
     * Sets the vaults list.
     *
     * @param vaults the list of {@link Vault} objects to set
     */
    public void setVaults(List<Vault> vaults) {
        this.vaults = (vaults == null) ? new ArrayList<>() : new ArrayList<>(vaults);
    }

    /**
     * Represents a Vault object.
     */
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

        /**
         * Returns a copy of the connection object.
         *
         * @return a copy of the {@link Connection}, or {@code null} if not set
         */
        public Connection getConnection() {
            return (connection == null) ? null : new Connection(connection);
        }

        /**
         * Sets the connection information for this Vault.
         *
         * @param connection the {@link Connection} object to set
         */
        public void setConnection(Connection connection) {
            this.connection = (connection == null) ? null : new Connection(connection);
        }
    }

    /**
     * Represents connection information for a Vault.
     */
    @Data
    public static class Connection {
        @JsonProperty("url")
        private String url;
        @JsonProperty("oAuthProfileId")
        private String oAuthProfileId;

        /** Constructor */
        public Connection() {}

        /**
         * Copy constructor that creates a new Connection from another instance.
         *
         * @param other the other {@link Connection} object to copy from
         */
        public Connection(Connection other) {
            if (other != null) {
                this.url = other.url;
                this.oAuthProfileId = other.oAuthProfileId;
            }
        }
    }
}
