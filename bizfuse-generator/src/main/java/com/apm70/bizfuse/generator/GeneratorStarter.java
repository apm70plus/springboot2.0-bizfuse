package com.apm70.bizfuse.generator;

import com.apm70.bizfuse.generator.impl.ControllerGenerator;
import com.apm70.bizfuse.generator.impl.ConvertorGenerator;
import com.apm70.bizfuse.generator.impl.DtoGenerator;
import com.apm70.bizfuse.generator.impl.RepositoryGenerator;
import com.apm70.bizfuse.generator.impl.ServiceGenerator;
import com.apm70.bizfuse.generator.impl.ServiceImplGenerator;
import com.apm70.bizfuse.generator.utils.Configuration;

public class GeneratorStarter {

    public static void main(final String[] args) throws ClassNotFoundException {
        if (args.length < 2) {
            System.out.println("请按照如下方式配置启动参数:");
            System.out.println("1. Model包路径: modelBasePackage=com.apm70.bizfuse.generator.model");
            System.out.println("2. Model数组，逗号分隔: models=User,Member,Role");
            System.out.println("3. 输出方式，默认控制台打印， file则输出到文件: output=file");
            System.out.println("4. 是否覆盖原文件，输出方式为file时才生效。默认不覆盖原文件: overrid=true");
            System.out.println("*****************参考配置*******************");
            System.out.println("modelBasePackage=com.apm70.bizfuse.generator.model");
            System.out.println("models=User,Member,Role");
            System.out.println("output=file");
            System.out.println("override=true");
            System.out.println("*****************参考配置*******************");
            return;
        }
        final String basePackage = args[0].trim().replace("modelBasePackage=", "");
        final String[] entities = args[1].trim().replace("models=", "").split(",");
        String output = "console";
        if (args.length >= 3) {
            output = args[2].trim().replace("output=", "").trim();
            if (!"file".equals(output)) {
                output = "console";
            }
        }
        boolean override = false;
        if ("file".equals(output) && (args.length >= 4)) {
            final String overrideCommand = args[3].trim().replace("override=", "").trim();
            if ("true".equals(overrideCommand)) {
                override = true;
            }
        }
        GeneratorStarter.instance().generateCode(basePackage, entities, OutputType.valueOf(output), override);
    }
    
    private static GeneratorStarter instance;
    
    public static GeneratorStarter instance() {
    		if (instance == null) {
    			instance = new GeneratorStarter();
    		}
    		return instance;
    }
    
    private GeneratorStarter() {
    }
    
    /**
     * 生成代码
     * 
     * @param basePackage Entity的包路径
     * @param entities Entity名称列表
     * @param outputType 输出类型，console | file
     * @param override 是否覆盖旧代码
     * @throws ClassNotFoundException 
     */
    public void generateCode(String basePackage, String[] entities, OutputType outputType, boolean override) throws ClassNotFoundException {
    	    final String outputDir = null;
        for (final String modelClass : entities) {
            final Class<?> modelClazz = Class.forName(basePackage + "." + modelClass);
            final Configuration config = new Configuration(modelClazz, outputType.name(), outputDir, override);
            final RepositoryGenerator repositoryGenerator = new RepositoryGenerator(config);
            final ServiceGenerator serviceGenerator = new ServiceGenerator(config);
            final ServiceImplGenerator serviceImplGenerator = new ServiceImplGenerator(config);
            final DtoGenerator dtoGenerator = new DtoGenerator(config);
            final ConvertorGenerator convertorGenerator = new ConvertorGenerator(config);
            final ControllerGenerator controllerGenerator = new ControllerGenerator(config);
            repositoryGenerator.generate();
            serviceGenerator.generate();
            serviceImplGenerator.generate();
            dtoGenerator.generate();
            convertorGenerator.generate();
            controllerGenerator.generate();
        }
    }
    
    public static enum OutputType {
    	console, file;
    }
}
