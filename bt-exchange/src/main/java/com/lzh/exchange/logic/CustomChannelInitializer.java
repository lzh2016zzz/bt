package com.lzh.exchange.logic;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomChannelInitializer extends ChannelInitializer {

		private byte[] infoHash;
		private final MetaDataResultTask result;

		@Override
		protected void initChannel(Channel ch) throws Exception {
			ch.pipeline()
					.addLast(new ReadTimeoutHandler(30))
					.addLast(new MetaDataExchangeHandler(result));
		}
	}