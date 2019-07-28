package com.lzh.dhtserver.common.entity;

import com.lzh.dhtserver.common.util.SystemClock;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

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
        if (infoHash != null) {
            this.infoHash = Arrays.copyOf(infoHash, infoHash.length);
        }
        timestamp = SystemClock.now();
    }

    public void setInfoHash(byte[] infoHash) {
        if (infoHash == null) {
            this.infoHash = null;
        } else this.infoHash = Arrays.copyOf(infoHash, infoHash.length);
    }

    public byte[] getInfoHash() {
        if (this.infoHash == null) {
            return null;
        }
        return Arrays.copyOf(this.infoHash, this.infoHash.length);
    }
}
