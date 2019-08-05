package com.lzh.bt.api;


/**
 * 通过hex获取热度
 */
public interface HotCounter {

    /**
     * 获取热度并 + 1
     *
     * @param hex
     * @return
     */
    Long getHotIncrement(Object hex);

    /**
     * 获取热度
     *
     * @param hex
     * @return
     */
    Long getHot(Object hex);
}
