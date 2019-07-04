package com.lzh.exchange.logic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class MessageListener {

    @Autowired
    ExchangeClient client;


//    @KafkaListener(id = "topic-torrent-meta-info-listener"
//     , groupId = "${spring.kafka.consumer.group-id}",
//    topics = "${spring.kafka.topics.workflow-has-finish-notify}")
//    public void listen(String msgData) {
//        log.info("demo receive : "+ msgData);
//    }

    @PostConstruct
    public void testRun() {
        log.info("開始獲取hash信息");
        client.send("c02538e35bffde93929c4b55d870e28bd2ee7318","114.102.7.170:15000");

    }
}
