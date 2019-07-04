package com.lzh.exchange.common.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;


//@Entity
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metadata {

    /**
     * mysql主键
     * 目前弃用
     */
//    @Id
//    @GeneratedValue
    private Long id;


    /**
     * infoHash信息,16进制形式
     */
    
    private String infoHash;

    /**
     * 名字
     */
    
    private String name;

    /**
     * 总长度(所有文件相加长度)
     */
    
    private Long length;



    /**
     * 热度
     */
    
    private Long hot = 0L;

    /**
     * es中的主键id
     */
    
    private String _id;

    /**
     * 种子类型
     */
    private Integer type;


    /**
     * 创建时间
     */
    
    private Date createTime = new Date();

    /**
     * 修改时间
     */
    private Date updateTime = new Date();

    /**
     * 文件信息 json
     */
    private String infoString;


    public Metadata(String infoHash, String infoString, String name, Long length, Integer type) {
        this.infoHash = infoHash;
        this.infoString = infoString;
        this.name = name;
        this.length = length;
        this.type = type;
    }
}
