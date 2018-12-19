package com.apm70.bizfuse.generator.impl;

import java.util.HashMap;
import java.util.Map;

import com.apm70.bizfuse.generator.AbstractGenerator;
import com.apm70.bizfuse.generator.ContentsFilter;
import com.apm70.bizfuse.generator.utils.Configuration;

public class ControllerGenerator extends AbstractGenerator {

    // 模板路径
    private static final String templatePath = "/codetemplate/Controller.template";

    private ContentsFilter filter;

    private final String templateContents;

    public ControllerGenerator(final Configuration config) {
        super(config, "controller");
        this.initFilter();
        this.templateContents = this.getFileString(ControllerGenerator.templatePath);
    }

    @Override
    public void generate() {
        final String value = this.filter.filter(this.templateContents);
        this.output(value);
    }

    private void initFilter() {
        final Map<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("@Package@", this.getPackage("controller"));
        filterMap.put("@ModelPath@", this.getModelPath());
        filterMap.put("@Model@", this.getModelName());
        filterMap.put("@model@", this.getModelNameWithHeadLow());
        filterMap.put("@DTOPath@", this.getClassPath("dto"));
        filterMap.put("@ConvertorPath@", this.getClassPath("convertor"));
        filterMap.put("@ServicePath@", this.getClassPath("service"));
        filterMap.put("@RepositoryPath@", this.getClassPath("repository"));
        this.filter = new ReplaceFilter(filterMap);
    }
}
