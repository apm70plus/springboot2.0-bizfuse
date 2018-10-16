package com.apm70.bizfuse.web.response;

import java.util.List;

import com.apm70.bizfuse.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class ListResponse<T> extends AbstractResponse {

    private static final long serialVersionUID = 8756487352760469154L;
    /**
     * 列表数据
     */
    @ApiModelProperty(value = "业务数据（List）", position = 1)
    protected List<T> data;

    ///////////////////////////////////////
    // Getter
    ///////////////////////////////////////
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(value = "data", index = 3)
    public List<T> getData() {
        return this.data;
    }

    ///////////////////////////////////////
    // Setter
    ///////////////////////////////////////
    public void setData(final List<T> data) {
        this.data = data;
    }

    ///////////////////////////////////////
    // Constructor
    ///////////////////////////////////////
    public ListResponse() {
    }

    ListResponse(final Status status) {
        this.status = status;
    }

    ///////////////////////////////////////
    // Builder
    ///////////////////////////////////////
    public static <T> ListResponse<T> success(final List<T> listData) {
        if (listData == null) {
            throw new BusinessException("NullPointerException", "The formal parameter 'listData' cannot be null");
        }

        final ListResponse<T> result = new ListResponse<>(Status.success);
        result.setData(listData);
        return result;
    }

    public static <T> ListResponse<T> failure(final List<T> listData, final ResultError... errors) {
        final ListResponse<T> result = new ListResponse<>(Status.failure);
        result.setData(listData);
        result.setErrors(errors);
        return result;
    }
}
