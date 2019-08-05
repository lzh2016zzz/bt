package com.lzh.bt.api;

import com.lzh.bt.api.entity.Constant;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Optional;

public abstract class AbstractServerContext implements HotCounter, SuccessMetaSetOps {


    @Override
    public Long getHotIncrement(Object hex) {
        return redisTemplate().opsForValue().increment(Constant.INFO_HASH_HEX_HOT + hex, 1L);
    }

    @Override
    public Long getHot(Object hex) {
        return Optional.ofNullable(NumberUtils.createLong(redisTemplate().opsForValue().get(Constant.INFO_HASH_HEX_HOT + hex))).orElse(0L);
    }

    @Override
    public boolean hexSaved(Object hex) {
        return redisTemplate().boundSetOps(Constant.SUCCESS_INFO_HASH_HEX).isMember(hex);
    }

    @Override
    public boolean markHexSaved(Object hex) {
        if (hex != null) {
            redisTemplate().opsForSet().add(Constant.SUCCESS_INFO_HASH_HEX, String.valueOf(hex));
            return true;
        }
        return false;
    }
}
