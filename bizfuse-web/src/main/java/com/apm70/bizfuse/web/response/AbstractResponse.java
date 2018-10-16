package com.apm70.bizfuse.web.response;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
public abstract class AbstractResponse implements Serializable {

    private static final long serialVersionUID = 3874081007408979058L;

    /**
     * 请求的处理结果状态
     */
    public enum Status {
        /**
         * 成功 或 失败
         */
        success, failure;
    }

    /**
     * 处理结果状态
     */
    @ApiModelProperty(value = "结果状态（成功 或 失败）", position = 0)
    protected Status status;

    /**
     * 错误消息
     */
    @ApiModelProperty(value = "异常信息", position = 10)
    @JsonInclude(Include.NON_NULL)
    protected ResultError[] errors;

    /**
     * 时间戳
     */
    @ApiModelProperty(value = "系统处理时间戳（增量拉数据时使用）", position = 11)
    protected Date timestamp;

    ///////////////////////////////////////
    // Getter
    ///////////////////////////////////////
    @JsonProperty(value = "status", index = 0)
    public Status getStatus() {
        return this.status;
    }

    @JsonProperty(value = "errors", index = 1)
    public ResultError[] getErrors() {
        return this.errors;
    }

    ///////////////////////////////////////
    // Setter
    ///////////////////////////////////////
    protected void setErrors(final ResultError... errors) {
        this.errors = errors;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }
    
    @JsonIgnore
    public boolean isFailure() {
        return Status.failure == this.status;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return Status.success == this.status;
    }

    @JsonIgnore
    public String errorsToString() {
        if ((this.errors != null) && (this.errors.length > 0)) {
            final StringBuilder builder = new StringBuilder();
            builder.append("Errors : [");
            for (final ResultError error : this.errors) {
                builder.append(error.toString());
            }
            builder.append("]");
            return builder.toString();
        } else {
            return "errors : []";
        }
    }
}
