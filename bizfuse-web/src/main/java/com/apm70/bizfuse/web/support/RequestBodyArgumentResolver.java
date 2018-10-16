package com.apm70.bizfuse.web.support;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.h2.util.IOUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.apm70.bizfuse.web.annotation.BodyVariable;
import com.apm70.bizfuse.web.utils.json.JsonUtils;

/**
 * Allows resolving the Simple type in RequestBody.
 *
 * @author liuyg
 */
public final class RequestBodyArgumentResolver implements HandlerMethodArgumentResolver {

	private static final String BODY_VALUES = "body$json$values";

	private static final Set<Class<?>> BASE_TYPES;
	static {
		BASE_TYPES = new HashSet<>();
		BASE_TYPES.addAll(Stream.of(
				String.class, 
				Integer.class, 
				Byte.class, 
				Long.class, 
				Float.class, 
				Double.class, 
				Short.class,
				BigDecimal.class, 
				BigInteger.class, 
				Boolean.class, 
				Date.class).collect(Collectors.toList()));
	}

	@Override
	public boolean supportsParameter(final MethodParameter parameter) {
		final BodyVariable bodyVariable = parameter.getParameterAnnotation(BodyVariable.class);
		if (bodyVariable == null)
			return false;
		if (!this.isBaseDataType(parameter.getParameterType())) {
			return false;
		}
		return true;
	}

	@Override
	public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer,
			final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, Object> body = (Map<String, Object>) webRequest
				.getAttribute(RequestBodyArgumentResolver.BODY_VALUES, RequestAttributes.SCOPE_REQUEST);
		if (body == null) {
			final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			IOUtils.copy(request.getInputStream(), out);
			body = JsonUtils.jsonToMap(out.toString());
			webRequest.setAttribute(RequestBodyArgumentResolver.BODY_VALUES, body, RequestAttributes.SCOPE_REQUEST);
		}
		final Object value = body.get(parameter.getParameterName());
		if (value == null) {
			return value;
		}
		final Class<?> type = parameter.getParameterType();
		if (type.isAssignableFrom(value.getClass())) {
			return value;
		}
		if (type.equals(Byte.class)) {
			return new Byte(String.valueOf(value));
		} else if (type.equals(Long.class)) {
			return new Long(String.valueOf(value));
		} else if (type.equals(Double.class)) {
			return new Double(String.valueOf(value));
		} else if (type.equals(Float.class)) {
			return new Float(String.valueOf(value));
		} else if (type.equals(Short.class)) {
			return new Short(String.valueOf(value));
		} else if (type.equals(BigDecimal.class)) {
			return new BigDecimal(String.valueOf(value));
		} else if (type.equals(BigInteger.class)) {
			return new BigInteger(String.valueOf(value));
		} else if (type.equals(Date.class)) {
			return new Date((Long) value);
		}
		return value;
	}

	private boolean isBaseDataType(final Class<?> clazz) {
		return (clazz.isPrimitive() || BASE_TYPES.contains(clazz));
	}
}
