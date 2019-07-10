package com.lzh.exchange.logic;


import com.lzh.exchange.common.util.NodeIdUtil;
import com.lzh.exchange.config.Constant;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
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
     */
    public MetaDataResultTask createTask(byte[] infoHash, String ip , int port) {
        final MetaDataResultTask result = MetaDataResultTask.metaDataResult();
        result.future(() -> queryTask(infoHash, ip, port, result));
        return result;
    }

    private ChannelFuture queryTask(byte[] infoHashHexStr, String ip, int port, MetaDataResultTask task) {
        return bootstrapFactory.build().handler(new CustomChannelInitializer(infoHashHexStr, task))
                .connect(new InetSocketAddress(ip, port))
                .addListener(new ConnectListener(infoHashHexStr,peerId,ip,port,task));
    }


    private class ConnectListener implements ChannelFutureListener {

        private byte[] infoHash;
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
                byte[] sendBytes = new byte[68];
                System.arraycopy(Constant.GET_METADATA_HANDSHAKE_PRE_BYTES, 0, sendBytes, 0, 28);
                System.arraycopy(infoHash, 0, sendBytes, 28, Constant.BASIC_HASH_LEN);
                System.arraycopy(selfPeerId, 0, sendBytes, 48, 20);
                future.channel().writeAndFlush(Unpooled.copiedBuffer(sendBytes));
        }

        public ConnectListener(byte[] infoHash, byte[] selfPeerId, String ip, int port, MetaDataResultTask task) {
            this.infoHash = infoHash;
            this.selfPeerId = selfPeerId;
            this.ip = ip;
            this.port = port;
            this.task = task;
        }
    }

}
