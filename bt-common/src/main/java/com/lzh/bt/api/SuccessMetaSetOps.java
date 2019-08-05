package com.lzh.bt.api;

public interface SuccessMetaSetOps extends RedisTemplateHolder {

    boolean hexSaved(Object hex);

    boolean markHexSaved(Object hex);
}
