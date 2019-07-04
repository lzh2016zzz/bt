package com.lzh.exchange.common.constant;

import com.alibaba.fastjson.JSON;
import com.lzh.exchange.common.entity.Metadata;
import com.lzh.exchange.common.util.bencode.BencodingUtils;
import io.netty.channel.ChannelFuture;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class MetaDataResultTask {

	/**
	 * 结果
	 */
	private byte[] result;

	private final CountDownLatch latch;

	private Consumer<Metadata> successCallBack;

	private long timeUnit = 0L;

	private TimeUnit unit = TimeUnit.SECONDS;

	private Supplier<ChannelFuture> future;

	private boolean async = false;

	private MetaDataResultTask(CountDownLatch latch) {
		this.latch = latch;
	}


	public MetaDataResultTask future(Supplier<ChannelFuture> future){
		this.future = future;
		return this;
	}


	public static MetaDataResultTask metaDataResult(){
		MetaDataResultTask metaDataResult = new MetaDataResultTask(new CountDownLatch(1));
		return metaDataResult;
	}

	/**
	 * 设置获取结果的超时时间，如果超过指定时间，则会直接返回结果。
	 * 默认情况下会一直阻塞直到线程被中断
	 * @param timeUnit 时间
	 * @param unit 单位
	 * @return
	 */
	public MetaDataResultTask await(long timeUnit, TimeUnit unit){
		this.timeUnit = timeUnit;
		this.unit = unit;
		return this;
	}

	public MetaDataResultTask success(Consumer<Metadata> callBack){
		this.successCallBack = callBack;
		return this;
	}

	public void start(){
		try {
			if(successCallBack != null) {
				future.get();
				if (timeUnit != 0) {
					this.latch.await(this.timeUnit, this.unit);
				} else {
					this.latch.await();
				}
				Optional.ofNullable(this.result)
						.filter(k -> !ArrayUtils.isEmpty(k))
						.map(this::bytes2Metadata)
						.ifPresent(metadata -> successCallBack.accept(metadata));
			}
		} catch (InterruptedException e) {
			log.error("回调异常",e);
		}
	}

	/**
	 * byte[] 转 {@link Metadata}
	 */
	@SuppressWarnings("unchecked")
	public Metadata bytes2Metadata(byte[] bytes) {
		try {
			String metadataStr = new String(bytes, CharsetUtil.UTF_8);
			String metadataBencodeStr = metadataStr.substring
					(0, metadataStr.indexOf("6:pieces")) + "e";
			Map<String, ?> resultMap = BencodingUtils.decode(metadataBencodeStr.getBytes(CharsetUtil.UTF_8));
			log.info("metaData信息 ： {}", JSON.toJSON(resultMap));
		} catch (Exception e) {
			log.error("[bytes2Metadata]失败.e:", e.getMessage(), e);
		}
		return null;
	}




	public byte[] getResult() {
		return result;
	}

	public void setResult(byte[] result) {
		this.result = result;
	}

	public void awake(){
		this.latch.countDown();
	}
}