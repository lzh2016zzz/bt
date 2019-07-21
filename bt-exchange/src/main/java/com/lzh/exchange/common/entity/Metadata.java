package com.lzh.exchange.common.entity;

import lombok.Data;


@Data
public class Metadata {



    /**
     * infoHash信息,16进制形式
     */
    
    private String infoHash;


    /**
     * 是否单文件
     */
    private boolean single;

    /**
     * 名字
     */
    
    private String name;


    /**
     * 文件名字. 多个用;分隔
     */
    private String fileName;


    /**
     * 后缀名. 多个用;分隔
     */
    private String suffixes;

    /**
     * 总长度(所有文件相加长度)
     */
    
    private Long length;


    /**
     * 热度
     */
    
    private Long hot = 0L;


    /**
     * 种子类型
     */
    private Integer type;


}
