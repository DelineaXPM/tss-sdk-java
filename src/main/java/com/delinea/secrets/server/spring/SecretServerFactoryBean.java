package com.delinea.secrets.server.spring;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Creates an initializes a {@link SecretServer} object using Spring application
 * properties.
 *
 * <p>
 * The required properties are:
 * <ul>
 * <li>{@code secret_server.tenant} when accessing Secret Server Cloud <u>or</u>
 * <li>{@code secret_server.api_root_url} <u>and</u>
 * {@code secret_server.oauth2.token_url} for on-premises servers
 * <li>{@code secret_server.oauth2.username}
 * <li>{@code secret_server.oauth2.password}
 * </ul>
 *
 * <p>
 * The SDK gets these properties from the Spring Boot
 * {@code application.properties} file in {@code src/main/resources} by default.
 *
 */
@Component
public class SecretServerFactoryBean implements FactoryBean<SecretServer>, InitializingBean {
	static class AccessGrant {
		private String accessToken, refreshToken, tokenType;
		private int expiresIn;

		@JsonProperty("access_token")
		public String getAccessToken() {
			return accessToken;
		}

		@JsonProperty("expires_in")
		public int getExpiresIn() {
			return expiresIn;
		}

		@JsonProperty("refresh_token")
		public String getRefreshToken() {
			return refreshToken;
		}

		@JsonProperty("token_type")
		public String getTokenType() {
			return tokenType;
		}
	};

	private static final String GRANT_REQUEST_USERNAME_PROPERTY = "username";

	private static final String GRANT_REQUEST_PASSWORD_PROPERTY = "password";

	private static final String GRANT_REQUEST_GRANT_TYPE_PROPERTY = "grant_type";

	private static final String GRANT_REQUEST_GRANT_TYPE = "password";

	private static final String AUTHORIZATION_HEADER_NAME = "Authorization";

	private static final String AUTHORIZATION_TOKEN_TYPE = "Bearer";
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

	@Autowired(required = false)
	private ClientHttpRequestFactory requestFactory;

	@Autowired
	private Environment environment;
	private AuthenticationService authenticationService = new AuthenticationService();
	private static final int SDK_CLIENT_AUTH_MODE = 1;
	private static final int DEFAULT_AUTH_MODE = 0;
	
	 /**
     * After properties are set, validate configuration and initialize requestFactory if null.
     */
	@Override
	public void afterPropertiesSet() throws Exception {
		String authModeStr = environment.getProperty("authentication_mode");
		if (StringUtils.hasText(authModeStr)) {
			authenticationMode = Integer.parseInt(authModeStr);
		} else {
			authenticationMode = DEFAULT_AUTH_MODE;
		}
		String apiVersionProp = environment.getProperty("api_version");
		this.API_VERSION = (apiVersionProp != null && !apiVersionProp.isEmpty()) ? apiVersionProp : "v1";
		if (authenticationMode == DEFAULT_AUTH_MODE) {
			this.serverUsername = environment.getProperty("server_username");
			this.serverPassword = environment.getProperty("server_password");
			Assert.state(StringUtils.hasText(serverUsername) && StringUtils.hasText(serverPassword),
				"server.username and secret.password must be set when authenticationMode is 0");
		} else {
			this.ruleName = environment.getProperty("rule_name");
			this.onboardingKey = environment.getProperty("onboarding_key");
			Assert.state(StringUtils.hasText(ruleName) && StringUtils.hasText(onboardingKey),
					"rule_name and onboarding_key must be set when authenticationMode is 1");
		}
		this.serverUrl = environment.getProperty("server_url");
		Assert.state(StringUtils.hasText(serverUrl),
				"server_url must be set.");

		if (requestFactory == null)
			requestFactory = new SimpleClientHttpRequestFactory();
	}

