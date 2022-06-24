package com.dev.luqman;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;

@EnableWebMvc
@SpringBootApplication
public class GoogleCatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoogleCatalogApplication.class, args);
    }
}
