package com.lzh.bt.api.repository;

import com.lzh.bt.api.entity.Metadata;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetadataRepository extends ElasticsearchRepository<Metadata, String> {

    List<Metadata> findAllByNameContainingLikeAndSuffixesContainsOrderByHotDesc(String name, String suffixes,Pageable pageable);
}
