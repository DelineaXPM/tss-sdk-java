package com.delinea.platform.service;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.delinea.platform.model.ServerResponseModel;
import com.delinea.server.spring.AuthenticationModel;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles authentication logic for both Secret Server and Platform.
 * <p>
 * Determines which type of login to perform based on health check endpoints.
 */
@Service
public class AuthenticationService implements IAuthenticationService {
	private RestTemplate restTemplate ;
	private PlatformLogin platformLogin = new PlatformLogin();
	
	 public void setRestTemplate(RestTemplate restTemplate) {
	        this.restTemplate = restTemplate;
	        this.platformLogin.setRestTemplate(restTemplate);
	    }

	 /**
     * Determines whether to authenticate against Secret Server or Platform
     * by performing health checks, and proceeds accordingly.
     *
     * @param authModel the authentication details
     * @return populated {@link AuthenticationModel} with token and state
     * @throws Exception if authentication fails or endpoints are unreachable
     */
    @Override
    public AuthenticationModel authenticateAsync(AuthenticationModel authModel) throws Exception {
        try {
            String platformHealthCheckUrl = authModel.getServerURL().replaceFirst("/$", "") + "/health";
            String ssHealthCheckUrl = authModel.getServerURL().replaceFirst("/$", "") + "/api/v1/healthcheck";
            boolean isSsHealthy = checkJsonResponseAsync(ssHealthCheckUrl);
            if (isSsHealthy) {
                authModel.setPlatformLogin(false);
            } else {
                boolean isPlatformHealthy = checkJsonResponseAsync(platformHealthCheckUrl);
                if (isPlatformHealthy) {
                    authModel.setPlatformLogin(true);
                    return platformLogin.platformAuthentication(authModel);
                } 
            }
            return authModel;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private boolean checkJsonResponseAsync(String url) throws IOException {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                return false;
            }
            String responseBody = response.getBody();
            if (responseBody != null && !responseBody.isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ServerResponseModel jsonResponse =
                            objectMapper.readValue(responseBody, ServerResponseModel.class);
                    return jsonResponse != null && jsonResponse.isHealthy();
                } catch (IOException e) {
                    return responseBody.contains("Healthy");
                }
            }
            return false;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            return false;
        } catch (Exception ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }
}
