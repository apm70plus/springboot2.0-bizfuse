package com.apm70.bizfuse.generator.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.apm70.bizfuse.generator.AbstractGenerator;
import com.apm70.bizfuse.generator.ContentsFilter;
import com.apm70.bizfuse.generator.utils.Configuration;

public class DtoGenerator extends AbstractGenerator {

    private ContentsFilter filter;

    private final String modelSource;

    public DtoGenerator(final Configuration config) {
        super(config, "dto");
        final String modelSource = this.getFileString(this.config.getModelSrcPath());
        this.modelSource = this.initApiDocs(modelSource);
        this.initFilter();
    }

    // 初始化API文档信息
    private String initApiDocs(final String source) {
        final Pattern pattern = Pattern.compile("(/\\*\\*\\s+\\*\\s)(.+)(\\s+\\*/)");
        String result = source;
        final Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            final String originalStr = matcher.group(0);
            final String docStr = matcher.group(2);
            result = result.replace(originalStr,
                    originalStr + System.lineSeparator() + "    @ApiModelProperty(\"" + docStr + "\")");
        }
        return result;
    }

    @Override
    public void generate() {
        final String value = this.filter.filter(this.modelSource);
        this.output(value);
    }

    private void initFilter() {
        final Map<String, String> filterMap = new HashMap<String, String>();
        final String packageStr = "package " + this.getPackage("dto") + ";" + System.lineSeparator()
                + "import com.apm70.bizfuse.web.dto.AbstractDTO;";
        filterMap.put("package.+;", packageStr);
        filterMap.put("import javax\\.persistence.+\\s+", "");
        filterMap.put("import com\\.apm70\\.bizfuse\\.jpa\\.domain.+",
                "import io.swagger.annotations.ApiModel;");
        filterMap.put("@Entity", "@ApiModel");
        filterMap.put("@Table.+\\s+", "");
        //filterMap.put("\\s.+serialVersionUID.+\\s+", "    private static final long serialVersionUID = 1L;");
        filterMap.put("\\n\\s+@Column.+", "");
        filterMap.put("\\n\\s+@Enumerated.+", "");
        filterMap.put("\\n\\s+@OneToOne.+", "");
        filterMap.put("\\n\\s+@OneToMany.+", "");
        filterMap.put("\\n\\s+@ManyToMany.+", "");
        filterMap.put("\\n\\s+@ManyToOne.+", "");
        filterMap.put("\\n\\s+@Table.+", "");
        filterMap.put("\\n\\s+@Temporal.+", "");
        final String dtoClass = "public class @Model@DTO extends AbstractDTO {".replace("@Model@",
                this.config.getModelClazz().getSimpleName());
        filterMap.put("public class.+\\{", dtoClass);
        this.filter = new ReplaceFilter(filterMap);
    }

}
