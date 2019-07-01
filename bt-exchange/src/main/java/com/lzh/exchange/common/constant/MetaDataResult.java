package com.lzh.exchange.common.constant;

import lombok.Data;

import java.util.concurrent.CountDownLatch;

@Data
public class MetaDataResult {
	private byte[] result;
	private final CountDownLatch latch;

	public MetaDataResult(CountDownLatch latch) {
		this.latch = latch;
	}
}