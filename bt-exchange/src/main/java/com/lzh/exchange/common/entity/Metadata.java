package com.lzh.exchange.common.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


//@Entity
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
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
