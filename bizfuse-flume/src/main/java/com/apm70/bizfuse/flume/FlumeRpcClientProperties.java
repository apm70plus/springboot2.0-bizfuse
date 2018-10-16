package com.apm70.bizfuse.flume;

import java.util.Properties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FlumeRpcClientProperties {
    private String storePath;
    private Properties flumeProp;
}
