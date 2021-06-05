package com.odazie.faunadataapiandjava;

import com.faunadb.client.FaunaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.net.MalformedURLException;

@SpringBootApplication
public class FaunaDataApiAndJavaApplication {

    @Value("${fauna-db.secret}")
    private String adminKey;

    public static void main(String[] args) {
        SpringApplication.run(FaunaDataApiAndJavaApplication.class, args);
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public FaunaClient faunaConfiguration() throws MalformedURLException {

        return FaunaClient.builder()
                .withSecret(adminKey)
                .build();
    }

}
