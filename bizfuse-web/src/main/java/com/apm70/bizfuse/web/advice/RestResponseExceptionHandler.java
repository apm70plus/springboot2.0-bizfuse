package com.apm70.bizfuse.web.advice;

import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.UnknownHttpStatusCodeException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.apm70.bizfuse.exception.AccessDenyException;
import com.apm70.bizfuse.exception.BusinessException;
import com.apm70.bizfuse.web.advice.RestResponseExceptionHandler.HandlerCondition;
import com.apm70.bizfuse.web.exception.RestResponseException;
import com.apm70.bizfuse.web.response.RestResponse;
import com.apm70.bizfuse.web.response.ResultError;

@Conditional(HandlerCondition.class)
@ControllerAdvice
public class RestResponseExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RestResponseExceptionHandler.class);

    @Autowired
    protected MessageSource messageSource;

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(final Exception ex, final Object body,
            final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        final String method = ((ServletWebRequest) request).getHttpMethod().toString();
        final String url = ((ServletWebRequest) request).getRequest().getRequestURL().toString();

        RestResponseExceptionHandler.logger.error(method + " " + url, ex);

        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
            final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        final ArrayList<ResultError> errors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            //final String objectName = error.getObjectName();
            final String defaultMessage = error.getDefaultMessage();
            final String message = this.getLocalMessage(defaultMessage, defaultMessage);
            //final String field = error.getCode();
            errors.add(new ResultError(defaultMessage, message, null));
        });
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            final String objectName = error.getObjectName();
            final String defaultMessage = error.getDefaultMessage();
            final String field = error.getField();
            errors.add(new ResultError(objectName, defaultMessage, field));
        });

        final RestResponse<Void> resultDTO = RestResponse.failure(errors.toArray(new ResultError[0]));

        return new ResponseEntity<>(resultDTO, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            final MissingServletRequestParameterException ex, final HttpHeaders headers, final HttpStatus status,
            final WebRequest request) {
        final String parameterName = ex.getParameterName();
        final String parameterType = ex.getParameterType();
        final String message = ex.getMessage();

        final RestResponse<Void> resultDTO = RestResponse.failure(new ResultError(parameterType, message, parameterName));

        return new ResponseEntity<>(resultDTO, headers, status);
    }

    /**
     * 业务自定义异常处理 (BusinessRuntimeException)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> exceptionHandler(final BusinessException ex, final HttpServletRequest request) {
        final String method = request.getMethod();
        final String url = request.getRequestURL().toString();

        RestResponseExceptionHandler.logger.info(method + " " + url, ex);

        final String code = ex.getCode();
        final String message = this.getLocalMessage(code, ex.getMessage(), ex.getParams());

        RestResponseExceptionHandler.logger.info("[" + code + "] - " + message);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final RestResponse<Void> resultDTO = RestResponse.failure(new ResultError(code, message, null));

        return new ResponseEntity<>(resultDTO, headers, HttpStatus.OK);
    }

    /**
     * RestTemplate 方法调用异常处理 (RestClientException)
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Object> exceptionHandler(final RestClientException ex, final HttpServletRequest request) {
        final String method = request.getMethod();
        final String url = request.getRequestURL().toString();

        RestResponseExceptionHandler.logger.error(method + " " + url, ex);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (ex instanceof HttpStatusCodeException) {
            final String statusCode = String.valueOf(((HttpStatusCodeException) ex).getStatusCode());
            final String statusText = ((HttpStatusCodeException) ex).getStatusText();
            final String responseBody = ((HttpStatusCodeException) ex).getResponseBodyAsString();

            RestResponseExceptionHandler.logger.error("[" + statusCode + "] - " + statusText);

            return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
        } else if (ex instanceof UnknownHttpStatusCodeException) {
            final String statusCode = String.valueOf(((UnknownHttpStatusCodeException) ex).getRawStatusCode());
            final String statusText = ((UnknownHttpStatusCodeException) ex).getStatusText();
            final String responseBody = ((UnknownHttpStatusCodeException) ex).getResponseBodyAsString();

            RestResponseExceptionHandler.logger.error("[" + statusCode + "] - " + statusText);

            return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
        }

        return new ResponseEntity<>(RestResponse.failure(), headers, HttpStatus.OK);
    }

    /**
     * 权限校验异常处理 (AccessDenyException)
     */
    @ExceptionHandler(AccessDenyException.class)
    public ResponseEntity<Object> exceptionHandler(final AccessDenyException ex, final HttpServletRequest request) {
        final String method = request.getMethod();
        final String url = request.getRequestURL().toString();

        RestResponseExceptionHandler.logger.info(method + " " + url, ex);

        final String message = this.getLocalMessage(ex.getCode(), ex.getMessage());

        RestResponseExceptionHandler.logger.info(message);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final RestResponse<Void> resultDTO = RestResponse.failure(new ResultError("403", message, null));

        return new ResponseEntity<>(resultDTO, headers, HttpStatus.OK);
    }

    @ExceptionHandler(RestResponseException.class)
    public ResponseEntity<Object> exceptionHandler(final RestResponseException ex,
            final HttpServletRequest request) {
        final String method = request.getMethod();
        final String url = request.getRequestURL().toString();

        RestResponseExceptionHandler.logger.info(method + " " + url, ex);

        final RestResponse<Void> resultDTO = RestResponse.failure(ex.getErrors());
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(resultDTO, headers, HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exceptionHandler(final Exception ex, final HttpServletRequest request) {
        final String method = request.getMethod();
        final String url = request.getRequestURL().toString();

        RestResponseExceptionHandler.logger.error(method + " " + url + " 请求发生异常.", ex);
        final ResponseStatus statusAnnotation = ex.getClass().getAnnotation(ResponseStatus.class);
        if (statusAnnotation != null) {
            final HttpStatus status = statusAnnotation.value();
            return new ResponseEntity<>(ex.getMessage(), status);
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (ex.getClass().getName().contains("Authentication")
                || ex.getClass().getName().contains("AccessDeniedException")) {
            return new ResponseEntity<>(RestResponse.failure(new ResultError("403", "无访问权限", null)), headers,
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(RestResponse.failure(new ResultError("406", "服务异常", null)), headers, HttpStatus.OK);
    }

    protected String getLocalMessage(final String code, final String defaultMsg, final Object... params) {
        final Locale local = LocaleContextHolder.getLocale();
        return this.messageSource.getMessage(code, params, defaultMsg, local);
    }

    public static class HandlerCondition implements Condition {

        @Override
        public boolean matches(final ConditionContext paramConditionContext,
                final AnnotatedTypeMetadata paramAnnotatedTypeMetadata) {
            final String[] existingBeans =
                    paramConditionContext.getBeanFactory().getBeanNamesForType(RestResponseExceptionHandler.class);
            return (existingBeans == null) || (existingBeans.length == 0);
        }

    }
}
