package com.lzh.exchange.service;

import com.alibaba.fastjson.JSON;
import com.lzh.exchange.common.entity.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MetadataService {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    /**
     * send metadata to topic
     *
     * @param metadata
     */
    public void pushMetaData(Metadata metadata) {
        log.info("request to push metadata, info-hash : {}", metadata.getInfoHash());
        kafkaTemplate.send(MessageBuilder.withPayload(JSON.toJSONString(metadata)).build());
    }

}
