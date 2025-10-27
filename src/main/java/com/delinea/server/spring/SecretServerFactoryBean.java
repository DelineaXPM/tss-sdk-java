package com.delinea.server.spring;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.delinea.platform.service.AuthenticationService;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Factory Bean that creates and configures a {@link SecretServer} instance.
 * Handles both Secret Server and Platform authentication modes.
 */
@Component
public class SecretServerFactoryBean implements FactoryBean<SecretServer>, InitializingBean {

    // Nested class representing OAuth token response
    static class AccessGrant {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private int expiresIn;

        @JsonProperty("access_token")
        public String getAccessToken() { return accessToken; }

        @JsonProperty("expires_in")
        public int getExpiresIn() { return expiresIn; }

        @JsonProperty("refresh_token")
        public String getRefreshToken() { return refreshToken; }

        @JsonProperty("token_type")
        public String getTokenType() { return tokenType; }
    }

    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String AUTHORIZATION_TOKEN_TYPE = "Bearer";
    private static final int SDK_CLIENT_AUTH_MODE = 1;
    private static final int DEFAULT_AUTH_MODE = 0;

    @Autowired
    private Environment environment;

    private RestTemplate restTemplate = new RestTemplate(); // Injected, proxy-aware RestTemplate

    private final AuthenticationService authenticationService = new AuthenticationService();
	private static final String GRANT_REQUEST_USERNAME_PROPERTY = "username";
	private static final String GRANT_REQUEST_PASSWORD_PROPERTY = "password";
	private static final String GRANT_REQUEST_GRANT_TYPE_PROPERTY = "grant_type";
	private static final String GRANT_REQUEST_GRANT_TYPE = "password";
    private String API_VERSION;
    private String ruleName;
    private String onboardingKey;
    private int authenticationMode;
    private String clientId;
    private String clientSecret;
    private String serverUrl;
    private String secreterverUrl;
    private String serverUsername;
    private String serverPassword;

    /**
     * Validate configuration and initialize key properties.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        String authModeStr = environment.getProperty("authentication_mode");
        authenticationMode = StringUtils.hasText(authModeStr)
                ? Integer.parseInt(authModeStr)
                : DEFAULT_AUTH_MODE;

        this.API_VERSION = environment.getProperty("api.version", "v1");
        this.serverUrl = environment.getProperty("server.url");
        Assert.state(StringUtils.hasText(serverUrl), "server.url must be set.");

        if (authenticationMode == DEFAULT_AUTH_MODE) {
            this.serverUsername = environment.getProperty("server.username");
            this.serverPassword = environment.getProperty("server.password");
            Assert.state(StringUtils.hasText(serverUsername) && StringUtils.hasText(serverPassword),
                    "server.username and server.password must be set when authenticationMode = 0");
        } else if (authenticationMode == SDK_CLIENT_AUTH_MODE) {
            this.ruleName = environment.getProperty("rule.name");
            this.onboardingKey = environment.getProperty("onboarding.key");
            Assert.state(StringUtils.hasText(ruleName) && StringUtils.hasText(onboardingKey),
                    "rule.name and onboarding.key must be set when authenticationMode = 1");
        }
    }

    /**
     * Build and return a configured SecretServer bean.
     */
    @Override
    public SecretServer getObject() throws Exception {
        AccessGrant accessGrant = getAccessGrant();

        SecretServer secretServer = new SecretServer();
        secretServer.setUriTemplateHandler(new DefaultUriBuilderFactory(secreterverUrl + "/api/" + API_VERSION));

        ClientHttpRequestFactory factory = new InterceptingClientHttpRequestFactory(
                restTemplate.getRequestFactory(),
                Arrays.asList((request, body, execution) -> {
                    request.getHeaders().add(
                            AUTHORIZATION_HEADER_NAME,
                            String.format("%s %s", AUTHORIZATION_TOKEN_TYPE, accessGrant.getAccessToken())
                    );
                    return execution.execute(request, body);
                })
        );

        secretServer.setRequestFactory(factory);
        return secretServer;
    }

    /**
     * Determines which authentication flow to use and fetches an access token.
     */
    private AccessGrant getAccessGrant() throws Exception {
        if (authenticationMode == DEFAULT_AUTH_MODE) {
            AuthenticationModel authModel = authenticateUser();
            if (authModel == null) {
                throw new IllegalStateException("Invalid Server URL or credentials");
            }

            if (authModel.isPlatformLogin()) {
                AccessGrant accessGrant = new AccessGrant();
                accessGrant.accessToken = authModel.getToken();
                this.secreterverUrl = authModel.getVaultURL();
                return accessGrant;
            } else {
                return getTokenUsingSScred();
            }
        } else {
            this.secreterverUrl = serverUrl;
            registerSDKClient();
            return getTokenUsingSDKClient();
        }
    }

    private AuthenticationModel authenticateUser() throws Exception {
        AuthenticationModel authModel = authenticationService
                .authenticateAsync(new AuthenticationModel(serverUsername, serverPassword, serverUrl));
        if (authModel != null) {
            this.secreterverUrl = authModel.isPlatformLogin() ? authModel.getVaultURL() : serverUrl;
        }
        return authModel;
    }

    private AccessGrant getTokenUsingSScred() {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add(GRANT_REQUEST_USERNAME_PROPERTY, serverUsername);
		request.add(GRANT_REQUEST_PASSWORD_PROPERTY, serverPassword);
		request.add(GRANT_REQUEST_GRANT_TYPE_PROPERTY, GRANT_REQUEST_GRANT_TYPE);

        return restTemplate.postForObject(
                secreterverUrl + "/oauth2/token".replaceAll("/*$", ""),
                request,
                AccessGrant.class
        );
    }

    private void registerSDKClient() throws UnknownHostException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("Description", String.format(
                "Machine: %s, OS: %s - %s %s",
                InetAddress.getLocalHost().getHostName(),
                System.getProperty("os.arch"),
                System.getProperty("java.version"),
                System.getProperty("sun.arch.data.model")
        ));
        payload.put("Name", InetAddress.getLocalHost().getHostName());
        payload.put("OnboardingKey", onboardingKey);
        payload.put("RuleName", ruleName);
        payload.put("ClientId", UUID.randomUUID().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                serverUrl + "/api/" + API_VERSION + "/sdk-client-accounts",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            clientId = (String) body.get("clientId");
            clientSecret = (String) body.get("clientSecret");
        }
    }

    private AccessGrant getTokenUsingSDKClient() throws UnsupportedEncodingException {
        String body = "client_id=sdk-client-" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&grant_type=client_credentials";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<AccessGrant> response = restTemplate.exchange(
                serverUrl + "/oauth2/token",
                HttpMethod.POST,
                entity,
                AccessGrant.class
        );

        return response.getStatusCode() == HttpStatus.OK ? response.getBody() : null;
    }

    @Override
    public Class<?> getObjectType() {
        return SecretServer.class;
    }
}
