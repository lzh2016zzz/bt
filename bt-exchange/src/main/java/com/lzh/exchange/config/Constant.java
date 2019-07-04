package com.lzh.exchange.config;

import io.netty.util.NettyRuntime;

public class Constant {

    //常量配置--------

    //每个节点信息默认占用的字节长度. 为20位nodeId,4位ip,2位port
    public static final Integer NODE_BYTES_LEN = 26;

    //每个peer信息占用的字节长度,4位ip + 2位port
    public static final Integer PEER_BYTES_LEN = 6;


    //nodeId和infohash的长度
    public static final Integer BASIC_HASH_LEN = 20;

    //获取种子元信息时,第一条握手信息的前缀, 28位byte. 第2-20位,是ASCII码的BitTorrent protocol,
    // 第一位19,是固定的,表示这个字符串的长度.后面八位是BT协议的版本.可以全为0,某些软件对协议进行了扩展,协议号不全为0,不必理会.
    public static final byte[] GET_METADATA_HANDSHAKE_PRE_BYTES = {19, 66, 105, 116, 84, 111, 114, 114, 101, 110, 116, 32, 112, 114,
            111, 116, 111, 99, 111, 108, 0, 0, 0, 0, 0, 16, 0, 1};


    //并发线程数配置
    public static final int NETTY_THREADS = NettyRuntime.availableProcessors() + 2;

    //metadata数据, 每一分片大小 16KB, 此处为byte
    public static final long METADATA_PIECE_SIZE = 16 << 10;

}
