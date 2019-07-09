package com.lzh.exchange.logic;

import com.alibaba.fastjson.JSON;
import com.lzh.exchange.common.entity.Metadata;
import com.lzh.exchange.common.util.bencode.BencodingUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class MetaDataResultTask {

	/**
	 * metadata 元数据的缓冲区
	 */
	private ByteBuf result;

	/**
	 * 获取元数据成功回调
	 */
	private Consumer<Metadata> successCallBack;

	/**
	 * 获取元数据失败回调
	 */
	private Consumer<Throwable> failureCallBack;

	/**
	 * 查询任务
	 */
	private Supplier<ChannelFuture> future;

	/**
	 * 严格模式 开启时,会对peer返回的数据大小做校验 必须和metaDataSize相等
	 */
	private boolean strictMode = false;

	private CountDownLatch countDownLatch = new CountDownLatch(1);
	//TODO :超时后是否直接执行


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

	protected ByteBuf getResult() {
		return result;
	}

	protected void setResult(ByteBuf result) {
		this.result = result;
	}

	protected void doSuccess(){
		Optional.ofNullable(this.result)
				.filter(ByteBuf::hasArray)
				.map(ByteBuf::array)
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