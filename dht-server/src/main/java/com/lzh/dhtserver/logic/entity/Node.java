package com.lzh.dhtserver.logic.entity;

import lombok.Data;

import java.net.InetSocketAddress;
import java.util.Arrays;

/***
 * DHT 节点
 *
 **/
@Data
public class Node {

    private byte[] nodeId;
    private InetSocketAddress addr;

    public Node(byte[] nodeId, InetSocketAddress addr) {
        if (nodeId != null) {
            this.nodeId = Arrays.copyOf(nodeId, nodeId.length);
        }
        this.addr = addr;
    }

    public void setNodeId(byte[] nodeId) {
        if (nodeId == null) {
            this.nodeId = null;
        } else this.nodeId = Arrays.copyOf(nodeId, nodeId.length);
    }

    public byte[] getNodeId() {
        if (this.nodeId == null) {
            return null;
        }
        return Arrays.copyOf(this.nodeId, this.nodeId.length);
    }
}