	private AccessGrant getAccessGrant() throws UnknownHostException, UnsupportedEncodingException,Exception {
		if (authenticationMode == DEFAULT_AUTH_MODE) {
			AuthenticationModel authenticationModel = isPlatfromOrSS();
			if(authenticationModel != null) {
				if (authenticationModel.isPlatformLogin()) {
					AccessGrant accessGrant = new AccessGrant();
					accessGrant.accessToken = authenticationModel.getToken();
					this.secreterverUrl = authenticationModel.getVaultURL();
					return accessGrant;
				} else {
					return getTokenUsingSScred();
				}
			}else {
				throw new NullPointerException("Invalid Server URL ");
			}
		}else {
			this.secreterverUrl = serverUrl;
			setSDKClientCred();
			return getTokenUsingSDKClient();
		}
	}
	
	private AuthenticationModel isPlatfromOrSS() throws Exception {
		AuthenticationModel authenticationModel = authenticationService
				.authenticateAsync(new AuthenticationModel(serverUsername, serverPassword, serverUrl));
		if (authenticationModel != null) {
			if (authenticationModel.isPlatformLogin()) {
				return authenticationModel;
			} else {
				this.secreterverUrl = serverUrl;
				return authenticationModel;
			}
		}
		return null;
	}

	private AccessGrant getTokenUsingSScred() {
		final MultiValueMap<String, String> request = new LinkedMultiValueMap<String, String>();

		request.add(GRANT_REQUEST_USERNAME_PROPERTY, serverUsername);
		request.add(GRANT_REQUEST_PASSWORD_PROPERTY, serverPassword);
		request.add(GRANT_REQUEST_GRANT_TYPE_PROPERTY, GRANT_REQUEST_GRANT_TYPE);
		return new RestTemplate().postForObject(secreterverUrl+"/oauth2/token".replaceAll("/*$", ""),
				request, AccessGrant.class);
	}

	private AccessGrant getTokenUsingSDKClient() throws UnsupportedEncodingException {
		String body = "client_id=sdk-client-" + URLEncoder.encode(clientId, StandardCharsets.UTF_8.toString())
				+ "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8.toString()) + "&grant_type=client_credentials";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<String> entity = new HttpEntity<>(body, headers);
		ResponseEntity<AccessGrant> response = new RestTemplate().exchange(serverUrl+ "/oauth2/token", HttpMethod.POST, entity,
				AccessGrant.class);

		if (response.getStatusCode() == HttpStatus.OK) {
			return response.getBody();
		}
			return null;
	}

	private void setSDKClientCred() throws UnknownHostException {
		Map<String, Object> payload = new HashMap<>();
		payload.put("Description",
				String.format("Machine: %s, OS: %s - %s %s", InetAddress.getLocalHost().getHostName(),
						System.getProperty("os.arch"), System.getProperty("java.version"),
						System.getProperty("sun.arch.data.model")));
		payload.put("Name", InetAddress.getLocalHost().getHostName());
		payload.put("OnboardingKey", onboardingKey);
		payload.put("RuleName", ruleName);
		payload.put("ClientId", UUID.randomUUID().toString());

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
		ResponseEntity<Map<String, Object>> response = new RestTemplate().exchange(serverUrl+"/api/"+API_VERSION+"/sdk-client-accounts", HttpMethod.POST, entity,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		if (response.getStatusCode() == HttpStatus.OK) {
			Map<String, Object> responseBody = response.getBody();
			if (responseBody != null ) {
				clientId = (String) responseBody.get("clientId");
				clientSecret = (String) responseBody.get("clientSecret");
			}
		}
	}

	@Override
	public SecretServer getObject() throws Exception {
	    AccessGrant accessGrant = getAccessGrant();
	    final SecretServer secretServer = new SecretServer();
	    secretServer.setUriTemplateHandler(
	        new DefaultUriBuilderFactory(secreterverUrl + "/api/"+API_VERSION)
	    );

	    secretServer.setRequestFactory(
	        new InterceptingClientHttpRequestFactory(requestFactory,
	            Arrays.asList((request, body, execution) -> {
	                request.getHeaders().add(AUTHORIZATION_HEADER_NAME,
	                        String.format("%s %s", AUTHORIZATION_TOKEN_TYPE, accessGrant.accessToken));
	                return execution.execute(request, body);
	            })));

	    return secretServer;
	}

	@Override
	public Class<?> getObjectType() {
		return SecretServer.class;
	}
}
