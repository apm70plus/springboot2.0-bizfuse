package com.apm70.bizfuse.web.dto;

import java.io.Serializable;
import java.util.HashMap;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class JsTreeNodeDTO implements Serializable {

    private static final long serialVersionUID = -4583615966620009555L;

    @ApiModelProperty(value = "父节点ID", position = 0)
    private Long parent;

    @NotNull
    @ApiModelProperty(value = "ID", position = 1)
    private Long id;

    @ApiModelProperty(value = "文本内容", position = 2)
    private String text;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final HashMap<String, Object> state = new HashMap<>();

    @JsonProperty
    public String getId() {
        return this.id.toString();
    }

    @JsonIgnore
    public void setId(final Long id) {
        this.id = id;
    }

    @JsonProperty
    public String getParent() {
        return this.parent != null ? this.parent.toString() : "#";
    }

    @JsonIgnore
    public void setParent(final Long parent) {
        this.parent = parent;
    }

    @JsonProperty
    public String getText() {
        return this.text;
    }

    @JsonIgnore
    public void setText(final String text) {
        this.text = text;
    }

    @JsonProperty
    public HashMap<String, Object> getState() {
        return this.state;
    }
}
