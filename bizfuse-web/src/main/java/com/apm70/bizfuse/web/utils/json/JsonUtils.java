package com.apm70.bizfuse.web.utils.json;

import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.apm70.bizfuse.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JSON 工具类。
 */
public class JsonUtils {
    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    /** Map type */
    public static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };
    /** 普通对象 Mapper */
    private static ObjectMapper mapper;
    /** 可过滤的对象 Mapper */
    private static ObjectMapper filterMapper;

    static {
        JsonUtils.mapper = new ObjectMapper();
        JsonUtils.mapper.setSerializationInclusion(Include.ALWAYS);
        JsonUtils.mapper.setVisibility(JsonUtils.mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        JsonUtils.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonUtils.mapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        JsonUtils.mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        JsonUtils.mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    /**
     * 私有构造函数。
     */
    private JsonUtils() {
    };

    /**
     * 对象实例转JSON字符串。
     *
     * @param pojo 对象实例
     * @param <T> 对象类型
     * @return 转换的JSON字符串
     */
    public static <T> String pojoToJson(final T pojo) {
        if (pojo == null) {
            return null;
        }
        try {
            final String json = JsonUtils.getMapper().writeValueAsString(pojo);
            return json;
        } catch (final IOException e) {
            throw new BusinessException("Failed to convert Object2JSONString. ", e);
        }
    }

    /**
     * JSON字符串转对象实例。
     *
     * @param json JSON串
     * @param pojoClass 对象类型
     * @param <T> 对象类型
     * @return 转换的对象实例
     */
    public static <T> T jsonToPojo(final String json, final Class<T> pojoClass) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return JsonUtils.getMapper().readValue(json, pojoClass);
        } catch (final IOException e) {
            throw new BusinessException("Failed to convert JSONString2Object. ", e);
        }
    }

    public static <T> List<T> jsonToPojoList(final String json, final TypeReference<List<T>> valueTypeRef) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return JsonUtils.getMapper().readValue(json, valueTypeRef);
        } catch (final IOException e) {
            throw new BusinessException("Failed to convert JSONString2Object. ", e);
        }
    }

    /**
     * JSON字符串转对象实例。
     *
     * @param src 数据流Reader
     * @param pojoClass 对象类型
     * @param <T> 对象类型
     * @return 转换的对象实例
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public static <T> T jsonToPojo(final Reader src, final Class<T> pojoClass) throws JsonParseException,
            JsonMappingException, IOException {
        return JsonUtils.getMapper().readValue(src, pojoClass);
    }

    @SuppressWarnings("unchecked")
    public static <T> T jsonToPojo(final String json, final TypeReference<T> valueTypeRef) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return (T) JsonUtils.getMapper().readValue(json, valueTypeRef);
        } catch (final IOException e) {
            throw new BusinessException("Failed to convert JSONString2Object. ", e);
        }
    }

    /**
     * JSON字符串转Map。
     *
     * @param json JSON串
     * @return 转换的Map实例
     */
    public static Map<String, Object> jsonToMap(final String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return JsonUtils.getMapper().readValue(json, JsonUtils.MAP_TYPE);
        } catch (final IOException e) {
            throw new BusinessException("Failed to convert JSONString2Map. ", e);
        }
    }

    /**
     * 对象之间的转换。
     *
     * @param sourceObject 源对象
     * @param targetType 目标对象
     * @param <T> 对象类型
     * @return 转换的对象实例
     */
    public static <T> T pojoToPojo(final T sourceObject, final Class<T> targetType) {
        if (sourceObject == null) {
            return null;
        }
        return JsonUtils.getMapper().convertValue(sourceObject, targetType);
    }

    /**
     * 对象实例转Map。
     *
     * @param pojo 对象实例
     * @param <T> 对象类型
     * @return 转换的Map实例
     */
    public static <T> Map<String, Object> pojoToMap(final T pojo) {
        if (pojo == null) {
            return null;
        }
        return JsonUtils.getMapper().convertValue(pojo, JsonUtils.MAP_TYPE);
    }

    /**
     * 按指定项目将对象实例转为Map。
     *
     * @param pojo 对象实例
     * @param fields 要转换的项目
     * @param <T> 对象类型
     * @return 转换的Map实例
     */
    public static <T> Map<String, Object> pojoToMapWithSpecifiedFields(final T pojo, final String... fields) {
        final String json = JsonUtils.pojoToJsonWithSpecifiedFields(pojo, fields);
        return JsonUtils.jsonToMap(json);
    }

    /**
     * 按过滤条件将对象实例转为Map。
     *
     * @param pojo 对象实例
     * @param fields 要转换的项目
     * @param <T> 对象类型
     * @return 转换的Map实例
     */
    public static <T> Map<String, Object> pojoToMapWithFilterFields(final T pojo, final String... fields) {
        final String json = JsonUtils.pojoToJsonWithFilterFields(pojo, fields);
        return JsonUtils.jsonToMap(json);
    }

    /**
     * 按指定项目将对象实例转为Map。
     *
     * @param pojo 对象实例
     * @param filterId 过滤器Id（若设定Id，则Id必须跟POJO类的注解@JsonFilter的Id一致）
     * @param fields 要转换的项目
     * @param <T> 对象类型
     * @return 转换的Map实例
     */
    public static <T> Map<String, Object> pojoToMapWithSpecifiedFields(final T pojo, final String filterId,
            final String... fields) {
        final String json = JsonUtils.pojoToJsonWithSpecifiedFields(pojo, filterId, fields);
        return JsonUtils.jsonToMap(json);
    }

    /**
     * 按过滤条件将对象实例转为Map。
     *
     * @param pojo 对象实例
     * @param filterId 过滤器Id（若设定Id，则Id必须跟POJO类的注解@JsonFilter的Id一致）
     * @param fields 要过滤的项目
     * @param <T> 对象类型
     * @return 转换的Map实例
     */
    public static <T> Map<String, Object> pojoToMapWithFilterFields(final T pojo, final String filterId,
            final String... fields) {
        final String json = JsonUtils.pojoToJsonWithFilterFields(pojo, filterId, fields);
        return JsonUtils.jsonToMap(json);
    }

    /**
     * 按指定项目将对象实例转为JSON字符串。
     *
     * @param pojo 对象实例
     * @param fields 要转换的项目
     * @param <T> 对象类型
     * @return 转换的JSON字符串
     */
    public static <T> String pojoToJsonWithSpecifiedFields(final T pojo, final String... fields) {
        if (pojo == null) {
            return null;
        }
        return JsonUtils.pojoToJsonWithSpecifiedFields(pojo, pojo.getClass().getName(), fields);
    }

    /**
     * 按过滤条件将对象实例转为JSON字符串。
     *
     * @param pojo 对象实例
     * @param fields 要转换的项目
     * @param <T> 对象类型
     * @return 转换的JSON字符串
     */
    public static <T> String pojoToJsonWithFilterFields(final T pojo, final String... fields) {
        if (pojo == null) {
            return null;
        }
        return JsonUtils.pojoToJsonWithFilterFields(pojo, pojo.getClass().getName(), fields);
    }

    /**
     * 按指定项目将对象实例转为JSON字符串。
     *
     * @param pojo 对象实例
     * @param filterName 过滤器Id（若设定Id，则Id必须跟POJO类的注解@JsonFilter的Id一致）
     * @param fields 要转换的项目
     * @param <T> 对象类型
     * @return 转换的JSON字符串
     */
    public static <T> String pojoToJsonWithSpecifiedFields(final T pojo, String filterName, final String... fields) {
        if (pojo == null) {
            return null;
        }
        if (StringUtils.isEmpty(filterName)) {
            filterName = pojo.getClass().getName();
        }
        return JsonUtils.pojoToJson(pojo, filterName, SimpleBeanPropertyFilter.filterOutAllExcept(fields));
    }

    /**
     * 按过滤条件将对象实例转为JSON字符串。
     *
     * @param pojo 对象实例
     * @param filterName 过滤器Id（若设定Id，则Id必须跟POJO类的注解@JsonFilter的Id一致）
     * @param fields 要过滤掉的项目
     * @param <T> 对象类型
     * @return 转换的JSON字符串
     */
    public static <T> String pojoToJsonWithFilterFields(final T pojo, final String filterName, final String... fields) {
        if (pojo == null) {
            return null;
        }
        String filter = filterName;
        if (StringUtils.isEmpty(filter)) {
            filter = pojo.getClass().getName();
        }
        return JsonUtils.pojoToJson(pojo, filter, SimpleBeanPropertyFilter.serializeAllExcept(fields));
    }

    /**
     * 根据指定的过滤器将对象实例转换为Map。
     *
     * @param pojo 对象实例
     * @param filter Bean属性过滤器
     * @param <T> 对象类型
     * @return 转换的Map
     */
    public static <T> Map<String, Object> pojoToMap(final T pojo, final PropertyFilter filter) {
        if (pojo == null) {
            return null;
        }
        final String json = JsonUtils.pojoToJson(pojo, pojo.getClass().getName(), filter);
        return JsonUtils.jsonToMap(json);
    }

    /**
     * 根据指定的过滤器将对象实例转换为Map。
     *
     * @param pojo 对象实例
     * @param filterName 过滤器Id（若设定Id，则Id必须跟POJO类的注解@JsonFilter的Id一致）
     * @param filter Bean属性过滤器
     * @param <T> 对象类型
     * @return 转换的Map
     */
    public static <T> Map<String, Object> pojoToMap(final T pojo, final String filterName,
            final PropertyFilter filter) {
        if (pojo == null) {
            return null;
        }
        final String json = JsonUtils.pojoToJson(pojo, filterName, filter);
        return JsonUtils.jsonToMap(json);
    }

    /**
     * 根据指定的过滤器将对象实例转换为JSON串。
     *
     * @param pojo 对象实例
     * @param filter Bean属性过滤器
     * @param <T> 对象类型
     * @return 转换的JSON字符串
     */
    public static <T> String pojoToJson(final T pojo, final PropertyFilter filter) {
        if (pojo == null) {
            return null;
        }
        return JsonUtils.pojoToJson(pojo, pojo.getClass().getName(), filter);
    }

    /**
     * 根据指定的过滤器将对象实例转换为JSON串。
     *
     * @param pojo 对象实例
     * @param filterName 过滤器Id（若设定Id，则Id必须跟POJO类的注解@JsonFilter的Id一致）
     * @param filter Bean属性过滤器
     * @param <T> 对象类型
     * @return 转换的JSON字符串
     */
    public static <T> String pojoToJson(final T pojo, String filterName, final PropertyFilter filter) {
        if (StringUtils.isEmpty(filterName)) {
            filterName = pojo.getClass().getName();
        }
        final FilterProvider filters = new SimpleFilterProvider().addFilter(filterName, filter);
        try {
            final String json = JsonUtils.getFilterMapper().writer(filters).writeValueAsString(pojo);
            if (JsonUtils.logger.isDebugEnabled()) {
                JsonUtils.logger.debug("pojoToJson :" + json);
            }
            return json;
        } catch (final IOException e) {
            throw new BusinessException("Failed to convert Object2JSONString. ", e);
        }
    }

    /**
     * 获取 ObjectMapper 实例。
     *
     * @return ObjectMapper实例
     */
    private static ObjectMapper getMapper() {
        if (JsonUtils.mapper != null) {
            return JsonUtils.mapper;
        }
        synchronized (JsonUtils.class) {
            if (JsonUtils.mapper != null) {
                return JsonUtils.mapper;
            }
            JsonUtils.mapper = new ObjectMapper();
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            mapper.registerModule(javaTimeModule);
            //JsonUtils.mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            return JsonUtils.mapper;
        }
    }

    /**
     * 获取可过滤的 ObjectMapper 实例。
     *
     * @return ObjectMapper实例
     */
    private static ObjectMapper getFilterMapper() {
        if (JsonUtils.filterMapper != null) {
            return JsonUtils.filterMapper;
        }
        synchronized (JsonUtils.class) {
            if (JsonUtils.filterMapper != null) {
                return JsonUtils.filterMapper;
            }
            JsonUtils.filterMapper = new ObjectMapper();
            JsonUtils.filterMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            return JsonUtils.filterMapper;
        }
    }
}
