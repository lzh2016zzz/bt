package com.lzh.exchange.logic.config;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.NettyRuntime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

@Configuration
@Slf4j
public class NettyConfig  implements ApplicationListener<ContextClosedEvent> {


    @Value("${logic.udp.port}")
    private int udpPort;

    private EventLoopGroup group;

    private static final int nThreads = NettyRuntime.availableProcessors() + 1;

    @Bean(name = "group")
    public EventLoopGroup group() {
        return new NioEventLoopGroup();
    }

    @Bean(name = "clientBootstrap")
    public Bootstrap bootstrap() {
        log.info("初始化bootstrap bean。。");
        group = group();
        Bootstrap b = new Bootstrap();
        b.group(new NioEventLoopGroup(nThreads))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(1, 102400, Integer.MAX_VALUE));

        return b;
    }


    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        if(contextClosedEvent.getApplicationContext().getParent() == null) {
            group.shutdownGracefully();
        }
    }

}
