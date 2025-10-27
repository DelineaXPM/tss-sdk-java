package com.delinea.platform.model;

/** Represents the result of an authentication attempt. */
public class AuthResult {
    private boolean success;
    private String token;
    private String errorMessage;
    
	/**
	 * Creates a new authentication result.
	 *
	 * @param success      true if authentication succeeded
	 * @param token        the access token
	 * @param errorMessage the error message if authentication failed
	 */
    public AuthResult(boolean success, String token, String errorMessage) {
        this.success = success;
        this.token = token;
        this.errorMessage = errorMessage;
    }

    /** @return the authentication token */
    public boolean isSuccess() { return success; }
    
    /** @return the authentication token */
    public String getToken() { return token; }
    
    /** @return the error message, or null if none */
    public String getErrorMessage() { return errorMessage; }
}