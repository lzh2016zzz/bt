package com.lzh.exchange.common.entity;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;


@Data
@Builder
public class Metadata {


    /**
     * info-hash hexadecimal
     */

    private String infoHash;


    /**
     * is the metadata only one file
     */
    private boolean single;

    /**
     * metadata name
     */
    private String name;

    /**
     * multi files info.
     * <br/>
     * example : <br/>[
     * {
     * "length": 693195,
     * "path": [
     * "3-13-2012 5-23-31 PM.jpg"
     * ]
     * },
     * {
     * "length": 676387,
     * "path": [
     * "3-3-2012 4-32-48 PM.jpg"
     * ]
     * }]
     */
    private String multiFile;


    /**
     * suffixes
     * <br/>
     * use ','separate
     * <br/>
     * example : jpg,png,flac,mp4
     */
    private String suffixes;

    /**
     * files total length
     */

    private Long length;


    /**
     * hot
     */
    private Long hot;

    /**
     * createTime
     */
    private ZonedDateTime create = ZonedDateTime.now();



}
