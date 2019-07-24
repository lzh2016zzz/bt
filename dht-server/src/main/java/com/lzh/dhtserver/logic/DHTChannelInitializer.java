package com.lzh.dhtserver.logic;

import com.lzh.dhtserver.logic.handler.DHTServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DHTChannelInitializer extends ChannelInitializer<DatagramChannel> {

	private DHTServerHandler dhtServerHandler;

	@Override
	protected void initChannel(DatagramChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("handler", dhtServerHandler);
	}
}
