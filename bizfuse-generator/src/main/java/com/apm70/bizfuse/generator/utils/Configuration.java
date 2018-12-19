package com.apm70.bizfuse.generator.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class Configuration {

    /**
     * 父包
     */
    private final String parentPackage;
    /**
     * 子包
     */
    private final String childPackage;
    /**
     * 代码生成目标目录
     */
    private final String outputDir;
    /**
     * Model类
     */
    private final Class<?> modelClazz;
    /**
     * Model源码路径
     */
    private final String modelSrcPath;
    /**
     * Model属性
     */
    private final ModelProperties modelProperties;
    /**
     * 源码根目录
     */
    private final String srcRootPath;
    /**
     * 输出类型
     */
    private final String outputType;
    /**
     * 是否覆盖原文件
     */
    private final boolean override;

    public Configuration(final Class<?> modelClazz, final String outputType, final String outputDir,
            final boolean override) {
        this.modelClazz = modelClazz;
        this.modelProperties = new ModelProperties(modelClazz);
        this.outputDir = outputDir;
        this.outputType = outputType;
        this.override = override;

        final URL url = ClassLoader.getSystemClassLoader().getResource("./");
        File root;
        try {
            root = new File(URLDecoder.decode(url.getPath(), "utf-8"));
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("获取Model源码路径失败.", e);
        }
        final String classPath = root.getAbsolutePath();
        this.srcRootPath =
                classPath.substring(0, classPath.indexOf("target" + File.separator + "classes"))
                        + "src.main.java.".replace(".", File.separator);
        this.modelSrcPath = this.srcRootPath + this.modelClazz.getName().replace(".", File.separator) + ".java";
        final String modelFullname = modelClazz.getName();
        final int indexOfModel = modelFullname.indexOf(".model.");
        if (indexOfModel == -1) {
            throw new RuntimeException("Given modelName is invalid.");
        }
        final String modelName = modelClazz.getSimpleName();
        this.parentPackage = modelFullname.substring(0, indexOfModel);
        if ((indexOfModel + 7 + modelName.length()) < modelFullname.length()) {
            this.childPackage =
                    modelFullname.substring(indexOfModel + 7, modelFullname.length() - modelName.length() - 1);
        } else {
            this.childPackage = null;
        }
    }

    public String getParentPackage() {
        return this.parentPackage;
    }

    public String getOutputDir() {
        return this.outputDir;
    }

    public Class<?> getModelClazz() {
        return this.modelClazz;
    }

    public String getChildPackage() {
        return this.childPackage;
    }

    public ModelProperties getModelProperties() {
        return this.modelProperties;
    }

    public String getModelSrcPath() {
        return this.modelSrcPath;
    }

    public String getSrcRootPath() {
        return this.srcRootPath;
    }

    public String getOutputType() {
        return this.outputType;
    }

    public boolean isOverride() {
        return this.override;
    }
}
