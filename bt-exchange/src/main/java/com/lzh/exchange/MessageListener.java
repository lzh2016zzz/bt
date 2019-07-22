package com.lzh.exchange;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lzh.exchange.logic.ExchangeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class MessageListener {

    @Autowired
    ExchangeClient client;

    @KafkaListener(id = "topic-torrent-meta-info-listener",
    topics = "${logic.kafka.topic.topic-info-hash-output}")
    public void listen(String msgData) {
        //log.info("接收报文 : "+ msgData);
        JSONArray messages = null;
        try {
              msgData = "[" + msgData + "]";
              messages = JSONObject.parseArray(msgData);
        }catch (Exception ex){
            log.error("parsing json error,data,: " + msgData);
        }
        if(!CollectionUtils.isEmpty(messages)) {
            messages.stream().map(m -> (JSONObject) m).forEach(msg -> {
                String infoHash;
                if (!StringUtils.isEmpty(infoHash = msg.getString("infoHash"))) {
                    String ip;
                    int port;
                    if (!StringUtils.isEmpty(ip = msg.getString("ip")) &&
                            !StringUtils.isEmpty(port = msg.getInteger("port"))) {

                        //create task
                        //log.info("add task: {},{}", infoHash, ip + ":" + port);

                        client.createTask(Base64Utils.decodeFromString(infoHash), ip , port)
                                .success((meta) -> log.info("metadata :" + JSON.toJSONString(meta)))
                                .failure((err) -> log.error("task failure,reason ： " + err.getMessage()))
                                .start();
                    }
                } else {
                    log.error("info-hash is null or empty string ,parsing failure");
                    return;
                }
            });
        }

    }


}
