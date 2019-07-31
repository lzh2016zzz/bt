package com.lzh.bt.api.repository;

import com.lzh.bt.api.entity.Metadata;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetadataRepository extends ElasticsearchRepository<Metadata, String> {


}
