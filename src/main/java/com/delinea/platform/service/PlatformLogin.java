package com.delinea.platform.service;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.delinea.platform.model.OAuthTokens;
import com.delinea.platform.model.VaultsResponseModel;
import com.delinea.platform.model.VaultsResponseModel.Vault;
import com.delinea.server.spring.AuthenticationModel;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Handles authentication against Delinea Platform and vault retrieval. */
@Component
public class PlatformLogin {
    private RestTemplate restTemplate;
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Authenticates the user on the Delinea Platform and retrieves vault info.
     *
     * @param authModel authentication credentials
     * @return updated AuthenticationModel with token and vault details
     * @throws Exception if authentication or vault retrieval fails
     */
    public AuthenticationModel platformAuthentication(AuthenticationModel authModel) throws Exception {
        try {
            // 1. Get Access Token
            ResponseEntity<String> tokenResponse = getAccessToken(authModel);
            if (tokenResponse.getStatusCode() != HttpStatus.OK) {
                return handleErrorResponse(tokenResponse.getBody());
            }

            OAuthTokens authResponse = new ObjectMapper()
                    .readValue(tokenResponse.getBody(), OAuthTokens.class);

            if (authResponse == null) {
                return handleErrorResponse("Unable to authenticate for user " + authModel.getUserName()
                        + " on server " + authModel.getServerURL());
            }

            authModel.setToken(authResponse.getAccessToken());
            authModel.setTokenExpiration(ZonedDateTime.now().plusSeconds(authResponse.getExpiresIn()));

            // 2. Get Vault
            ResponseEntity<String> vaultResponse = getVault(authModel, authResponse.getAccessToken());
            if (vaultResponse.getStatusCode() != HttpStatus.OK) {
                return handleErrorResponse(vaultResponse.getBody());
            }

            VaultsResponseModel vResponse = new ObjectMapper()
                    .readValue(vaultResponse.getBody(), VaultsResponseModel.class);

            if (vResponse == null) {
                return handleErrorResponse("Unable to fetch vaults from server " + authModel.getServerURL());
            }

            Optional<Vault> vault = vResponse.getVaults().stream()
                    .filter(x -> x.isDefault() && x.isActive())
                    .findFirst();

            if (vault.isPresent()) {
                authModel.setVaultURL(vault.get().getConnection().getUrl());
                authModel.setVaultType(vault.get().getType());
            } else {
                return handleErrorResponse("Unable to fetch vaults from server " + authModel.getServerURL());
            }

            return authModel;
        } catch (Exception ex) {
            throw new Exception("Error occurred in PlatformAuthentication:\n" + ex.getMessage(), ex);
        }
    }

    private AuthenticationModel handleErrorResponse(String errorMessage) {
        return new AuthenticationModel(errorMessage, true);
    }

    /**
     * Authenticates the user on the Delinea Platform and retrieves vault info.
     *
     * @param authModel authentication credentials
     * @return updated AuthenticationModel with token and vault details
     * @throws Exception if authentication or vault retrieval fails
     */
    private ResponseEntity<String> getAccessToken(AuthenticationModel authModel) {
        String apiUrl = authModel.getServerURL().replaceAll("/$", "") + "/identity/api/oauth2/token/xpmplatform";
        String scope = "xpmheadless";

        if (authModel.getServerURL() == null) {
            throw new IllegalArgumentException("Missing required environment variables");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", authModel.getUserName());
        body.add("client_secret", authModel.getPassword());
        body.add("scope", scope);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);
    }

    public ResponseEntity<String> getVault(AuthenticationModel authModel, String token) {
        String apiUrl = authModel.getServerURL().replaceAll("/$", "") + "/vaultbroker/api/vaults";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, String.class);
    }
}
