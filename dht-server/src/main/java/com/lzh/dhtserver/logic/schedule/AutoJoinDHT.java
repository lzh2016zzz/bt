package com.lzh.dhtserver.logic.schedule;

import com.lzh.dhtserver.logic.DHTServer;
import com.lzh.dhtserver.logic.handler.DHTServerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/***
 * 定时检测本地节点数并自动加入 DHT 网络
 *
 **/
@Slf4j
@Component
public class AutoJoinDHT {

    @Autowired
    private DHTServerHandler handler;

    @Autowired
    private DHTServer dhtServer;

    @Scheduled(fixedDelay = 30 * 1000, initialDelay = 10 * 1000)
    public void doJob() {
        if (dhtServer.getNodesQueue().isEmpty()) {
            log.info("local dht nodes is empty,rejoin dht internet..");
            handler.joinDHT();
        }
    }
}
