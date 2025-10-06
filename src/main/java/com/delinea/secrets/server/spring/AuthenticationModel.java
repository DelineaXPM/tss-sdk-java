package com.delinea.secrets.server.spring;

import java.time.ZonedDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the authentication model used to connect to
 * Delinea Secret Server or Delinea Platform.
 * <p>
 * Contains credential and session-related information.
 */
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

    /**
     * Constructor used to represent authentication failure or error state.
     *
     * @param errorMessage error description
     * @param isPlatformLogin true if related to platform login
     */
    public AuthenticationModel(String errorMessage, boolean isPlatformLogin) {
        this.error = errorMessage;
        this.isPlatformLogin = isPlatformLogin;
    }

    /**
     * Constructor used for normal authentication requests.
     *
     * @param userName username for authentication
     * @param password password for authentication
     * @param serverURL server URL
     */
    public AuthenticationModel(String userName, String password, String serverURL) {
        this.userName = userName;
        this.password = password;
        this.serverURL = serverURL;
    }
}
