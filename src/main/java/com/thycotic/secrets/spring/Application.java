package com.thycotic.secrets.spring;

import static java.lang.Integer.parseInt;

import com.thycotic.secrets.server.spring.SecretServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.thycotic.secrets.server.spring")
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
