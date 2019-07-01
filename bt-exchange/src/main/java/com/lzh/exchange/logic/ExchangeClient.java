package com.lzh.exchange.logic;


import com.lzh.exchange.common.constant.MetaDataResult;
import com.lzh.exchange.common.util.NodeIdUtil;
import com.lzh.exchange.logic.config.Constant;
import com.lzh.exchange.logic.config.CustomChannelInitializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.AllArgsConstructor;
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
    public MetaDataResult send(String infoHashHexStr,String peer) {
        CountDownLatch latch = new CountDownLatch(1);
        final MetaDataResult result = new MetaDataResult(latch);
        String[] ipPort = peer.split(":");
        bootstrapFactory.build().handler(new CustomChannelInitializer(infoHashHexStr, result))
                .connect(new InetSocketAddress(ipPort[0], Integer.parseInt(ipPort[1])))
                .addListener(new ConnectListener(infoHashHexStr,peerId));
        return result;
    }


    @AllArgsConstructor
    private class ConnectListener implements ChannelFutureListener {
        private String infoHashHexStr;
        //自己的peerId,直接定义为和nodeId相同即可
        private byte[] selfPeerId;

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                //连接成功发送握手消息
                log.info("连接成功，查询info-hash信息，，");
                sendHandshakeMessage(future);
                return;
            }
            //如果失败 ,不做任何操作
            log.info("连接失败。。");
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
                System.arraycopy(infoHash, 0, sendBytes, 28, 20);
                System.arraycopy(selfPeerId, 0, sendBytes, 48, 20);
                future.channel().writeAndFlush(Unpooled.copiedBuffer(sendBytes));
            } catch (DecoderException e) {
                log.error("info-hash转换异常",infoHashHexStr);
            }
        }
    }

}
