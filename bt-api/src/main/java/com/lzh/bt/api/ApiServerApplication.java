package com.lzh.bt.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiServerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ApiServerApplication.class);
        app.run(args);
    }

}
