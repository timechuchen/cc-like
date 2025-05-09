package com.chuchen.cclike.manager.cache;

import lombok.Data;

/**
 * 新增返回结果类
 * @param expelledKey 被挤出的 key
 * @param isHotKey    当前 key 是否进入 TopK
 * @param currentKey  当前操作的 key
 */
public record AddResult(String expelledKey, boolean isHotKey, String currentKey) {

}