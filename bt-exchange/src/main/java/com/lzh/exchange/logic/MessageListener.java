package com.lzh.exchange.logic;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class MessageListener {

    @Autowired
    ExchangeClient client;


    @KafkaListener(id = "topic-torrent-meta-info-listener",
    topics = "${logic.kafka.topic.topic-info-hash-output}")
    public void listen(String msgData) {
        log.info("接收数据 : "+ msgData);
        JSONObject jsonObject = JSONObject.parseObject(msgData);
        String infoHash;
        if(!StringUtils.isEmpty(infoHash = jsonObject.getString("infoHash"))){
            String ip;
            String port;
            if(!StringUtils.isEmpty(ip = jsonObject.getString("ip")) &&
                    !StringUtils.isEmpty(port = jsonObject.getString("port"))){
                String hexString = Hex.encodeHexString(Base64Utils.decodeFromString(infoHash));
                log.info("获取数据: {},{}",hexString,ip + ":" + port);
                client.send(hexString,ip + ":" + port);
            }
        } else {
            log.error("获取到的info-hash为空，无法解析");
            return;
        }

    }

}
