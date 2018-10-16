package com.apm70.bizfuse.web.dto;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Auditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.apm70.bizfuse.web.response.ListResponse;
import com.apm70.bizfuse.web.response.PageResponse;
import com.apm70.bizfuse.web.response.RestResponse;
import com.apm70.bizfuse.web.support.LoginUserBeanGenerator;

/**
 * Model与DTO转换类
 */
public abstract class AbstractConvertor<Model, DTO> {

    @Autowired(required = false)
    protected LoginUserBeanGenerator currentUserFactoryBean;

    /**
     * 将 DTO 转换为 Model.
     *
     * @param dto 数据传输对象
     * @return
     */
    public abstract Model toModel(final DTO dto);

    /**
     * 将 Model 转换为 DTO.
     *
     * @param model 数据Model
     * @return
     */
    public DTO toDTO(final Model model) {
        return this.toDTO(model, false);
    }

    /**
     * 将 Model 转换为 DTO.
     *
     * @param model 数据Model
     * @param forListView 是否为列表视图做转换 true：列表视图 false：详细视图
     * @return
     */
    public abstract DTO toDTO(final Model model, boolean forListView);

    public final List<Model> toListModel(final List<DTO> dtoList, final Function<DTO, Model> toModelMapper) {
        final List<Model> modelList = dtoList.stream().map(toModelMapper).collect(Collectors.toList());
        return modelList;
    }

    public final List<Model> toListModel(final List<DTO> dtoList) {
        final List<Model> modelList = dtoList.stream().map(dto -> this.toModel(dto)).collect(Collectors.toList());
        return modelList;
    }

    public List<DTO> toListDTO(final List<Model> modelList, final Function<Model, DTO> toDTOMapper) {
        return modelList.stream().map(toDTOMapper).collect(Collectors.toList());
    }

    public final List<DTO> toListDTO(final List<Model> modelList) {
        final List<DTO> dtoList = modelList.stream().map(model -> this.toDTO(model, true)).collect(Collectors.toList());
        return dtoList;
    }

    public final Page<DTO> toPageDTO(final Page<Model> modelPage, final Function<Model, DTO> toDTOMapper) {
        final List<Model> modelList = modelPage.getContent();
        final List<DTO> dtoList = this.toListDTO(modelList, toDTOMapper);
        final long totalElements = modelPage.getTotalElements();
        final Page<DTO> dtoPage = new PageImpl<>(dtoList, this.getPageable(modelPage), totalElements);
        return dtoPage;
    }

    public final Page<DTO> toPageDTO(final Page<Model> modelPage) {
        final List<Model> modelList = modelPage.getContent();
        final List<DTO> dtoList = this.toListDTO(modelList);
        final long totalElements = modelPage.getTotalElements();
        final Page<DTO> dtoPage = new PageImpl<>(dtoList, this.getPageable(modelPage), totalElements);
        return dtoPage;
    }

    public final RestResponse<DTO> toResultDTO(final Model model, final Function<Model, DTO> toDTOMapper) {
        final DTO dto = toDTOMapper.apply(model);
        final RestResponse<DTO> resultDTO = RestResponse.success(dto);
        return resultDTO;
    }

    public final RestResponse<DTO> toResultDTO(final Model model) {
        final DTO dto = (model == null) ? null : this.toDTO(model);
        final RestResponse<DTO> resultDTO = RestResponse.success(dto);
        return resultDTO;
    }

    public final ListResponse<DTO> toResultDTO(final List<Model> modelList, final Function<Model, DTO> toDTOMapper) {
        final List<DTO> dtoList = this.toListDTO(modelList, toDTOMapper);
        final ListResponse<DTO> resultDTO = ListResponse.success(dtoList);
        return resultDTO;
    }

    public final ListResponse<DTO> toResultDTO(final List<Model> modelList) {
        final List<DTO> dtoList = this.toListDTO(modelList);
        final ListResponse<DTO> resultDTO = ListResponse.success(dtoList);
        return resultDTO;
    }

    public final PageResponse<DTO> toResultDTO(final Page<Model> modelPage, final Function<Model, DTO> toDTOMapper) {
        final List<DTO> dtoList = this.toListDTO(modelPage.getContent(), toDTOMapper);
        final PageResponse<DTO> resultDTO = PageResponse.success(dtoList, modelPage);
        return resultDTO;
    }

    public final PageResponse<DTO> toResultDTO(final Page<Model> modelPage) {
        final List<DTO> dtoList = this.toListDTO(modelPage.getContent());
        final PageResponse<DTO> resultDTO = PageResponse.success(dtoList, modelPage);
        return resultDTO;
    }

    protected final void loadAuditToDTO(final Auditable<String, Long, LocalDateTime> model, final AbstractAuditDTO dto) {
    	    model.getCreatedBy().ifPresent(dto::setCreatedBy);
    	    model.getLastModifiedBy().ifPresent(dto::setLastModifiedBy);
    	    model.getCreatedDate().ifPresent(dto::setCreatedDate);
    	    model.getLastModifiedDate().ifPresent(dto::setLastModifiedDate);
    }

    protected Pageable getPageable(final Page<Model> modelPage) {
        try {
            final Field pageableField = PageImpl.class.getSuperclass().getDeclaredField("pageable");
            pageableField.setAccessible(true);
            return (Pageable) pageableField.get(modelPage);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
        }
        return null;
    }

    /**
     * 获取当前登录用户
     *
     * @return 返回当前登录用户， 未登录（对于允许匿名访问的API）则返回null
     */
    protected Object getCurrentUser() {
        if (this.currentUserFactoryBean == null) {
            return null;
        }
        return this.currentUserFactoryBean.getLoginUser();
    }
}
