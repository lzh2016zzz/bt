package com.lzh.dhtserver.logic;


import com.lzh.dhtserver.logic.entity.Node;
import com.lzh.dhtserver.logic.entity.UniqueBlockingQueue;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/***
 * DHT Node Server Context
 **/
@Slf4j
public class DHTServerContext {


    private RedisTemplate redisTemplate;

    private KafkaTemplate<String, String> kafkaTemplate;

    private Bootstrap severBootstrap;

    private InetSocketAddress udpPort;

    private byte[] selfNodeId;

    private ChannelFuture serverChannelFuture;

    private DHTServerHandler dhtServerHandler;

    private final UniqueBlockingQueue nodesQueue = new UniqueBlockingQueue();


    /**
     * 启动节点列表
     */
    public static final List<InetSocketAddress> BOOTSTRAP_NODES = Collections.unmodifiableList(Arrays.asList(
            new InetSocketAddress("router.bittorrent.com", 6881),
            new InetSocketAddress("dht.transmissionbt.com", 6881),
            new InetSocketAddress("router.utorrent.com", 6881),
            new InetSocketAddress("dht.aelitis.com", 6881)));

    public DHTServerContext(RedisTemplate redisTemplate, KafkaTemplate<String, String> kafkaTemplate, Bootstrap severBootstrap, InetSocketAddress udpPort, byte[] selfNodeId, DHTServerHandler dhtServerHandler) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.severBootstrap = severBootstrap;
        this.udpPort = udpPort;
        if (selfNodeId != null) {
            this.selfNodeId = Arrays.copyOf(selfNodeId, selfNodeId.length);
        }
        if (dhtServerHandler == null) {
            throw new NullPointerException(" init dht server context failure : handler can not be null");
        }
        this.dhtServerHandler = dhtServerHandler;
        this.dhtServerHandler.setDhtServerContext(this);
    }

    /**
     * 发送 KRPC 协议数据报文
     *
     * @param packet
     */
    public void sendKRPC(DatagramPacket packet) {
        serverChannelFuture.channel().writeAndFlush(packet);
    }

    public UniqueBlockingQueue getNodesQueue() {
        return nodesQueue;
    }

    public byte[] getSelfNodeId() {
        if (this.selfNodeId == null) {
            return null;
        }
        return Arrays.copyOf(this.selfNodeId, selfNodeId.length);
    }

    /**
     * 启动 DHT 服务器
     *
     * @throws Exception
     */
    public void startServer() {
        try {
            serverChannelFuture = severBootstrap.bind(udpPort).sync();
            serverChannelFuture.channel().closeFuture();
            log.info("starting dht node sever, port : {}, nodeId : {}", udpPort.getPort(), Hex.encodeHexString(this.selfNodeId));
        } catch (InterruptedException e) {
            log.error("start dht node failure,nodeId :{}", this.getSelfNodeId());
        }
    }

    public ChannelHandler getDhtServerHandler() {
        return dhtServerHandler;
    }

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public KafkaTemplate<String, String> getKafkaTemplate() {
        return kafkaTemplate;
    }

    public InetSocketAddress getUdpPort() {
        return udpPort;
    }

    public void joinDHT() {
        if (this.getNodesQueue().isEmpty()) {
            log.info("local dht nodes is empty,rejoin dht internet , port : {}", this.udpPort.getPort());
            this.dhtServerHandler.joinDHT();
        }
    }

    /**
     * 获取dht节点
     *
     * @return
     */
    public void findNode() {
        Node node = this.getNodesQueue().poll();
        if (node != null) {
            this.dhtServerHandler.findNode(node.getAddr(), node.getNodeId(), this.getSelfNodeId());
        }
    }

    public boolean started() {
        return this.serverChannelFuture != null;
    }


}
