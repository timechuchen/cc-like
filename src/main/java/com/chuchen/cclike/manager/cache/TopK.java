package com.chuchen.cclike.manager.cache;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @author chuchen
 * @date 2025/5/9 10:35
 * @description 根据实现 HeavyKeeper 接口，实现 TopK 算法
 */
public interface TopK {

    AddResult add(String key, int value);

    /**
     * 返回当前 TopK 元素的列表
     */
    List<Item> list();

    /**
     * 获取被挤出的元素的队列
     */
    BlockingQueue<Item> expelled();

    /**
     * 对所有计数进行衰减
     */
    void fading();

    long total();
}
