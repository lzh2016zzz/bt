package com.lzh.exchange.logic;

import com.lzh.exchange.config.Constant;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketException;

@Slf4j
public class ConnectListener implements ChannelFutureListener {

    //torrent infoHash
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
    public void operationComplete(ChannelFuture future) {
        if (future.isSuccess()) {
            //连接成功发送握手消息
            log.info("connecting to node [{}:{} successful，send handshake message", ip, port);
            sendHandshakeMessage(future);
        } else {
            task.doFailure(() -> new SocketException("connecting to node[" + ip + " : " + port + "] failure"));
            future.channel().close();
        }
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
