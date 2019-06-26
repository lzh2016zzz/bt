package com.lzh.dhtserver.common.entity;

import com.lzh.dhtserver.common.util.SystemClock;
import lombok.Data;

import java.io.Serializable;

/***
 * Peer Wire 下载消息信息
 **/
@Data
public class DownloadMsgInfo implements Serializable {

	private String ip;
	private int port;
	private byte[] infoHash;
	private long timestamp;

	public DownloadMsgInfo(String ip, int port, byte[] infoHash) {
		this.ip = ip;
		this.port = port;
		this.infoHash = infoHash;
		timestamp = SystemClock.now();
	}
}
