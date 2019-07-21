package com.lzh.exchange.logic;


import com.lzh.exchange.common.util.NodeIdUtil;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/***
 *
 * metedata
 *
 **/
@Slf4j
@Component
public class ExchangeClient {


    @Autowired
    BootstrapFactory bootstrapFactory;

    /**
     * 本机的 peerId
     */
    public static final byte[] peerId = NodeIdUtil.createRandomNodeId();

    /**
     * 发送数据
     */
    public MetaDataResultTask createTask(byte[] infoHash, String ip, int port) {
        final MetaDataResultTask result = MetaDataResultTask.metaDataResult();
        result.future(() -> queryTask(infoHash, ip, port, result));
        return result;
    }

    private ChannelFuture queryTask(byte[] infoHashHexStr, String ip, int port, MetaDataResultTask task) {
        return bootstrapFactory.build().handler(new CustomChannelInitializer(infoHashHexStr, task))
                .connect(new InetSocketAddress(ip, port))
                .addListener(new ConnectListener(infoHashHexStr, peerId, ip, port, task));
    }

}
