package com.apm70.bizfuse.exception;

/**
 * 运行时业务异常类
 *
 * @author liuyg
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = -6882178806561789418L;

    /**
     * 异常编码（可结合i18n实现异常消息国际化）
     */
    private final String code;
    /**
     * 可选的参数（i18n中需要动态替换的参数）
     */
    private Object[] params;

    public BusinessException(final String code) {
        this.code = code;
    }

    public BusinessException(final String code, final Object... params) {
        this.code = code;
        this.params = params;
    }

    public BusinessException(final String code, final String defaultMessage) {
        super(defaultMessage);
        this.code = code;
    }

    public BusinessException(final String code, final String defaultMessage, final Object... params) {
        super(defaultMessage);
        this.code = code;
        this.params = params;
    }

    public BusinessException(final String code, final String defaultMessage, final Throwable cause) {
        super(defaultMessage, cause);
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public Object[] getParams() {
        return this.params;
    }
}
