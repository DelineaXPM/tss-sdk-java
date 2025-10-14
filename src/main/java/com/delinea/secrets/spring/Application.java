package com.delinea.secrets.spring;

import static java.lang.Integer.parseInt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.delinea.secrets.server.spring.SecretServer;

/**
 * Main entry point for the Delinea Secrets SDK Spring Boot application.
 * <p>
 * This class initializes the Spring context and triggers the {@link SecretServer}
 * bean to fetch a secret using the configured secret ID.
 */
@SpringBootApplication
@ComponentScan("com.delinea.secrets.server.spring")
public class Application {
    private final Logger log = LoggerFactory.getLogger(Application.class);

    @Value("${secret.id:#null}")
    private String secretId;

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Runs after the application context is initialized.
     * Uses the {@link SecretServer} bean to fetch the secret by ID.
     *
     * @param secretServer injected SecretServer bean
     * @return a CommandLineRunner that executes after startup
     * @throws Exception if fetching the secret fails
     */
    @Bean
    public CommandLineRunner runServer(final SecretServer secretServer) throws Exception {
        return args -> {
            log.info("Running with args: \"{}\"; getSecret({}) -> {}", args, secretId,
                    secretServer.getSecret(parseInt(secretId)).toString());
        };
    }
}
