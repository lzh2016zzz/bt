package com.lzh.dhtserver.logic;


import com.lzh.dhtserver.logic.entity.Node;
import com.lzh.dhtserver.logic.entity.UniqueBlockingQueue;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

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
    public static final List<InetSocketAddress> BOOTSTRAP_NODES = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new InetSocketAddress("router.bittorrent.com", 6881),
            new InetSocketAddress("dht.transmissionbt.com", 6881),
            new InetSocketAddress("router.utorrent.com", 6881),
            new InetSocketAddress("dht.aelitis.com", 6881))));

    public DHTServerContext(RedisTemplate redisTemplate, KafkaTemplate<String, String> kafkaTemplate, Bootstrap severBootstrap, InetSocketAddress udpPort, byte[] selfNodeId, DHTServerHandler dhtServerHandler) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.severBootstrap = severBootstrap;
        this.udpPort = udpPort;
        if (selfNodeId != null) {
            this.selfNodeId = Arrays.copyOf(selfNodeId, selfNodeId.length);
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
     * 随 SpringBoot 启动 DHT 服务器
     *
     * @throws Exception
     */
    @PostConstruct
    public void start() throws Exception {
        log.info("starting dht server,udpPort :{}  ", udpPort);
        serverChannelFuture = severBootstrap.bind(udpPort).sync();
        serverChannelFuture.channel().closeFuture();
        log.info("starting dht server success");
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

    //TODO :: FIX join dht task
    @Scheduled(fixedDelay = 30 * 1000, initialDelay = 10 * 1000)
    public void doJob() {
        if (this.getNodesQueue().isEmpty()) {
            log.info("local dht nodes is empty,rejoin dht internet..");
            dhtServerHandler.joinDHT();
        }
    }

    /**
     * 用于持续获取dht节点的任务
     *
     * @return
     */
    private Runnable getFindNodeTask() {
        return new FindNodeTask(this);
    }

    @AllArgsConstructor
    private class FindNodeTask implements Runnable {

        DHTServerContext context;

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    try {
                        Node node = context.getNodesQueue().poll();
                        if (node != null) {
                            context.dhtServerHandler.findNode(node.getAddr(), node.getNodeId(), context.getSelfNodeId());
                        }
                    } catch (Exception e) {
                    }
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
            }
        }
    }


}
