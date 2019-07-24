package com.lzh.dhtserver.logic.config;

import com.lzh.dhtserver.common.util.NodeIdUtil;
import com.lzh.dhtserver.logic.DHTChannelInitializer;
import com.lzh.dhtserver.logic.DHTServerContext;
import com.lzh.dhtserver.logic.DHTServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * Netty 服务器配置
 *
 **/
@Configuration
@ConfigurationProperties(prefix = "logic")
@Slf4j
public class NettyConfig implements ApplicationListener<ApplicationContextEvent> {

    @Value("${logic.udp.port}")
    private int udpPort;


    @Value("${logic.so.rcvbuf}")
    private int rcvbuf;

    @Value("${logic.so.sndbuf}")
    private int sndbuf;

    private volatile EventLoopGroup group;

    private List<DHTServerContext> dhtServerContexts = new CopyOnWriteArrayList<>();


    @Bean(name = "serverBootstrap")
    public Bootstrap bootstrap(InetSocketAddress udpPort, byte[] selfNodeId, RedisTemplate redisTemplate, KafkaTemplate<String, String> kafkaTemplate) {
        EventLoopGroup group = group();
        Bootstrap bootstrap = new Bootstrap();
        DHTServerContext context = new DHTServerContext(redisTemplate, kafkaTemplate, bootstrap, udpPort, selfNodeId, new DHTServerHandler());
        bootstrap.group(group).channel(NioDatagramChannel.class).handler(new DHTChannelInitializer(context));
        dhtServerContexts.add(context);
        Map<ChannelOption<?>, Object> tcpChannelOptions = udpChannelOptions();
        Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
        for (@SuppressWarnings("rawtypes")
                ChannelOption option : keySet) {
            bootstrap.option(option, tcpChannelOptions.get(option));
        }
        return bootstrap;
    }

    private void initServerContext() {
        dhtServerContexts.forEach(DHTServerContext::startServer);
    }

    @Scheduled(fixedDelay = 30 * 1000, initialDelay = 10 * 1000)
    public void doJob() {
        dhtServerContexts.forEach(DHTServerContext::joinDHT);
    }


    @Bean(name = "group")
    public synchronized EventLoopGroup group() {
        if (this.group == null) {
            this.group = new NioEventLoopGroup();
        }
        return this.group;
    }


    @Bean(name = "udpChannelOptions")
    public Map<ChannelOption<?>, Object> udpChannelOptions() {
        Map<ChannelOption<?>, Object> options = new HashMap<>();
        options.put(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        options.put(ChannelOption.SO_RCVBUF, rcvbuf);
        options.put(ChannelOption.SO_SNDBUF, sndbuf);
        return options;
    }


    @Bean(name = "udpSocketAddress")
    public InetSocketAddress udpPort() {
        return new InetSocketAddress(udpPort);
    }


    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event instanceof ContextClosedEvent && event.getApplicationContext().getParent() == null) {
            group.shutdownGracefully();
        } else if (event instanceof ContextRefreshedEvent && event.getApplicationContext().getParent() == null) {
            initServerContext();
        }
    }


    @Bean(name = "selfNodeId")
    public byte[] selfNodeId() throws IOException, DecoderException {
        String fileName = "nodeID.txt";
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            Optional<String> stringStream = Files.lines(path, CharsetUtil.UTF_8)
                    .flatMap(line -> Arrays.stream(line.split(" "))).findFirst();
            if (stringStream.isPresent()) {
                return Hex.decodeHex(stringStream.get());
            }
        } else {
            Files.createFile(path);
            byte[] b = NodeIdUtil.createRandomNodeId();
            String nodeIdHex = Hex.encodeHexString(b);
            Files.write(path, nodeIdHex.getBytes(CharsetUtil.UTF_8));
            return b;
        }
        return null;
    }

}
