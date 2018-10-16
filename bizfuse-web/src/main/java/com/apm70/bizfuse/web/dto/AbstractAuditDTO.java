package com.apm70.bizfuse.web.dto;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;

/**
 * 包含审计信息的抽象 DTO 类
 */
public abstract class AbstractAuditDTO extends AbstractDTO {

    private static final long serialVersionUID = 132020791921886009L;

    /**
     * 创建者
     */
    @ApiModelProperty(value = "创建人", position = 100)
    private String createdBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", position = 101)
    private LocalDateTime createdDate;

    /**
     * 修改者
     */
    @ApiModelProperty(value = "最后修改人", position = 102)
    private String lastModifiedBy;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "最后修改时间", position = 103)
    private LocalDateTime lastModifiedDate;

    //////////////////////////////////////////////////
    /// Getter and Setter
    //////////////////////////////////////////////////
    public String getCreatedBy() {
        return this.createdBy;
    }

    public final void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return this.createdDate;
    }

    public final void setCreatedDate(final LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    public final void setLastModifiedBy(final String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public final void setLastModifiedDate(final LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
