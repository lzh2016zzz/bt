package com.lzh.exchange.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.Mapping;

import java.io.Serializable;

@Getter @Setter @Builder @ToString
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "bt", type = "torrent")
@Mapping(mappingPath = "torrent_search_mapping.json")
public class Torrent implements Serializable {

    @Id
    private String infoHash;
    @Field(searchAnalyzer = "ik_smart",analyzer = "ik_smart")
    private String fileType;
    @Field(searchAnalyzer = "ik_smart",analyzer = "ik_smart")
    private String fileName;
    @Field(index = false)
    private long fileSize;
    @Field
    private long createDate;
    @Field(index = false)
    private String files;


}
