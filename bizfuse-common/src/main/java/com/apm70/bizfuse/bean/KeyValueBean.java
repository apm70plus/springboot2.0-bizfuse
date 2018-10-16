package com.apm70.bizfuse.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * 通用的key-value类型Bean
 */

@Setter
@Getter
public class KeyValueBean {

    public KeyValueBean() {
    }

    public KeyValueBean(final String key, final Object value) {
        this.key = key;
        this.value = value;
    }

    private String key;
    private Object value;
}
