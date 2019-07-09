package com.lzh.exchange.logic;

import com.lzh.exchange.common.util.bencode.BencodingUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/***
 * bep9 & bep 10
 **/
@Slf4j
@ChannelHandler.Sharable
public class MetaDataExchangeHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private MetaDataResultTask metaDataResultTask;

    private int metadataSize;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        String messageStr = new String(bytes, CharsetUtil.ISO_8859_1);

        //收到握手消息回复
        if (bytes[0] == (byte) 19) {
            //发送扩展消息
            sendExtendMessage(ctx);
        }

        //如果收到的消息中包含ut_metadata,提取出ut_metadata的值
        String utMetadataStr = "ut_metadata";
        String metadataSizeStr = "metadata_size";
        if (messageStr.contains(utMetadataStr) && messageStr.contains(metadataSizeStr)) {
            sendMetadataRequest(ctx, messageStr, utMetadataStr, metadataSizeStr);
        }

        //如果是分片信息
        if (messageStr.contains("msg_type")) {
//				log.info("收到分片消息:{}", messageStr);
            fetchMetadataBytes(messageStr);
        }
    }


    /***
     * 获取metadataBytes
     */
    private void fetchMetadataBytes(String messageStr) {

        String MetaDataResultStr = messageStr.substring(messageStr.indexOf("ee") + 2);
        byte[] metaDataResultStrBytes = MetaDataResultStr.getBytes(CharsetUtil.ISO_8859_1);
        ByteBuf buf;
        Optional.ofNullable(buf = metaDataResultTask.getResult())
                .orElseThrow(() -> new NullPointerException("metaDataTask.result初始化异常,null"))
                .writeBytes(metaDataResultStrBytes);
        //通知task获取metadata成功
        if (buf.array().length >= metadataSize) {
            metaDataResultTask.doSuccess();
        }
    }

    /**
     * 发送 metadata 请求消息
     */
    private void sendMetadataRequest(ChannelHandlerContext ctx, String messageStr, String utMetadataStr, String metadataSizeStr) {
        int utMetadataIndex = messageStr.indexOf(utMetadataStr) + utMetadataStr.length() + 1;
        //ut_metadata值
        int utMetadataValue = Integer.parseInt(messageStr.substring(utMetadataIndex, utMetadataIndex + 1));
        int metadataSizeIndex = messageStr.indexOf(metadataSizeStr) + metadataSizeStr.length() + 1;
        String otherStr = messageStr.substring(metadataSizeIndex);
        //metadata_size值
        metadataSize = Integer.parseInt(otherStr.substring(0, otherStr.indexOf("e")));
        //分块数
        int blockSum = (int) Math.ceil((double) metadataSize / 5);
        log.info("该种子metadata大小:{},分块数:{}", metadataSize, blockSum);
        initResult(metadataSize);
        //发送metadata请求
        for (int i = 0; i < blockSum; i++) {
            Map<String, Object> metadataRequestMap = new LinkedHashMap<>();
            metadataRequestMap.put("msg_type", 0);
            metadataRequestMap.put("piece", i);
            byte[] metadataRequestMapBytes = BencodingUtils.encode(metadataRequestMap);
            byte[] metadataRequestBytes = new byte[metadataRequestMapBytes.length + 6];
            metadataRequestBytes[4] = 20;
            metadataRequestBytes[5] = (byte) utMetadataValue;
            byte[] lenBytes = int2Bytes(metadataRequestMapBytes.length + 2);
            System.arraycopy(lenBytes, 0, metadataRequestBytes, 0, 4);
            System.arraycopy(metadataRequestMapBytes, 0, metadataRequestBytes, 6, metadataRequestMapBytes.length);
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(metadataRequestBytes));
        }
    }

    /**
     * 初始化byteBuf
     *
     * @param metadataSize
     */
    private void initResult(int metadataSize) {
        ByteBuf byteBuf = Unpooled.buffer(metadataSize >> 1, metadataSize);
        metaDataResultTask.setResult(byteBuf);
    }


    /**
     * 发送扩展消息
     *
     * @param ctx
     */
    private void sendExtendMessage(ChannelHandlerContext ctx) {
        Map<String, Object> extendMessageMap = new LinkedHashMap<>();
        Map<String, Object> extendMessageMMap = new LinkedHashMap<>();
        extendMessageMMap.put("ut_metadata", 1);
        extendMessageMap.put("m", extendMessageMMap);
        byte[] tempExtendBytes = BencodingUtils.encode(extendMessageMap);
        byte[] extendMessageBytes = new byte[tempExtendBytes.length + 6];
        extendMessageBytes[4] = 20;
        extendMessageBytes[5] = 0;
        byte[] lenBytes = int2Bytes(tempExtendBytes.length + 2);
        System.arraycopy(lenBytes, 0, extendMessageBytes, 0, 4);
        System.arraycopy(tempExtendBytes, 0, extendMessageBytes, 6, tempExtendBytes.length);
//			log.info("{}发送扩展消息:{}", infoHash, new String(extendMessageBytes, CharsetUtil.ISO_8859_1));
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(extendMessageBytes));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("处理连接异常:", cause.getCause());
        //关闭
        ctx.close();
    }

    private byte[] int2Bytes(int value) {
        byte[] des = new byte[4];
        des[3] = (byte) (value & 0xff);
        des[2] = (byte) ((value >> 8) & 0xff);
        des[1] = (byte) ((value >> 16) & 0xff);
        des[0] = (byte) ((value >> 24) & 0xff);
        return des;
    }

    public MetaDataExchangeHandler(MetaDataResultTask metaDataResultTask) {
        this.metaDataResultTask = metaDataResultTask;
    }
}