package com.lzh.exchange.logic;

import io.netty.bootstrap.Bootstrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BootstrapFactory {

    @Autowired
    @Qualifier("clientBootstrap")
    public Bootstrap bootstrap;

    public Bootstrap build(){
        return bootstrap.clone();
    }

}
