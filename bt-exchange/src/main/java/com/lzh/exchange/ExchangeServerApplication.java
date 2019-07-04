package com.lzh.exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExchangeServerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ExchangeServerApplication.class);
        app.run(args);
    }

}
