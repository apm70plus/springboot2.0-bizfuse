package com.apm70.bizfuse.web.support;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.apm70.bizfuse.support.Searchable;
import com.apm70.bizfuse.util.CommonUtils;

/**
 * Allows resolving the {@link Searchable}.
 *
 * @author liuyg
 */
public final class SearchableArgumentResolver implements HandlerMethodArgumentResolver {

    // 查询条件的默认前缀
    private static final String DEFAULT_PREFIX = "s_";

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return Searchable.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {
        final Searchable searchable = new Searchable();
        webRequest.getParameterNames().forEachRemaining(paramName -> {
            if (paramName.startsWith(SearchableArgumentResolver.DEFAULT_PREFIX)) {
                final String[] values = webRequest.getParameterValues(paramName);
                if (values.length == 1) {
                    String value = values[0];
                    if (CommonUtils.hasUrlEncoded(value)) {
                        value = CommonUtils.decodeURI(value);
                    }
                    searchable.put(paramName.substring(SearchableArgumentResolver.DEFAULT_PREFIX.length()), value);
                } else {
                    for (int i = 0; i < values.length; i++) {
                        if (CommonUtils.hasUrlEncoded(values[i])) {
                            values[i] = CommonUtils.decodeURI(values[i]);
                        }
                    }
                    searchable.put(paramName.substring(SearchableArgumentResolver.DEFAULT_PREFIX.length()), values);
                }
            }
        });
        return searchable;
    }
}
