package com.lzh.exchange.logic.config;

import com.lzh.exchange.logic.MetaDataResultTask;
import com.lzh.exchange.logic.MetaDataExchangeHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomChannelInitializer extends ChannelInitializer {

		private String infoHashHexStr;
		private final MetaDataResultTask result;

		@Override
		protected void initChannel(Channel ch) throws Exception {
			ch.pipeline()
					.addLast(new ReadTimeoutHandler(30))
					.addLast(new MetaDataExchangeHandler(infoHashHexStr, result));

		}
	}