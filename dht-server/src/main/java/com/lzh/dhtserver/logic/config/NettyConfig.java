package com.lzh.dhtserver.logic.config;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/***
 * Netty 服务器配置
 *
 **/
@Configuration
@ConfigurationProperties(prefix = "logic")
@Slf4j
public class NettyConfig implements ApplicationListener<ContextClosedEvent> {

	@Value("${logic.udp.port}")
	private int udpPort;
	@Value("${logic.so.backlog}")
	private int backlog;
	@Value("${logic.so.rcvbuf}")
	private int rcvbuf;
	@Value("${logic.so.sndbuf}")
	private int sndbuf;

	private EventLoopGroup group;


	@Autowired
	@Qualifier("channelInitializer")
	private ChannelInitializer channelInitializer;

	@Bean(name = "serverBootstrap")
	public Bootstrap bootstrap() {
		group = group();
		Bootstrap b = new Bootstrap();
		b.group(group)
				.channel(NioDatagramChannel.class)
				.handler(channelInitializer);
		Map<ChannelOption<?>, Object> tcpChannelOptions = udpChannelOptions();
		Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
		for (@SuppressWarnings("rawtypes")
				ChannelOption option : keySet) {
			b.option(option, tcpChannelOptions.get(option));
		}
		return b;
	}

	@Bean(name = "group")
	public EventLoopGroup group() {
		return new NioEventLoopGroup();
	}

	@Bean(name = "udpSocketAddress")
	public InetSocketAddress udpPort() {
		return new InetSocketAddress(udpPort);
	}

	@Bean(name = "udpChannelOptions")
	public Map<ChannelOption<?>, Object> udpChannelOptions() {
		Map<ChannelOption<?>, Object> options = new HashMap<>();
		options.put(ChannelOption.SO_BACKLOG, backlog);
		options.put(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		options.put(ChannelOption.SO_RCVBUF, rcvbuf);
		options.put(ChannelOption.SO_SNDBUF, sndbuf);
		return options;
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
		if(contextClosedEvent.getApplicationContext().getParent() == null) {
			group.shutdownGracefully();
		}
	}
}
