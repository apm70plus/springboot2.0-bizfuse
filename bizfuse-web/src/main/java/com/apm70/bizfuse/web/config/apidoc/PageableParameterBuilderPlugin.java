package com.apm70.bizfuse.web.config.apidoc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import springfox.documentation.schema.ModelReference;
import springfox.documentation.schema.ResolvedTypes;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
@Profile("swagger")
public class PageableParameterBuilderPlugin implements ParameterBuilderPlugin {
    private final TypeNameExtractor nameExtractor;
    private final TypeResolver resolver;

    @Autowired
    public PageableParameterBuilderPlugin(final TypeNameExtractor nameExtractor, final TypeResolver resolver) {
        this.nameExtractor = nameExtractor;
        this.resolver = resolver;
    }

    @Override
    public boolean supports(final DocumentationType delimiter) {
        return true;
    }

    private Function<ResolvedType, ? extends ModelReference> createModelRefFactory(final ParameterContext context) {
        final ModelContext modelContext = ModelContext.inputParam("page", context.resolvedMethodParameter().getParameterType(),
                context.getDocumentationType(),
                context.getAlternateTypeProvider(),
                context.getGenericNamingStrategy(),
                context.getIgnorableParameterTypes());
        return ResolvedTypes.modelRefFactory(modelContext, this.nameExtractor);
    }

    @Override
    public void apply(final ParameterContext context) {
        final ResolvedMethodParameter parameter = context.resolvedMethodParameter();
        final ResolvedType type = parameter.getParameterType();
        if ((type != null) && type.isInstanceOf(Pageable.class)) {
            final Function<ResolvedType, ? extends ModelReference> factory =
                    this.createModelRefFactory(context);

            final ModelReference intModel = factory.apply(this.resolver.resolve(Integer.TYPE));
            final ModelReference stringModel = factory.apply(this.resolver.resolve(List.class, String.class));

            final List<Parameter> parameters = Lists.newArrayList(
                    context.parameterBuilder()
                            .parameterType("query").name("page").modelRef(intModel)
                            .description("Page number of the requested page")
                            .build(),
                    context.parameterBuilder()
                            .parameterType("query").name("size").modelRef(intModel)
                            .description("Size of a page")
                            .build(),
                    context.parameterBuilder()
                            .parameterType("query").name("sort").modelRef(stringModel).allowMultiple(true)
                            .description("Sorting criteria in the format: property(,asc|desc). "
                                    + "Default sort order is ascending. "
                                    + "Multiple sort criteria are supported.")
                            .build());

            context.getOperationContext().operationBuilder().parameters(parameters);
        }
    }

}
