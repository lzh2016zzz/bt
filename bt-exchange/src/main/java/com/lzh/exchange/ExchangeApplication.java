package com.lzh.exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExchangeApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ExchangeApplication.class);
        app.run(args);
    }

}
