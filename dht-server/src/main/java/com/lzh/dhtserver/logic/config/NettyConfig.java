package com.lzh.dhtserver.logic.config;

import com.lzh.bt.api.common.common.util.NodeIdUtil;
import com.lzh.dhtserver.logic.DHTChannelInitializer;
import com.lzh.dhtserver.logic.DHTServerContextHolder;
import com.lzh.dhtserver.logic.DHTServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
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
import java.util.stream.IntStream;
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

    private List<DHTServerContextHolder> dhtServerContexts = new CopyOnWriteArrayList<>();


    @Bean(name = "dhtServerContexts")
    public List<DHTServerContextHolder> dhtServerContexts(@Qualifier("selfNodeIdMap") Map<Integer, byte[]> selfNodeIdMap,
                                                          RedisTemplate redisTemplate, KafkaTemplate<String, String> kafkaTemplate) {
        for (Map.Entry<Integer, byte[]> entry : selfNodeIdMap.entrySet()) {
            EventLoopGroup group = group();
            Bootstrap bootstrap = new Bootstrap();
            DHTServerContextHolder context = new DHTServerContextHolder(redisTemplate, kafkaTemplate, bootstrap, new InetSocketAddress(entry.getKey()), entry.getValue(), new DHTServerHandler());
            bootstrap.group(group).channel(NioDatagramChannel.class).handler(new DHTChannelInitializer(context));
            dhtServerContexts.add(context);
            Map<ChannelOption<?>, Object> tcpChannelOptions = udpChannelOptions();
            Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
            for (@SuppressWarnings("rawtypes")
                    ChannelOption option : keySet) {
                bootstrap.option(option, tcpChannelOptions.get(option));
            }
        }
        initServerContext();
        return dhtServerContexts;
    }

    private void initServerContext() {
        dhtServerContexts.forEach(DHTServerContextHolder::startServer);
    }


    @Bean(name = "dhtEventLoopGroup")
    public EventLoopGroup group() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        group.add(eventLoopGroup);
        return eventLoopGroup;
    }


    @Bean(name = "udpChannelOptions")
    public Map<ChannelOption<?>, Object> udpChannelOptions() {
        int minBufferSize = 1;
        int initialBufferSize = 102400;
        int maximumBufferSize = Integer.MAX_VALUE;
        Map<ChannelOption<?>, Object> options = new HashMap<>();
        options.put(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        options.put(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(minBufferSize, initialBufferSize, maximumBufferSize));
        options.put(ChannelOption.SO_RCVBUF, rcvbuf);
        options.put(ChannelOption.SO_SNDBUF, sndbuf);
        return options;
    }


    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event instanceof ContextClosedEvent && event.getApplicationContext().getParent() == null) {
            group.stream()
                    .filter(s -> !s.isShutdown())
                    .forEach(EventExecutorGroup::shutdownGracefully);
        }
    }


    @Bean(name = "selfNodeIdMap")
    public Map<Integer, byte[]> selfNodeIds() throws IOException, DecoderException {
        Map<Integer, byte[]> map = new HashMap<>();
        String portsStr;

        if (applicationArguments.getSourceArgs().length == 0) {
            log.info("Without port configuration(use ',' to separate or startPort~endPort ), the default dht node server(port: {}) will be started", udpPort);
            portsStr = String.valueOf(udpPort);
        } else {
            portsStr = applicationArguments.getSourceArgs()[0].trim();
        }

        List<Integer> ports;

        if (portsStr.matches("^\\d+[~-]\\d+$")) {
            String[] port = portsStr.split("[~-]");
            Integer from = Optional.of(Integer.parseInt(port[0]))
                    .filter(n -> !(n < 0 || n > 65535))
                    .orElseThrow(() -> new IllegalArgumentException("port is not legal(0-65535)"));
            Integer to = Optional.of(Integer.parseInt(port[1]))
                    .filter(n -> !(n < 0 || n > 65535))
                    .orElseThrow(() -> new IllegalArgumentException("port is not legal(0-65535)"));
            ports = IntStream.range(from, to)
                    .boxed()
                    .collect(Collectors.toList());
        } else if (portsStr.matches("^\\d+$|^\\d+,\\d+$|^\\d+,$")) {
            ports = Stream.of(portsStr.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("ports format error");
        }

        for (Iterator<Integer> it = ports.iterator(); it.hasNext(); ) {
            Integer port = it.next();
            String fileName = String.format("nodeID-%d.txt", port);
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                Optional<String> stringStream = Files.lines(path, CharsetUtil.UTF_8)
                        .flatMap(line -> Arrays.stream(line.split(" "))).findFirst();
                if (stringStream.isPresent()) {
                    map.put(port, Hex.decodeHex(stringStream.get().toCharArray()));
                } else {
                    throw new IllegalArgumentException("can not read nodeId from " + fileName);
                }
            } else {
                Files.createFile(path);
                byte[] b = NodeIdUtil.createRandomNodeId();
                String nodeIdHex = Hex.encodeHexString(b);
                Files.write(path, nodeIdHex.getBytes(CharsetUtil.UTF_8));
                map.put(port, b);
            }
        }
        return Collections.unmodifiableMap(map);
    }

}
