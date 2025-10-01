package com.delinea.secrets.server.spring;

public interface IAuthenticationService {
	 AuthenticationModel authenticateAsync(AuthenticationModel authModel) throws Exception;
}
