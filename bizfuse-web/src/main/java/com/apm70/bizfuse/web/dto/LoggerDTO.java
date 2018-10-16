package com.apm70.bizfuse.web.dto;

import java.io.Serializable;

import ch.qos.logback.classic.Logger;

public class LoggerDTO implements Serializable {

    private static final long serialVersionUID = 8440876576743143810L;

    public LoggerDTO(final Logger logger) {
        this.name = logger.getName();
        this.level = logger.getEffectiveLevel().toString();
    }

    private String name;
    private String level;

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(final String level) {
        this.level = level;
    }
}
