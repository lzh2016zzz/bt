package com.lzh.exchange.service;

import com.alibaba.fastjson.JSON;
import com.lzh.bt.api.common.constant.Constant;
import com.lzh.bt.api.common.entity.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MetadataService {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * send metadata to topic
     *
     * @param metadata
     */
    public void pushMetaData(Metadata metadata) {
        log.info("request to push metadata, info-hash : {}", metadata.getInfoHash());
        kafkaTemplate.send(MessageBuilder.withPayload(JSON.toJSONString(metadata)).build());
        //添加到成功下载的hex列表中
        redisTemplate.opsForSet().add(Constant.SUCCESS_INFO_HASH_HEX, metadata.getInfoHash());
    }

}
