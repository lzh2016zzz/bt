package com.lzh.dhtserver.common.vo;

import com.lzh.dhtserver.common.entity.Torrent;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter @Setter @Builder
public class TorrentPageVO implements Serializable {

	private List<Torrent> list;
	private Long total;
	private Integer page;
	private Integer limit;
}
