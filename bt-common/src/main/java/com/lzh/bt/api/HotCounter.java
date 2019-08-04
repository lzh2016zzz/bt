package com.lzh.bt.api;


public interface HotCounter {

    Long getHotIncrement(Object hex);

    Long getHot(Object hex);
}
