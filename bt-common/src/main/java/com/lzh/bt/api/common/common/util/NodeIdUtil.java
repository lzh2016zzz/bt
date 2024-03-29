package com.lzh.bt.api.common.common.util;

import java.util.Random;

/***
 * NodeId 生成工具
 **/
public class NodeIdUtil {

	public static byte[] createRandomNodeId() {
		Random random = new Random();
		byte[] r = new byte[20];
		random.nextBytes(r);
		return r;
	}

	public static byte[] getNeighbor(byte[] nodeId, byte[] info_hash) {
		byte[] bytes = new byte[20];
		System.arraycopy(info_hash, 0, bytes, 0, 10);
		System.arraycopy(nodeId, 10, bytes, 10, 10);
		return bytes;
	}

}
