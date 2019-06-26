package com.lzh.dhtserver.netty.entity;

import lombok.Data;

import java.net.InetSocketAddress;

/***
 * DHT 节点
 *
 **/
@Data
public class Node {

	private byte[] nodeId;
	private InetSocketAddress addr;

	public Node(byte[] nodeId, InetSocketAddress addr) {
		this.nodeId = nodeId;
		this.addr = addr;
	}
}
