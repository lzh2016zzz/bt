package com.lzh.exchange.logic;


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
import java.net.SocketException;

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
    public MetaDataResultTask createTask(String infoHashHexStr, String ip , int port) {
        final MetaDataResultTask result = MetaDataResultTask.metaDataResult();
        result.future(() -> queryTask(infoHashHexStr, ip, port, result));
        return result;
    }

    private ChannelFuture queryTask(String infoHashHexStr, String ip, int port, MetaDataResultTask task) {
        return bootstrapFactory.build().handler(new CustomChannelInitializer(infoHashHexStr, task))
                .connect(new InetSocketAddress(ip, port))
                .addListener(new ConnectListener(infoHashHexStr,peerId,ip,port,task));
    }


    private class ConnectListener implements ChannelFutureListener {

        private String infoHashHexStr;
        //自己的peerId,直接定义为和nodeId相同即可
        private byte[] selfPeerId;
        //连接的ip
        private String ip;
        //端口号
        private int port;
        //task
        private MetaDataResultTask task;

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                //连接成功发送握手消息
                log.info("连接peer成功，向peer[{}:{}]查询info-hash",ip,port);
                sendHandshakeMessage(future);
                return;
            }
            task.doFailure(() -> new SocketException("连接peer失败"));
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

        public ConnectListener(String infoHashHexStr, byte[] selfPeerId, String ip, int port,MetaDataResultTask task) {
            this.infoHashHexStr = infoHashHexStr;
            this.selfPeerId = selfPeerId;
            this.ip = ip;
            this.port = port;
            this.task = task;
        }
    }

}
