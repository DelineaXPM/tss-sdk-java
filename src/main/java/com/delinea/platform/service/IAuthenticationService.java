package com.delinea.platform.service;

import com.delinea.server.spring.AuthenticationModel;

/**
 * Defines the contract for authentication services used by the SDK.
 */
public interface IAuthenticationService {
	 AuthenticationModel authenticateAsync(AuthenticationModel authModel) throws Exception;
}
