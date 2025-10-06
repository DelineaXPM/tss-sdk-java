package com.delinea.secrets.server.spring;

/**
 * Defines the contract for authentication services used by the SDK.
 */
public interface IAuthenticationService {
	 AuthenticationModel authenticateAsync(AuthenticationModel authModel) throws Exception;
}
