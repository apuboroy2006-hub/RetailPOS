package com.example.retailpos_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import com.retailpos.backend.config.MongoConfig;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.example.retailpos_backend",
        "com.retailpos.backend"
})
@Import(MongoConfig.class)
public class RetailposBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetailposBackendApplication.class, args);
    }
}