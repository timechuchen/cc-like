package com.chuchen.cclike.model.enums;

import lombok.Getter;

/**
 * @author chuchen
 * @date 2025/5/8 18:26
 * @description 标识 Lua 脚本执行状态
 */
@Getter
public enum LuaStatusEnum {

    // 成功
    SUCCESS(1L),
    // 失败
    FAIL(-1L);

    private final Long value;

    LuaStatusEnum(long value) {
        this.value = value;
    }
}
