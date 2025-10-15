package com.delinea.secrets.server.spring;

import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.Arrays;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.util.UriBuilderFactory;

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
    public static final String DEFAULT_API_URL_TEMPLATE = "https://%s.secretservercloud.%s/api/v1",
            DEFAULT_OAUTH2_TOKEN_URL_TEMPLATE = "https://%s.secretservercloud.%s/oauth2/token", DEFAULT_TLD = "com";

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
    
    // ======== Proxy Properties ========
    @Value("${secret_server.proxy.host:#{null}}")
    private String proxyHost;

    @Value("${secret_server.proxy.port:0}")
    private int proxyPort;

    @Value("${secret_server.proxy.username:#{null}}")
    private String proxyUsername;

    @Value("${secret_server.proxy.password:#{null}}")
    private String proxyPassword;

    @Value("${secret_server.api_root_url_template:" + DEFAULT_API_URL_TEMPLATE + "}")
    private String apiRootUrlTemplate;

    @Value("${secret_server.api_root_url:#{null}}")
    private String apiRootUrl;

    @Value("${secret_server.oauth2.username}")
    private String username;

    @Value("${secret_server.oauth2.password}")
    private String password;

    @Value("${secret_server.oauth2.token_url_template:" + DEFAULT_OAUTH2_TOKEN_URL_TEMPLATE + "}")
    private String tokenUrlTemplate;

    @Value("${secret_server.oauth2.token_url:#{null}}")
    private String tokenUrl;

    @Value("${secret_server.tenant:#{null}}")
    private String tenant;

    @Value("${secret_server.tld:" + DEFAULT_TLD + "}")
    private String tld;

    @Autowired(required = false)
    private ClientHttpRequestFactory requestFactory;

    private UriBuilderFactory uriBuilderFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(StringUtils.hasText(apiRootUrlTemplate) && StringUtils.hasText(tokenUrlTemplate)
                || StringUtils.hasText(apiRootUrl) && StringUtils.hasText(tokenUrl) || StringUtils.hasText(tenant),
                "Either secret_server.tenant or both of either secret_server.api_root_url and secret_server.oauth2.token_url or secret_server.api_root_url_template and secret_server.oauth2.token_url_template must be set.");

        tld = tld.replaceAll("^\\.*(.*?)\\.*$", "$1");
        uriBuilderFactory = new DefaultUriBuilderFactory(fromUriString(
                StringUtils.hasText(tenant) ? String.format(apiRootUrlTemplate.replaceAll("/*$", ""), tenant, tld)
                        : apiRootUrl.replaceAll("/*$", "")));
        if (requestFactory == null)
            requestFactory = createRequestFactoryWithProxy();
    }

    /**
     * Builds an HTTP request factory with optional proxy support.
     */
    private ClientHttpRequestFactory createRequestFactoryWithProxy() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        if (StringUtils.hasText(proxyHost) && proxyPort > 0) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            factory.setProxy(proxy);

            System.out.println("[SecretServerFactoryBean] Using proxy: " + proxyHost + ":" + proxyPort);

            if (StringUtils.hasText(proxyUsername)) {
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                proxyUsername,
                                proxyPassword != null ? proxyPassword.toCharArray() : new char[0]);
                    }
                });
                System.out.println("[SecretServerFactoryBean] Proxy authentication set for user: " + proxyUsername);
            }
        } else {
            System.out.println("[SecretServerFactoryBean] No proxy configured.");
        }

        return factory;
    }

    private AccessGrant getAccessGrant() {
        final MultiValueMap<String, String> request = new LinkedMultiValueMap<String, String>();

        request.add(GRANT_REQUEST_USERNAME_PROPERTY, username);
        request.add(GRANT_REQUEST_PASSWORD_PROPERTY, password);
        request.add(GRANT_REQUEST_GRANT_TYPE_PROPERTY, GRANT_REQUEST_GRANT_TYPE);
        String url = StringUtils.hasText(tenant)
                ? String.format(tokenUrlTemplate.replaceAll("/*$", ""), tenant, tld)
                : tokenUrl.replaceAll("/*$", "");

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate.postForObject(url, request, AccessGrant.class);
    }
    
    @Override
    public SecretServer getObject() throws Exception {
        final SecretServer secretServer = new SecretServer();

        secretServer.setUriTemplateHandler(uriBuilderFactory);
        secretServer.setRequestFactory(new InterceptingClientHttpRequestFactory(
                requestFactory,
                Arrays.asList((request, body, execution) -> {
                    request.getHeaders().add("Authorization",
                            "Bearer " + getAccessGrant().accessToken);
                    return execution.execute(request, body);
                })));

        return secretServer;
    }

    @Override
    public Class<?> getObjectType() {
        return SecretServer.class;
    }
}
