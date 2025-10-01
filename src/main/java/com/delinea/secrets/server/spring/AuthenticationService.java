package com.delinea.secrets.server.spring;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuthenticationService implements IAuthenticationService {
	//@Autowired
	private RestTemplate restTemplate = new RestTemplate();
	//@Autowired
	private PlatformLogin platformLogin = new PlatformLogin();


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
                } else {
                    return null;
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
