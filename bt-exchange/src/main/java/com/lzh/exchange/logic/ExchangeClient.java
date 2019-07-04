package com.lzh.exchange.logic;


import com.lzh.exchange.common.constant.MetaDataResult;
import com.lzh.exchange.common.util.NodeIdUtil;
import com.lzh.exchange.logic.config.Constant;
import com.lzh.exchange.logic.config.CustomChannelInitializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

/***
 *
 * metedata
 *
 **/
@Slf4j
@Component
public class ExchangeClient {


    @Autowired
    BootstrapFactory bootstrapFactory;

    /**
     * 本機的 peerId
     */
    public static final byte[] peerId = NodeIdUtil.createRandomNodeId();

    /**
     * 发送数据
     *
     * @throws InterruptedException
     */
    public MetaDataResult send(String infoHashHexStr,String ip ,int port) {
        CountDownLatch latch = new CountDownLatch(1);
        final MetaDataResult result = new MetaDataResult(latch);
        bootstrapFactory.build().handler(new CustomChannelInitializer(infoHashHexStr, result))
                .connect(new InetSocketAddress(ip, port))
                .addListener(new ConnectListener(infoHashHexStr,peerId,ip,port,latch));
        return result;
    }


    private class ConnectListener implements ChannelFutureListener {

        private String infoHashHexStr;
        //自己的peerId,直接定义为和nodeId相同即可
        private byte[] selfPeerId;

        private String ip;

        private int port;

        private CountDownLatch countDownLatch;

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                //连接成功发送握手消息
                log.info("连接peer成功，向peer[{}:{}]查询info-hash",ip,port);
                sendHandshakeMessage(future);
                return;
            }
            //如果失败 ,不做任何操作
            log.info("连接peer失败,ip:{},port:{}",ip,port);
            countDownLatch.countDown();
            future.channel().close();
        }

        /**
         * 发送握手消息
         */
        private void sendHandshakeMessage(ChannelFuture future) {
            byte[] infoHash;
            try {
                infoHash = Hex.decodeHex(infoHashHexStr);
                byte[] sendBytes = new byte[68];
                System.arraycopy(Constant.GET_METADATA_HANDSHAKE_PRE_BYTES, 0, sendBytes, 0, 28);
                System.arraycopy(infoHash, 0, sendBytes, 28, Constant.BASIC_HASH_LEN);
                System.arraycopy(selfPeerId, 0, sendBytes, 48, 20);
                future.channel().writeAndFlush(Unpooled.copiedBuffer(sendBytes));
            } catch (DecoderException e) {
                log.error("info-hash转换异常",infoHashHexStr);
            }
        }

        public ConnectListener(String infoHashHexStr, byte[] selfPeerId,String ip,int port,CountDownLatch countDownLatch) {
            this.infoHashHexStr = infoHashHexStr;
            this.selfPeerId = selfPeerId;
            this.ip = ip;
            this.port = port;
            this.countDownLatch = countDownLatch;
        }
    }

}
