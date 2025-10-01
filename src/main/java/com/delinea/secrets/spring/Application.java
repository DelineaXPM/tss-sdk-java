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

import com.delinea.secrets.server.spring.AuthenticationModel;
import com.delinea.secrets.server.spring.AuthenticationService;
import com.delinea.secrets.server.spring.PlatformLogin;
import com.delinea.secrets.server.spring.SecretServer;

@SpringBootApplication
@ComponentScan("com.delinea.secrets.server.spring")
public class Application {
    private final Logger log = LoggerFactory.getLogger(Application.class);

    @Value("${secret.id:#null}")
    private String secretId;

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner runServer(final SecretServer secretServer) throws Exception {
        return args -> {
            log.info("running with args \"{}\"; getSecret({}) -> {}", args, secretId,
                    secretServer.getSecret(parseInt(secretId)).toString());
        };
    }
}