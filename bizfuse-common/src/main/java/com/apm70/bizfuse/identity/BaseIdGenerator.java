package com.apm70.bizfuse.identity;

import java.util.Random;

import org.springframework.util.DigestUtils;

/**
 * 基础ID生成类。
 */
public abstract class BaseIdGenerator implements IdGenerator {
    protected final long siteId;
    protected final String prefix;

    private static final Random RANDOM = new Random();

    /**
     * 支持分布式的构造函数。 <br>
     * 每个节点需要使用不同的SiteId参数，共支持0~9个集群节点。
     */
    public BaseIdGenerator(final long siteId, final String prefix) {
        if ((siteId < 0) || (siteId > 99)) {
            throw new IllegalArgumentException("SiteId must be in [0, 91].");
        }
        this.siteId = siteId;
        this.prefix = prefix;
    }

    /**
     * 初始化。
     */
    protected abstract void prepare();

    protected abstract long high();

    protected abstract long low();

    @Override
    public synchronized long generate() {
        this.prepare();
        return this.high() + this.low();
    }

    // 0 -- 9, A -- Z
    //为了分辨度，    去掉了79:O, 73:I
    protected static final char[] CHAR_TABLE = new char[] {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69,
            70, 71, 72, 74, 75, 76, 77, 78, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90 };
    protected static final int CHAR_TABLE_SIZE = BaseIdGenerator.CHAR_TABLE.length;

    @Override
    public String generateCode() {
        long number = this.generate();

        final StringBuilder sb = new StringBuilder();

        while (number > 0) {
            int mod = (int) (number % BaseIdGenerator.CHAR_TABLE_SIZE);
            sb.append(BaseIdGenerator.CHAR_TABLE[mod]);
            if (mod == 0) {
                mod = BaseIdGenerator.CHAR_TABLE_SIZE;
            }
            number = (number - mod) / BaseIdGenerator.CHAR_TABLE_SIZE;
        }

        sb.reverse();
        if (this.prefix != null) {
            return this.prefix + sb.toString();
        } else {
            return sb.toString();
        }
    }

    /**
     * 生成随机HEX数，常用做Token类。
     *
     * @return
     */
    @Override
    public String generateNonceToken() {
        final String code = this.generateCode() + BaseIdGenerator.RANDOM.nextLong();
        return DigestUtils.md5DigestAsHex(code.getBytes());
    }
}
