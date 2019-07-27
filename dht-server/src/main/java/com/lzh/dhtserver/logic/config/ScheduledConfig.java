package com.lzh.dhtserver.logic.config;

import com.lzh.dhtserver.logic.DHTServerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@EnableScheduling
@Configuration
public class ScheduledConfig {

    @Autowired
    List<DHTServerContext> dhtServerContexts;


    @Scheduled(fixedDelay = 10, initialDelay = 10 * 1000)
    public void findNodeJob() {
        dhtServerContexts.stream().filter(DHTServerContext::started).forEach(DHTServerContext::findNode);
    }

    @Scheduled(fixedDelay = 30 * 1000, initialDelay = 10 * 1000)
    public void joinDhtJob() {
        dhtServerContexts.stream().filter(DHTServerContext::started).forEach(DHTServerContext::joinDHT);
    }
}
