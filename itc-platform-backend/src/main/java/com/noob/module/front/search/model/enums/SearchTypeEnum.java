package com.noob.module.front.search.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName SearchTypeEnum
 * @Description 聚合搜索类型枚举
 * @Author holic-x
 * @Date 2024/4/27 11:00
 */
public enum SearchTypeEnum {
    POST("帖子", "articles"),
    USER("用户", "users"),
    PICTURE("图片", "pictures"),
    INTERFACE_INFO("图片", "interfaces"),
    VIDEO("视频", "videos");

    private final String text;

    private final String value;

    SearchTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static SearchTypeEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (SearchTypeEnum anEnum : SearchTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
