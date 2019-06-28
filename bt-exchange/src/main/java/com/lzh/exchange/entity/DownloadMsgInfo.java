package com.lzh.exchange.entity;

import lombok.Data;

import java.io.Serializable;

/***
 * Peer Wire 下载消息信息
 **/
@Data
public class DownloadMsgInfo implements Serializable {

	private String ip;
	private int port;
	private String infoHash;
	private long timestamp;

}
