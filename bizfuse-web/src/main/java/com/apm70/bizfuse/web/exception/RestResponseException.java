package com.apm70.bizfuse.web.exception;

import com.apm70.bizfuse.web.response.ResultError;

/**
 * Rest请求结果运行时异常<br>
 * 主要为了包装RestResponse中的异常
 *
 * @author liuyg
 */
public class RestResponseException extends RuntimeException {

    private static final long serialVersionUID = 7913293827793560253L;

    private ResultError[] errors;

    public RestResponseException(final ResultError[] errors, final String message) {
        super(message);
        this.setErrors(errors);
    }

    public ResultError[] getErrors() {
        return this.errors;
    }

    public void setErrors(final ResultError[] errors) {
        this.errors = errors;
    }
}
