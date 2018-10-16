package com.apm70.bizfuse.web.response;

import java.util.List;

import org.springframework.data.domain.Page;

import com.apm70.bizfuse.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class PageResponse<T> extends ListResponse<T> {

    private static final long serialVersionUID = -1575401489882908012L;
    /**
     * 分页信息
     */
    @ApiModelProperty(value = "分页信息", position = 2)
    private PageData pageable;

    ///////////////////////////////////////
    // Getter
    ///////////////////////////////////////
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(value = "data", index = 2)
    @Override
    public List<T> getData() {
        return super.getData();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(value = "pageable", index = 3)
    public PageData getPageable() {
        return this.pageable;
    }

    ///////////////////////////////////////
    // Setter
    ///////////////////////////////////////
    public void setPageable(final PageData pageable) {
        this.pageable = pageable;
    }

    ///////////////////////////////////////
    // Constructor
    ///////////////////////////////////////
    public PageResponse() {
    }

    PageResponse(final Status status) {
        super(status);
    }

    ///////////////////////////////////////
    // Builder
    ///////////////////////////////////////
    public static <T> PageResponse<T> success(final Page<T> pageData) {
        if (pageData == null) {
            throw new BusinessException("NullPointerException", "The formal parameter 'pageData' cannot be null");
        }

        final PageResponse<T> result = new PageResponse<>(Status.success);
        result.setData(pageData.getContent());
        result.setPageable(PageData.convert(pageData));
        return result;
    }

    public static <T> PageResponse<T> success(final List<T> listData, final Page<?> pageData) {
        if (pageData == null) {
            throw new BusinessException("NullPointerException", "The formal parameter 'pageData' cannot be null");
        }

        final PageResponse<T> result = new PageResponse<>(Status.success);
        result.setData(listData);
        result.setPageable(PageData.convert(pageData));
        return result;
    }
}
