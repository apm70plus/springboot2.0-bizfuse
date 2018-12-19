package com.apm70.bizfuse.generator.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ModelProperties {

    private final List<ModelProperty> properties = new ArrayList<ModelProperty>();

    public ModelProperties(final Class<?> clazz) {
        final Field fields[] = clazz.getDeclaredFields();
        for (final Field field : fields) {
            if ((field.getModifiers() & Modifier.STATIC) != 0) {
                continue;
            }

            final String fieldName = field.getName();
            final String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getMethodName = "get" + firstLetter + fieldName.substring(1);
            String setMethodName = "set" + firstLetter + fieldName.substring(1);

            if (field.getType().toString().contains("boolean")) {
                if (fieldName.startsWith("is")) {
                    getMethodName = fieldName;
                    setMethodName = "set" + fieldName.substring(2);
                } else {
                    getMethodName = "is" + firstLetter + fieldName.substring(1);
                }
            }
            final Annotation[] annotations = field.getDeclaredAnnotations();
            final ModelProperty property =
                    new ModelProperty(fieldName, getMethodName, setMethodName, field.getType(), annotations);
            this.properties.add(property);
        }
    }

    public List<ModelProperty> getProperties() {
        return this.properties;
    }

    public static class ModelProperty {
        private final String name;
        private final String getter;
        private final String setter;
        private final Class<?> type;
        private final Annotation[] annotations;

        public ModelProperty(final String name, final String readMethod, final String writeMethod,
                final Class<?> type, final Annotation[] annotations) {
            super();
            this.name = name;
            this.getter = readMethod;
            this.setter = writeMethod;
            this.type = type;
            this.annotations = annotations;
        }

        public String getName() {
            return this.name;
        }

        public String getGetter() {
            return this.getter;
        }

        public String getSetter() {
            return this.setter;
        }

        public Class<?> getType() {
            return this.type;
        }

        public Annotation[] getAnnotations() {
            return this.annotations;
        }
    }
}
