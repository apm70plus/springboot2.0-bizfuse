package com.apm70.bizfuse.web.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;

/**
 * 异常结果集
 *
 * @author liuyg
 */
public class ResultError implements Serializable {

    private static final long serialVersionUID = 8042733799291115991L;
    /**
     * 异常关联的字段
     */
    @ApiModelProperty(value = "字段名（字段校验类异常）", position = 0)
    private String field;
    /**
     * 异常消息
     */
    @ApiModelProperty(value = "异常消息", position = 1)
    private String errmsg;
    /**
     * 异常编码
     */
    @ApiModelProperty(value = "异常编码", position = 2)
    private String errcode;

    public ResultError() {
    }

    public ResultError(final String errmsg, final String field) {
        this.field = field;
        this.errmsg = errmsg;
    }

    public ResultError(final String errcode, final String errmsg, final String field) {
        this.field = field;
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public String getField() {
        return this.field;
    }

    public String getErrmsg() {
        return this.errmsg;
    }

    public String getErrcode() {
        return this.errcode;
    }

    public void setErrcode(final String errcode) {
        this.errcode = errcode;
    }

    public void setField(final String field) {
        this.field = field;
    }

    public void setErrmsg(final String errmsg) {
        this.errmsg = errmsg;
    }

    @Override
    public String toString() {
        return String.format("{errcode:%s, errmsg:%s, field:%s}", this.errcode, this.errmsg, this.field);
    }
}
