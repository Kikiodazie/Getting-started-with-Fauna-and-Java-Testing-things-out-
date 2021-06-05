package com.odazie.faunadataapiandjava;

import com.faunadb.client.FaunaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FaunaDataApiAndJavaApplication {

    @Value("${fauna-db.adminKey}")
    private String adminKey;

    public static void main(String[] args) {
        SpringApplication.run(FaunaDataApiAndJavaApplication.class, args);
    }

    @Bean
    public FaunaClient faunaConfiguration(){

        return FaunaClient.builder()
                .withSecret(adminKey)
                .build();
    }

}
