package com.lzh.exchange;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lzh.exchange.logic.ExchangeClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
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
            log.error("解析json数据异常，报文: " + msgData);
        }
        if(!CollectionUtils.isEmpty(messages)) {
            messages.stream().map(m -> (JSONObject) m).forEach(msg -> {
                String infoHash;
                if (!StringUtils.isEmpty(infoHash = msg.getString("infoHash"))) {
                    String ip;
                    int port;
                    if (!StringUtils.isEmpty(ip = msg.getString("ip")) &&
                            !StringUtils.isEmpty(port = msg.getInteger("port"))) {

                        //创建任务
                        log.info("新增任务: {},{}", infoHash, ip + ":" + port);

                        client.createTask(Base64Utils.decodeFromString(infoHash), ip , port)
                                .success((meta) -> {
                                    //成功回调
                                })
                                .failure((err) -> log.error("任务失败，原因： " + err.getMessage()))
                                .start();
                    }
                } else {
                    log.error("获取到的info-hash为空，无法解析");
                    return;
                }
            });
        }

    }


}
