package com.lzh.dhtserver.logic;


import com.lzh.dhtserver.logic.entity.Node;
import com.lzh.dhtserver.logic.entity.UniqueBlockingQueue;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.AllArgsConstructor;
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

    private boolean started = false;


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
        if (started()) {
            serverChannelFuture.channel().writeAndFlush(packet);
        } else {
            log.error("node is not ready,send KRPC package failure + id:" + Hex.encodeHexString(this.getSelfNodeId()));
        }
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
            started = true;
            serverChannelFuture = severBootstrap.bind(udpPort).sync();
            serverChannelFuture.channel().closeFuture();
            startFindNodeTask();
            joinDHT();
            log.info("starting dht node sever, port : {}, nodeId : {}", udpPort.getPort(), Hex.encodeHexString(this.selfNodeId));
        } catch (InterruptedException e) {
            log.error("start dht node task is Interrupted,nodeId :{}", this.getSelfNodeId());
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
     * 用于持续获取dht节点的任务
     *
     * @return
     */
    private Thread startFindNodeTask() {
        FindNodeTask task = new FindNodeTask(this);
        task.setName("find-node-task-" + this.udpPort.getPort());
        return task;
    }

    @AllArgsConstructor
    private class FindNodeTask extends Thread {

        DHTServerContext context;

        @Override
        public void run() {
            try {
                log.info("keep findNodeTask,port:{}", context.udpPort.getPort());
                while (!isInterrupted()) {
                    Node node = context.getNodesQueue().take();
                    if (node != null) {
                        context.dhtServerHandler.findNode(node.getAddr(), node.getNodeId(), context.getSelfNodeId());
                    }
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {

            }
        }
    }


    public boolean started() {
        return this.started;
    }


}
