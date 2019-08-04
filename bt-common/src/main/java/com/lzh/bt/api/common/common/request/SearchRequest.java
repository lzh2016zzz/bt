package com.lzh.bt.api.common.common.request;

import lombok.Getter;
import lombok.Setter;

/***
 * 搜索请求
 **/
@Getter @Setter
public class SearchRequest {

	private String fileName;
	private String fileType;
	private String sortBy;
	private String order;

	private Integer page;
	private Integer limit = 20;

	public SearchRequest() {
	}
}
