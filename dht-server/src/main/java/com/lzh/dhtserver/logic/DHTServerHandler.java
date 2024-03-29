package com.lzh.dhtserver.logic;

import com.alibaba.fastjson.JSON;
import com.lzh.bt.api.common.common.entity.DownloadMsgInfo;
import com.lzh.bt.api.common.common.util.NodeIdUtil;
import com.lzh.bt.api.common.common.util.bencode.BencodingUtils;
import com.lzh.dhtserver.logic.entity.Node;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.messaging.support.MessageBuilder;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/***
 * 参见 Bittorrent 协议：
 * http://www.bittorrent.org/beps/bep_0005.html
 **/
@Slf4j
@ChannelHandler.Sharable
public class DHTServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    protected static final byte[] emptyBytes = new byte[]{};

    protected DHTServerContextHolder dhtServerContext;

    private static final Charset defaultCharset = CharsetUtil.ISO_8859_1;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        byte[] buff = new byte[packet.content().readableBytes()];
        packet.content().readBytes(buff);

        Map<String, ?> map = BencodingUtils.decode(buff);

        if (map == null || map.get("y") == null)
            return;

        String y = new String((byte[]) map.get("y"), defaultCharset);

        if ("q".equals(y)) {            //请求 Queries
            onQuery(map, packet.sender());
        } else if ("r".equals(y)) {     //回复 Responses
            onResponse(map, packet.sender());
        }

    }

    /**
     * 解析查询请求
     *
     * @param map
     * @param sender
     */
    protected void onQuery(Map<String, ?> map, InetSocketAddress sender) {
        //transaction id 会话ID
        byte[] t = (byte[]) map.get("t");
        //query name: ping, find node, get_peers, announce_peer
        String q = new String((byte[]) map.get("q"), defaultCharset);
        //query params
        Map<String, ?> a = (Map<String, ?>) map.get("a");
        //log.info("on query, query name is {}", q);
        switch (q) {
            case "ping"://ping Query = {"t":"aa", "y":"q", "q":"ping", "a":{"id":"发送者ID"}}
                responsePing(t, sender);
                break;
            case "find_node"://find_node Query = {"t":"aa", "y":"q", "q":"find_node", "a": {"id":"abcdefghij0123456789", "target":"mnopqrstuvwxyz123456"}}
                responseFindNode(t, sender);
                break;
            case "get_peers"://get_peers Query = {"t":"aa", "y":"q", "q":"get_peers", "a": {"id":"abcdefghij0123456789", "info_hash":"mnopqrstuvwxyz123456"}}
                responseGetPeers(t, (byte[]) a.get("info_hash"), sender);
                break;
            case "announce_peer"://announce_peers Query = {"t":"aa", "y":"q", "q":"announce_peer", "a": {"id":"abcdefghij0123456789", "implied_port": 1, "info_hash":"mnopqrstuvwxyz123456", "port": 6881, "token": "aoeusnth"}}
                responseAnnouncePeer(t, a, sender);
                break;
            default:
                return;
        }
    }


    /**
     * 回复 ping 请求
     * Response = {"t":"aa", "y":"r", "r": {"id":"自身节点ID"}}
     *
     * @param t
     * @param sender
     */
    protected void responsePing(byte[] t, InetSocketAddress sender) {
        Map r = new HashMap<String, Object>();
        r.put("id", dhtServerContext.getSelfNodeId());
        DatagramPacket packet = createPacket(t, "r", r, sender);
        dhtServerContext.sendKRPC(packet);
        //log.info("response ping[{}]", sender);
    }

    /**
     * 回复 find_node 请求, 由于是模拟的 DHT 节点，所以直接回复一个空的 node 集合即可
     * Response = {"t":"aa", "y":"r", "r": {"id":"0123456789abcdefghij", "nodes": "def456..."}}
     *
     * @param t
     * @param sender
     */
    protected void responseFindNode(byte[] t, InetSocketAddress sender) {
        HashMap<String, Object> r = new HashMap<>();
        r.put("id", dhtServerContext.getSelfNodeId());
        r.put("nodes", emptyBytes);
        DatagramPacket packet = createPacket(t, "r", r, sender);
        dhtServerContext.sendKRPC(packet);
        //log.info("response find_node[{}]", sender);
    }

    /**
     * 回复 get_peers 请求，必须回复，不然收不到 announce_peer 请求
     * Response with closest nodes = {"t":"aa", "y":"r", "r": {"id":"abcdefghij0123456789", "token":"aoeusnth", "nodes": "def456..."}}
     *
     * @param t
     * @param sender
     */
    protected void responseGetPeers(byte[] t, byte[] info_hash, InetSocketAddress sender) {
        HashMap<String, Object> r = new HashMap<>();
        r.put("token", new byte[]{info_hash[0], info_hash[1]});
        r.put("nodes", emptyBytes);
        r.put("id", NodeIdUtil.getNeighbor(dhtServerContext.getSelfNodeId(), info_hash));
        DatagramPacket packet = createPacket(t, "r", r, sender);
        dhtServerContext.sendKRPC(packet);
        //log.info("response get_peers[{}]", sender);
    }

    /**
     * 处理对方响应内容，由于我们只主动给对方发送了 find_node 请求，所以只会收到 find_node 的回复进行解析即可
     * 解析出响应的节点列表再次给这些节点发送 find_node 请求，即可无限扩展与新的节点保持通讯（即把自己的节点加入到对方的桶里，
     * 欺骗对方让对方给自己发送 announce_peer 请求，这样一来我们就可以抓取 DHT 网络中别人正在下载的种子文件信息）
     *
     * @param map
     * @param sender
     */
    protected void onResponse(Map<String, ?> map, InetSocketAddress sender) {
        //transaction id
        byte[] t = (byte[]) map.get("t");
        //由于在我们发送查询 DHT 节点请求时，构造的查询 transaction id 为字符串 find_node（见 findNode 方法），所以根据字符串判断响应请求即可
        String type = new String(t, defaultCharset);
        if ("find_node".equals(type)) {
            //处理error
            Object r;
            if (map.get("e") == null && (r = map.get("r")) != null) {
                resolveNodes((Map) r);
            }
        }
//        else if ("ping".equals(type)) {
//
//        } else if ("get_peers".equals(type)) {
//
//        } else if ("announce_peer".equals(type)) {
//
//        }
    }

    /**
     * 回复 announce_peer 请求，该请求中包含了对方正在下载的 torrent 的 info_hash 以及 端口号
     * Response = {"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
     *
     * @param t
     * @param a      请求参数 a：
     *               {
     *               "id" : "",
     *               "implied_port": <0 or 1>,    //为1时表示当前自身的端口就是下载端口
     *               "info_hash" : "<20-byte infohash of target torrent>",
     *               "port" : ,
     *               "token" : "" //get_peer 中回复的 token，用于检测是否一致
     *               }
     * @param sender
     */
    protected void responseAnnouncePeer(byte[] t, Map a, InetSocketAddress sender) {

        byte[] info_hash = (byte[]) a.get("info_hash");
        byte[] token = (byte[]) a.get("token");
        int port;
        if (a.containsKey("implied_port") && ((BigInteger) a.get("implied_port")).shortValue() != 0) {
            port = sender.getPort();
        } else {
            port = ((BigInteger) a.get("port")).intValue();
        }

        HashMap<String, Object> r = new HashMap<>();
        r.put("id", NodeIdUtil.getNeighbor(dhtServerContext.getSelfNodeId(), info_hash));
        DatagramPacket packet = createPacket(t, "r", r, sender);
        dhtServerContext.sendKRPC(packet);
        // 将 info_hash 放进消息队列
        if (token.length == 2 && info_hash[0] == token[0] && info_hash[1] == token[1]) {
            String infoHashHEX = Hex.encodeHexString(Optional.ofNullable(info_hash).orElse(emptyBytes));
            //热度计数 + 1
            dhtServerContext.getHotIncrement(infoHashHEX);
            if (!dhtServerContext.hexSaved(infoHashHEX)) {
                //发送节点信息
                log.info("node{}[AP]:{}:{}", dhtServerContext.getUdpPort().getPort(), sender.getHostString(), port);
                dhtServerContext.getKafkaTemplate().send(MessageBuilder.withPayload(JSON.toJSONString(new DownloadMsgInfo(sender.getHostString(), port, info_hash))).build());
            }
        }
    }

    /**
     * 解析响应内容中的 DHT 节点信息
     *
     * @param r
     */
    protected Void resolveNodes(Map r) {

        byte[] nodes = (byte[]) r.get("nodes");

        if (nodes == null)
            return null;

        for (int i = 0; i < nodes.length; i += 26) {
            try {
                InetAddress ip = InetAddress.getByAddress(new byte[]{nodes[i + 20], nodes[i + 21], nodes[i + 22], nodes[i + 23]});
                InetSocketAddress address = new InetSocketAddress(ip, (0x0000FF00 & (nodes[i + 24] << 8)) | (0x000000FF & nodes[i + 25]));
                byte[] nid = new byte[20];
                System.arraycopy(nodes, i, nid, 0, 20);
                dhtServerContext.getNodesQueue().offer(new Node(nid, address));
                //log.info("get node address=[{}] ", address);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return null;
    }

    /**
     * 加入 DHT 网络
     */
    public void joinDHT() {
        for (InetSocketAddress addr : DHTServerContextHolder.BOOTSTRAP_NODES) {
            findNode(addr, null, dhtServerContext.getSelfNodeId());
        }
    }

    /**
     * 发送查询 DHT 节点请求
     *
     * @param address 请求地址
     * @param nid     请求节点 ID
     * @param target  目标查询节点
     */
    protected void findNode(InetSocketAddress address, byte[] nid, byte[] target) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("target", target);
        if (nid != null)
            map.put("id", NodeIdUtil.getNeighbor(dhtServerContext.getSelfNodeId(), nid));
        DatagramPacket packet = createPacket("find_node".getBytes(CharsetUtil.ISO_8859_1), "q", map, address);
        dhtServerContext.sendKRPC(packet);
    }

    /**
     * 构造 KRPC 协议数据
     *
     * @param t
     * @param y
     * @param arg
     * @return
     */
    protected DatagramPacket createPacket(byte[] t, String y, Map<String, Object> arg, InetSocketAddress address) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("t", t);
        map.put("y", y);
        if (!arg.containsKey("id"))
            arg.put("id", dhtServerContext.getSelfNodeId());

        if (y.equals("q")) {
            map.put("q", t);
            map.put("a", arg);
        } else {
            map.put("r", arg);
        }
        byte[] buff = BencodingUtils.encode(map);
        DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(buff), address);
        return packet;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("an exception was fired, caught:", cause);
        //关闭
        ctx.close();
    }


    public void setDhtServerContext(DHTServerContextHolder dhtServerContext) {
        this.dhtServerContext = dhtServerContext;
    }
}
