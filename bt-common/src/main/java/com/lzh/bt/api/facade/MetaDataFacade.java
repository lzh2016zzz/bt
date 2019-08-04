package com.lzh.bt.api.facade;

import com.lzh.bt.api.entity.Metadata;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MetaDataFacade {


    /**
     * 按照名称和后缀查询
     *
     * @param name     名称
     * @param suffix   后缀
     * @param pageable 分页参数
     * @return
     */
    ResponseEntity<List<Metadata>> queryByNamePage(String name, String suffix, Pageable pageable);
}
