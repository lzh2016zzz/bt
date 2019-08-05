package com.lzh.bt.api.service;

import com.alibaba.fastjson.JSON;
import com.lzh.bt.api.AbstractServerContext;
import com.lzh.bt.api.entity.Metadata;
import com.lzh.bt.api.repository.MetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MetaDataService extends AbstractServerContext {

    @Autowired
    MetadataRepository metadataRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @KafkaListener(id = "topic-torrent-meta-info-saver",
            topics = "${logic.kafka.topic.topic-torrent-meta-info}")
    public void receiveMetaData(String msgData) {
        msgData = "[" + msgData + "]";
        List<Metadata> metadata = JSON.parseArray(msgData, Metadata.class);
        metadataRepository.saveAll(metadata.stream().peek(k -> log.info("request to save metadata : " + k.toString())).collect(Collectors.toList()));
    }

    /**
     * page query
     *
     * @param name     名称
     * @param suffix   后缀
     * @param pageable 分页
     * @return
     */
    public List<Metadata> query(String name, String suffix, Pageable pageable) {
        return metadataRepository.findAllByNameContainingAndSuffixesContainsOrderByHotDesc(name, suffix, pageable).stream()
                .map(k -> {
                    k.setHot(getHot(k.getInfoHash()));
                    return k;
                }).collect(Collectors.toList());
    }


    @Override
    public RedisTemplate<String, String> redisTemplate() {
        return this.redisTemplate;
    }
}
