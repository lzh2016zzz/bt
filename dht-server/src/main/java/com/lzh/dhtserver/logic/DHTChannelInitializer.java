package com.lzh.dhtserver.logic;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DHTChannelInitializer extends ChannelInitializer<DatagramChannel> {

    private DHTServerContext dhtServer;

    @Override
    protected void initChannel(DatagramChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("handler", dhtServer.getDhtServerHandler());
    }
}
