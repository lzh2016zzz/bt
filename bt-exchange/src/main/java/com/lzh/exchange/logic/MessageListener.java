package com.lzh.exchange.logic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lzh.exchange.common.constant.MetaDataResult;
import com.lzh.exchange.common.entity.Metadata;
import com.lzh.exchange.common.util.bencode.BencodingUtils;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

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
                        String hexString = Hex.encodeHexString(Base64Utils.decodeFromString(infoHash));
                        log.info("获取数据: {},{}", hexString, ip + ":" + port);
                        MetaDataResult metaDataResult = client.send(hexString, ip , port);
                        try {
                            metaDataResult.getLatch().await(10, TimeUnit.SECONDS);
                            if (metaDataResult.getResult() != null) {
                                bytes2Metadata(metaDataResult.getResult(), infoHash);
                            }
                        } catch (InterruptedException e) {
                            log.error("获取信息超时，请重试{},{}", hexString, ip + ":" + port);
                        }
                    }
                } else {
                    log.error("获取到的info-hash为空，无法解析");
                    return;
                }
            });
        }

    }

    /**
     * byte[] 转 {@link Metadata}
     */
    @SuppressWarnings("unchecked")
    public Metadata bytes2Metadata(byte[] bytes, String infoHashHexStr) {
        try {
            String metadataStr = new String(bytes, CharsetUtil.UTF_8);
            String metadataBencodeStr = metadataStr.substring
                    (0, metadataStr.indexOf("6:pieces")) + "e";
            Map<String, ?> resultMap = BencodingUtils.decode(metadataBencodeStr.getBytes(CharsetUtil.UTF_8));
            log.info("metaData信息 ： {}", JSON.toJSON(resultMap));
        } catch (Exception e) {
            log.error("[bytes2Metadata]失败.e:", e.getMessage(), e);
        }
        return null;
    }

}
