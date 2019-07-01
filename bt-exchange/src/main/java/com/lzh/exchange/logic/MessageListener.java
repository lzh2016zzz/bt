package com.lzh.exchange.logic;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class MessageListener {

    @Autowired
    ExchangeClient client;

    @PostConstruct
    public void testRun() {
        log.info("開始獲取hash信息");
        String hash = "c7f89b89848d678dad4073c10e4028f3ce2061ce";
        try {
            byte []b = Hex.decodeHex(hash);
            client.send(Hex.encodeHexString(b),"111.197.114.250:15000");
        } catch (DecoderException e) {
            e.printStackTrace();
        }
    }
}
