package com.delinea.secrets.server.spring;

public class AuthResult {
    private boolean success;
    private String token;
    private String errorMessage;

    public AuthResult(boolean success, String token, String errorMessage) {
        this.success = success;
        this.token = token;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() { return success; }
    public String getToken() { return token; }
    public String getErrorMessage() { return errorMessage; }
}