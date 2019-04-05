package com.github.euonmyoji.newhonor.api;

/**
 * @author yinyangshi
 */
public enum ArgLevel {
    /**
     * 无视错误
     */
    IGNORE,
    /**
     * 当头衔不为指定情况时发出警告
     */
    WARNING,
    /**
     * 当头衔不为指定情况时则丢出异常
     */
    ERROR
}
