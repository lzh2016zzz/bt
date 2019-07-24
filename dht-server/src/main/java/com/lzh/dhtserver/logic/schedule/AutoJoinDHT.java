package com.lzh.dhtserver.logic.schedule;

import com.lzh.dhtserver.logic.DHTServerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
//
//    @Autowired
//    private DHTServerContext getDhtServer;
//
//    @Scheduled(fixedDelay = 30 * 1000, initialDelay = 10 * 1000)
//    public void doJob() {
//        if (getDhtServer.getNodesQueue().isEmpty()) {
//            log.info("local dht nodes is empty,rejoin dht internet..");
//            handler.joinDHT();
//        }
//    }
}
