package com.delinea.platform.service;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Autowired
    private Environment environment;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        String proxyHost = environment.getProperty("proxy.host");
        String proxyPortStr = environment.getProperty("proxy.port");
        String proxyUsername = environment.getProperty("proxy.username");
        String proxyPassword = environment.getProperty("proxy.password");

        // Validate proxy configuration
        if (proxyHost == null || proxyHost.isBlank() || proxyPortStr == null || proxyPortStr.isBlank()) {
            return builder.build();
        }
        System.out.println("proxy configured");
        int proxyPort;
        try {
            proxyPort = Integer.parseInt(proxyPortStr);
            if (proxyPort <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid proxy.port value: " + proxyPortStr, e);
        }

        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();

        if (proxyUsername != null && !proxyUsername.isBlank()) {
        	 System.out.println("Using authenticated proxy details");
            credsProvider.setCredentials(
                new AuthScope(proxyHost, proxyPort),
                new UsernamePasswordCredentials(proxyUsername, proxyPassword != null ? proxyPassword.toCharArray() : new char[0])
            );
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setProxy(proxy)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }
}
