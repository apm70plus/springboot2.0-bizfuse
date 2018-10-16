package com.apm70.bizfuse.exception;

/**
 * 禁止访问异常
 * <p>
 * 系统内权限校验不过的，返回该异常
 *
 * @author liuyg
 */
public class AccessDenyException extends RuntimeException {
    private static final long serialVersionUID = 6774083802665621769L;

    private final String code;

    public AccessDenyException(final String code) {
        this.code = code;
    }

    public AccessDenyException(final String code, final String defaultMessage) {
        super(defaultMessage);
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

}
