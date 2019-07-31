package com.lzh.bt.api.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;


@Data
@Builder
@Document(indexName = "bt", type = "MetaInfo")
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Metadata implements Serializable {

    /**
     * info-hash hexadecimal
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String infoHash;

    /**
     * is the metadata only one file
     */
    @Field(type = FieldType.Keyword)
    private boolean single;

    /**
     * metadata name
     */
    @Field(searchAnalyzer = "ik_smart", analyzer = "ik_max_word")
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
    @Field(type = FieldType.Text)
    private String multiFile;


    /**
     * suffixes
     * <br/>
     * use ','separate
     * <br/>
     * example : jpg,png,flac,mp4
     */
    @Field(searchAnalyzer = "ik_smart", analyzer = "ik_smart")
    private String suffixes;

    /**
     * files total length
     */
    @Field(type = FieldType.Long)
    private Long length;


    /**
     * hot
     */
    @Field(type = FieldType.Long)
    private Long hot;

    /**
     * createTime
     */
    @Field(type = FieldType.Long)
    private Long create;


}
