package com.lzh.exchange.logic;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class MessageListener {

    @Autowired
    ExchangeClient client;

    @PostConstruct
    public void testRun() {
        log.info("開始獲取hash信息");
        String hash = "c505d185d46615c679272c7f833e54e4e42bd07e";
        try {
            byte []b = Hex.decodeHex(hash);
            client.send(Hex.encodeHexString(b),"59.115.154.156:22477");
        } catch (DecoderException e) {
            e.printStackTrace();
        }
    }
}
