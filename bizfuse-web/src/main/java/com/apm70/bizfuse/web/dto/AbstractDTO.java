package com.apm70.bizfuse.web.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;

public abstract class AbstractDTO implements Serializable {

    private static final long serialVersionUID = -5828841103727023345L;

    @ApiModelProperty(value = "主键ID", position = 0)
    private Long id;

    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @JsonIgnore
    public boolean isNew() {
        return this.id == null;
    }
}
