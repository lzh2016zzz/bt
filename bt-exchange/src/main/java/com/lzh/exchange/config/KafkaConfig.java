package com.lzh.exchange.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {

    @Value("${logic.kafka.broker.host}")
    private String host;
    @Value("${logic.kafka.broker.port}")
    private int port;
    @Value("${logic.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        log.info("初始化Kakfa配置..");
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(Constant.NETTY_THREADS);
        factory.setBatchListener(true);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    @Bean
    public DefaultKafkaConsumerFactory consumerFactory() {
        return new DefaultKafkaConsumerFactory(consumerConfigs());
    }




    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> map = new HashMap<>();
        map.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);
        map.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,10);
        map.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,60000);
        map.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        map.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        map.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, host + ":" + port);
        map.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 100);
        map.put(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        return map;
    }
}
