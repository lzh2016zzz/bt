package com.lzh.bt.api.rest;

import com.lzh.bt.api.entity.Metadata;
import com.lzh.bt.api.facade.MetaDataFacade;
import com.lzh.bt.api.service.MetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/metaData")
public class MetaDataRest implements MetaDataFacade {

    @Autowired
    MetaDataService metaDataService;

    @GetMapping("/query/names/{name}/suffixes/{suffix}")
    public ResponseEntity<List<Metadata>> queryByNamePage(@PathVariable String name, @PathVariable String suffix, Pageable pageable) {
        return ResponseEntity.ok(metaDataService.query(name, suffix, pageable));
    }


}
