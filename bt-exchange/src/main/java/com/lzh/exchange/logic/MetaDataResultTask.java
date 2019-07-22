package com.lzh.exchange.logic;

import com.alibaba.fastjson.JSON;
import com.lzh.exchange.common.entity.Metadata;
import com.lzh.exchange.common.util.Bencode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class MetaDataResultTask {

    /**
     * decoder
     */
    private static Bencode bencode = new Bencode(CharsetUtil.UTF_8);
    /**
     * metadata buffer
     */
    private ByteBuf result;
    /**
     * get metadata success callback
     */
    private Consumer<Metadata> successCallBack;
    /**
     * get metadata failure callback
     */
    private Consumer<Throwable> failureCallBack;
    /**
     * query task
     */
    private Supplier<ChannelFuture> future;


    private MetaDataResultTask() {

    }

    //apis

    /**
     * @param callBack
     * @return
     */
    public MetaDataResultTask success(Consumer<Metadata> callBack) {
        this.successCallBack = callBack;
        return this;
    }


    public MetaDataResultTask failure(Consumer<Throwable> callBack) {
        this.failureCallBack = callBack;
        return this;
    }

    public void start() {
        if (successCallBack != null) {
            future.get();
        }
    }

    //bt-client logic

    protected MetaDataResultTask future(Supplier<ChannelFuture> future) {
        this.future = future;
        return this;
    }


    protected static MetaDataResultTask metaDataResult() {
        MetaDataResultTask metaDataResult = new MetaDataResultTask();
        return metaDataResult;
    }

    protected ByteBuf getResult() {
        return result;
    }

    protected void setResult(ByteBuf result) {
        this.result = result;
    }

    protected void doSuccess() {
        Optional.ofNullable(this.result)
                .filter(ByteBuf::hasArray)
                .map(ByteBuf::array)
                .map(this::bytes2Metadata)
                .ifPresent(metadata -> successCallBack.accept(metadata));
    }

    protected void doFailure(Supplier<Throwable> caught) {
        Optional.ofNullable(this.failureCallBack)
                .ifPresent(cb -> cb.accept(caught.get()));
    }

    /**
     * byte[] to {@link Metadata}
     */
    @SuppressWarnings("unchecked")
    private Metadata bytes2Metadata(byte[] bytes) {
        String metadataStr = new String(bytes, CharsetUtil.UTF_8);
        return Optional.of(metadataStr.indexOf("6:pieces"))
                .filter(k -> k != -1)
                .map(endIndex -> {
                    //metadata is dict type
                    String s = metadataStr.substring(0, endIndex) + "e";
                    Map<String, ?> resultMap = bencode.decode(s.getBytes(CharsetUtil.UTF_8), Map.class);
                    if (resultMap != null) {
                        /**
                         * multi-file
                         */
                        if (resultMap.get("files") != null) {

                            List<Map<String, Object>> fileList = (List<Map<String, Object>>) resultMap.get("files");

                            String name = (String) resultMap.get("name");

                            String joinSuffixes = fileList.stream()
                                    .map(files -> ((ArrayList<String>) files.get("path")))
                                    .filter(Objects::nonNull)
                                    .flatMap(Collection::stream)
                                    .map(str -> getSuffix(str))
                                    .filter(Objects::nonNull)
                                    .distinct().collect(Collectors.joining(","));

                            long totalLength = fileList.stream()
                                    .map(files -> (long) files.get("length"))
                                    .reduce(0L, Long::sum);

                            return Metadata.builder()
                                    .name(name)
                                    .suffixes(joinSuffixes)
                                    .multiFile(JSON.toJSONString(fileList))
                                    .length(totalLength)
                                    .single(false)
                                    .build();
                        } else {
                            /**
                             * single-file
                             */
                            String name = (String) resultMap.get("name");
                            Long length = (Long) resultMap.get("length");
                            return Metadata.builder()
                                    .name(name)
                                    .suffixes(getSuffix(name))
                                    .length(length)
                                    .single(true)
                                    .build();
                        }
                    }
                    return null;
                })
                .orElseThrow(() -> new RuntimeException(""));
    }

    private String getSuffix(String name) {
        int index = name.lastIndexOf(".");
        return index == -1 ? null : name.substring(index + 1).toLowerCase();
    }

}