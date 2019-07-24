package com.lzh.exchange.config;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

@Configuration
@Slf4j
public class NettyConfig implements ApplicationListener<ContextClosedEvent> {

    private EventLoopGroup group;

    @Value("${logic.connect-timeout-sec}")
    private int timeoutSec;


    @Bean(name = "group")
    public EventLoopGroup group() {
        return new NioEventLoopGroup();
    }

    @Bean(name = "clientBootstrap")
    public Bootstrap bootstrap() {
        log.info("init bootstrap bean。。");
        group = group();
        int threads = Constant.LOOP_GROUP_THREADS;
        int connectTimeout = timeoutSec * 1000;
        int minBufferSize = 1;
        int initialBufferSize = 102400;
        int maximumBufferSize = Integer.MAX_VALUE;
        log.info("bootstrap config value : [threads : {} ,connectTimeout : {} ,minBufferSize : {} ,initialBufferSize : {},maximumBufferSize :{}]"
                ,threads,connectTimeout,minBufferSize,initialBufferSize,maximumBufferSize);
        Bootstrap b = new Bootstrap();
        b.group(new NioEventLoopGroup(threads))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(minBufferSize, initialBufferSize, maximumBufferSize));
        return b;
    }


    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        if (contextClosedEvent.getApplicationContext().getParent() == null) {
            group.shutdownGracefully();
        }
    }

}
