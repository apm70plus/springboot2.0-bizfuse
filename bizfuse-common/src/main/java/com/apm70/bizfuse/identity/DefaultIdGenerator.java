package com.apm70.bizfuse.identity;

import java.util.Calendar;

/**
 * 缺省流水号ID生成器。 <br>
 * long类型数值的范围： 92+233720+3685+4774807 (63bit) <br>
 * 生成规则： SiteID(2位) + YYMMDD(6位) + hhmm(4位) + 流水号(7位)。 <br>
 * 7位流水号可以满足每天1000万个ID分配， <br>
 * 算法上允许流水号溢出，溢出后在原来时间上加一分钟后重置流水号从零分配。 <br>
 * 每天第一次生成ID时会自动重置流水号。<br>
 * 若需要每天零时重置流水号，需要外部在零时调用reset方法。<br>
 * 每次重新启动JVM会重置流水号，一分钟间隔以上重启理论上不会产生重复流水号。<br>
 * 注意： 一分钟之内重启两次会产生重复ID；一分钟内生成1000万以上ID后，下一分钟重启也会产生重复ID，需要规避。<br>
 * 使用 SiteID作为集群JVM区别码（缺省一个JVM时为20），支持00~91共92个JVM区别码。<br>
 * 若需要更多集群节点（JVM区别码）的支持，可以使用一下算法生成ID（支持8192个）。<br>
 * SiteID(13bit) + YYMMDD(6+4+5bit) + hhmm(5+6bit) + 流水号(24bit)。<br>
 */
public class DefaultIdGenerator extends BaseIdGenerator {
    private long date = 0;
    private long time = 0;

    private long high = 0l;
    private long low = 0l;

    public DefaultIdGenerator(final long siteId) {
        this(siteId, null);
    }

    public DefaultIdGenerator(final long siteId, final String prefix) {
        super(siteId, prefix);
    }

    /**
     * 初始化。
     */
    @Override
    protected synchronized final void prepare() {
        final Calendar calendar = Calendar.getInstance();

        final long curDate =
                ((calendar.get(Calendar.YEAR) % 100L) * 10000L) + ((calendar.get(Calendar.MONTH) + 1) * 100L)
                + calendar.get(Calendar.DAY_OF_MONTH);
        if (curDate != this.date) {
            this.date = curDate;
            this.time = (calendar.get(Calendar.HOUR_OF_DAY) * 100L) + calendar.get(Calendar.MINUTE);
            this.high = ((this.siteId * 10000000000L) + (curDate * 10000L) + this.time) * 10000000L;
            this.low = 1L;
        }
        if (this.low == 10000000) {
            this.high = this.high + 10000000L;
            this.low = 1L;
        }
    }

    @Override
    protected final long high() {
        return this.high;
    }

    @Override
    protected final long low() {
        return this.low++;
    }

    @Override
    public synchronized void reset() {
        this.date = 0;
    }

    public static final void main(final String[] argv) {
        final IdGenerator idGenerator = new DefaultIdGenerator(20l, "SA");
        final long start = System.currentTimeMillis();
        String result = null;
        for (long i = 0; i < 1000L; i++) {
            final long id = idGenerator.generate();
            System.out.println(i + ": " + id);
            result = idGenerator.generateCode();
            System.out.println(i + ": " + result);
            result = idGenerator.generateNonceToken();
            System.out.println(i + ": " + result);
        }
        final long end = System.currentTimeMillis();
        System.out.println((end - start) + "ms");
    }
}
