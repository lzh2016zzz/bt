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
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private List<EventLoopGroup> group = new CopyOnWriteArrayList<>();

    @Autowired
    private ApplicationArguments applicationArguments;

    private List<DHTServerContext> dhtServerContexts = new CopyOnWriteArrayList<>();


    @Bean(name = "dhtServerContexts")
    public List<DHTServerContext> dhtServerContexts(@Qualifier("selfNodeIdMap") Map<Integer, byte[]> selfNodeIdMap,
                                                    RedisTemplate redisTemplate, KafkaTemplate<String, String> kafkaTemplate) {
        for (Map.Entry<Integer, byte[]> entry : selfNodeIdMap.entrySet()) {
            EventLoopGroup group = group();
            Bootstrap bootstrap = new Bootstrap();
            DHTServerContext context = new DHTServerContext(redisTemplate, kafkaTemplate, bootstrap, new InetSocketAddress(entry.getKey()), entry.getValue(), new DHTServerHandler());
            bootstrap.group(group).channel(NioDatagramChannel.class).handler(new DHTChannelInitializer(context));
            dhtServerContexts.add(context);
            Map<ChannelOption<?>, Object> tcpChannelOptions = udpChannelOptions();
            Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
            for (@SuppressWarnings("rawtypes")
                    ChannelOption option : keySet) {
                bootstrap.option(option, tcpChannelOptions.get(option));
            }
        }
        return dhtServerContexts;
    }

    private void initServerContext() {
        dhtServerContexts.forEach(DHTServerContext::startServer);
    }



    @Bean(name = "group")
    public synchronized EventLoopGroup group() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        group.add(eventLoopGroup);
        return eventLoopGroup;
    }


    @Bean(name = "udpChannelOptions")
    public Map<ChannelOption<?>, Object> udpChannelOptions() {
        Map<ChannelOption<?>, Object> options = new HashMap<>();
        options.put(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        options.put(ChannelOption.SO_RCVBUF, rcvbuf);
        options.put(ChannelOption.SO_SNDBUF, sndbuf);
        return options;
    }


    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event instanceof ContextClosedEvent && event.getApplicationContext().getParent() == null) {
            group.stream().forEach(EventExecutorGroup::shutdownGracefully);
        } else if (event instanceof ContextRefreshedEvent && event.getApplicationContext().getParent() == null) {
            initServerContext();
        }
    }


    @Bean(name = "selfNodeIdMap")
    public Map<Integer, byte[]> selfNodeIds() throws IOException, DecoderException {
        Map<Integer, byte[]> map = new HashMap<>();
        String portsStr;

        if (applicationArguments.getSourceArgs().length == 0) {
            log.info("Without port configuration(use ',' to separate), the default dht node server(port: {}) will be started", udpPort);
            portsStr = String.valueOf(udpPort);
        } else {
            portsStr = applicationArguments.getSourceArgs()[0];
        }

        List<Integer> ports = Stream.of(portsStr.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        for (Iterator<Integer> it = ports.iterator(); it.hasNext(); ) {
            Integer port = it.next();
            String fileName = String.format("nodeID-%d.txt", port);
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                Optional<String> stringStream = Files.lines(path, CharsetUtil.UTF_8)
                        .flatMap(line -> Arrays.stream(line.split(" "))).findFirst();
                if (stringStream.isPresent()) {
                    map.put(port, Hex.decodeHex(stringStream.get()));
                }
            } else {
                Files.createFile(path);
                byte[] b = NodeIdUtil.createRandomNodeId();
                String nodeIdHex = Hex.encodeHexString(b);
                Files.write(path, nodeIdHex.getBytes(CharsetUtil.UTF_8));
                map.put(port, b);
            }
        }
        return map;
    }

}
