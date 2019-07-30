package com.lzh.dhtserver.logic.config;

import com.lzh.dhtserver.logic.DHTServerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@EnableScheduling
@Configuration
public class ScheduledConfig {

    @Autowired
    List<DHTServerContext> dhtServerContexts;

}
