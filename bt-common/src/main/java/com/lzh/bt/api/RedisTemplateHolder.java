package com.lzh.bt.api;

import org.springframework.data.redis.core.RedisTemplate;

public interface RedisTemplateHolder {


    /**
     * 获取redis-template
     *
     * @return
     */
    RedisTemplate<String, String> setRedisTemplate();
}
