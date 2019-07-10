package com.lzh.dhtserver.logic;


import com.lzh.dhtserver.common.util.NodeIdUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/***
 * 模拟 DHT 节点服务器
 *
 **/
@Slf4j
@Component
public class DHTServer {

    @Autowired
    @Qualifier("serverBootstrap")
    private Bootstrap b;

    @Autowired
    @Qualifier("udpSocketAddress")
    private InetSocketAddress udpPort;

    private ChannelFuture serverChannelFuture;


    /**
     * 启动节点列表
     */
    public static final List<InetSocketAddress> BOOTSTRAP_NODES = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new InetSocketAddress("router.bittorrent.com", 6881),
            new InetSocketAddress("dht.transmissionbt.com", 6881),
            new InetSocketAddress("router.utorrent.com", 6881),
            new InetSocketAddress("dht.aelitis.com", 6881))));

    /**
     * 随 SpringBoot 启动 DHT 服务器
     *
     * @throws Exception
     */
    @PostConstruct
    public void start() throws Exception {
        log.info("启动dht-server,udpPort :{}  ", udpPort);
        serverChannelFuture = b.bind(udpPort).sync();
        serverChannelFuture.channel().closeFuture();
        log.info("启动dht-server成功.");
    }

    /**
     * 发送 KRPC 协议数据报文
     *
     * @param packet
     */
    public void sendKRPC(DatagramPacket packet) {
        serverChannelFuture.channel().writeAndFlush(packet);
    }
}
