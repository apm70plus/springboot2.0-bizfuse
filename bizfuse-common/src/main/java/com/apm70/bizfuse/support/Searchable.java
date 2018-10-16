package com.apm70.bizfuse.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询条件
 *
 * @author liuyg
 */
public class Searchable {

    private final Map<String, Object> params = new HashMap<String, Object>();

    public boolean isEmpty() {
        return this.params.isEmpty();
    }

    public boolean hasKey(final String key) {
        return this.params.containsKey(key);
    }

    public String getStrValue(final String key) {
        final Object value = this.params.get(key);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    public Integer getIntValue(final String key) {
        final Object value = this.params.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        final Integer intValue = Integer.valueOf(String.valueOf(value));
        return intValue;
    }

    public Long getLongValue(final String key) {
        final Object value = this.params.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        final Long longValue = Long.valueOf(String.valueOf(value));
        return longValue;
    }

    public Double getDoubleValue(final String key) {
        final Object value = this.params.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        final Double doubleValue = Double.valueOf(String.valueOf(value));
        return doubleValue;
    }

    public Boolean getBooleanValue(final String key) {
        final Object value = this.params.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.valueOf(String.valueOf(value));
    }

    public String[] getStrArray(final String key) {
        final Object value = this.params.get(key);
        if (value == null) {
            return null;
        }
        if (value.getClass().isArray()) {
            return (String[]) value;
        } else if (List.class.isAssignableFrom(value.getClass())) {
            @SuppressWarnings("unchecked")
            final List<Object> list = (List<Object>) value;
            final String[] array = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = String.valueOf(list.get(i));
            }
            return array;
        } else {
            return new String[] {String.valueOf(value) };
        }
    }

    public boolean isArrayValue(final String key) {
        final Object value = this.params.get(key);
        if (value == null) {
            return false;
        }
        return value.getClass().isArray()
                || List.class.isAssignableFrom(value.getClass());
    }

    public void put(final String key, final Object value) {
        this.params.put(key, value);
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder();
        str.append("{");
        this.params.entrySet().stream().forEach(entry -> {
            if (str.length() > 1) {
                str.append(",");
            }
            str.append(entry.getKey()).append(":").append(entry.getValue());
        });
        str.append("}");
        return str.toString();
    }

    @SuppressWarnings("unchecked")
    public String toQueryString() {
        final StringBuilder str = new StringBuilder();
        this.params.entrySet().stream().forEach(entry -> {
            final Object value = entry.getValue();
            if (value.getClass().isArray()) {
                final Object[] values = (Object[]) value;
                for (final Object obj : values) {
                    if (str.length() > 1) {
                        str.append("&");
                    }
                    str.append("s_").append(entry.getKey()).append("=").append(String.valueOf(obj));
                }
            } else if (Collection.class.isAssignableFrom(value.getClass())) {
                for (final Object obj : (Collection<Object>) value) {
                    if (str.length() > 1) {
                        str.append("&");
                    }
                    str.append("s_").append(entry.getKey()).append("=").append(String.valueOf(obj));
                }
            } else {
                if (str.length() > 1) {
                    str.append("&");
                }
                str.append("s_").append(entry.getKey()).append("=").append(String.valueOf(value));
            }
        });
        return str.toString();
    }
}
