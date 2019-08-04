package com.lzh.bt.api;

import com.lzh.bt.api.entity.Constant;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;

public abstract class AbstractHotCounter implements HotCounter {

    private RedisTemplate<String, String> redisTemplate = setRedisTemplate();

    @Override
    public Long getHotIncrement(Object hex) {
        return redisTemplate.opsForValue().increment(Constant.INFO_HASH_HEX_HOT + hex, 1L);
    }

    @Override
    public Long getHot(Object hex) {
        return Optional.ofNullable(NumberUtils.createLong(redisTemplate.opsForValue().get(Constant.INFO_HASH_HEX_HOT + hex))).orElse(0L);
    }

    public abstract RedisTemplate<String, String> setRedisTemplate();

}
