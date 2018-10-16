package com.apm70.bizfuse.identity;

/**
 * 流水号ID生成管器接口。
 */
public interface IdGenerator {

    /**
     * 重置ID生成器，
     */
    public abstract void reset();

    /**
     * 生成ID。
     */
    public abstract long generate();

    /**
     * 生成字符ID
     *
     * @return
     */
    public abstract String generateCode();
    
    /**
     * 生成随机HEX数，常用做Token类。
     *
     * @return
     */
    public abstract String generateNonceToken();

}
