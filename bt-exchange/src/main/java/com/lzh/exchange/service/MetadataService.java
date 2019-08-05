package com.lzh.exchange.service;

import com.alibaba.fastjson.JSON;
import com.lzh.bt.api.AbstractServerContext;
import com.lzh.bt.api.common.entity.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MetadataService extends AbstractServerContext {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    /**
     * send metadata to topic
     *
     * @param metadata
     */
    public void pushMetaData(Metadata metadata) {
        log.info("request to push metadata, info-hash : {}", metadata.getInfoHash());
        kafkaTemplate.send(MessageBuilder.withPayload(JSON.toJSONString(metadata)).build());
        //标记成下载成功
        markHexSaved(metadata.getInfoHash());
    }

    @Override
    public RedisTemplate<String, String> setRedisTemplate() {
        return this.redisTemplate;
    }
}
