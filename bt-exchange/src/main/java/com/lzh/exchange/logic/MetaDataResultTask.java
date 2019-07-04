package com.lzh.exchange.logic;

import com.alibaba.fastjson.JSON;
import com.lzh.exchange.common.entity.Metadata;
import com.lzh.exchange.common.util.bencode.BencodingUtils;
import io.netty.channel.ChannelFuture;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class MetaDataResultTask {

	/**
	 *
	 */
	private byte[] result;

	private Consumer<Metadata> successCallBack;

	private Consumer<Throwable> failureCallBack;

	private Supplier<ChannelFuture> future;


	private MetaDataResultTask() {

	}

	//apis

	/**
	 *
	 * @param callBack
	 * @return
	 */
	public MetaDataResultTask success(Consumer<Metadata> callBack){
		this.successCallBack = callBack;
		return this;
	}


	public MetaDataResultTask failure(Consumer<Throwable> callBack){
		this.failureCallBack = callBack;
		return this;
	}

	public void start(){
		if(successCallBack != null) {
			future.get();
		}
	}

	//bt-client logic

	protected MetaDataResultTask future(Supplier<ChannelFuture> future){
		this.future = future;
		return this;
	}


	protected static MetaDataResultTask metaDataResult(){
		MetaDataResultTask metaDataResult = new MetaDataResultTask();
		return metaDataResult;
	}

	protected byte[] getResult() {
		return result;
	}

	protected void setResult(byte[] result) {
		this.result = result;
	}

	protected void doSuccess(){
		Optional.ofNullable(this.result)
				.filter(k -> !ArrayUtils.isEmpty(k))
				.map(this::bytes2Metadata)
				.ifPresent(metadata -> successCallBack.accept(metadata));
	}

	protected void doFailure(Supplier<Throwable> caught){
		Optional.ofNullable(this.failureCallBack)
				.ifPresent(cb -> cb.accept(caught.get()));
	}

	/**
	 * byte[] 转 {@link Metadata}
	 */
	@SuppressWarnings("unchecked")
	private Metadata bytes2Metadata(byte[] bytes) {
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
}